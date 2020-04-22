package com.dnastack.ddap.explore.resource.spi.wallet;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.common.security.InvalidTokenException;
import com.dnastack.ddap.explore.resource.exception.ResourceAuthorizationException;
import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.OAuthState;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import com.dnastack.ddap.explore.resource.spi.ResourceClient;
import com.dnastack.ddap.ic.oauth.client.TokenExchangeException;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;


public class ReactiveWalletResourceClient implements ResourceClient {

    private final String spiKey;
    private final WalletResourceClientConfig config;
    private final static String OIDC_SCOPES = "openid profile email";

    public ReactiveWalletResourceClient(String spiKey, WalletResourceClientConfig config) {
        this.spiKey = spiKey;
        this.config = config;
    }

    @Override
    public String getSpiKey() {
        return spiKey;
    }

    @Override
    public Mono<List<Resource>> listResources(String realm, List<Id> collectionsToFilter, List<String> interfaceTypesToFilter, List<String> interfaceUrisToFilter) {
        return Mono.fromCallable(() ->
            config.getResources().stream().filter(walletResource -> {
                boolean keep = true;
                if (collectionsToFilter != null && !collectionsToFilter.isEmpty()) {
                    keep = collectionsToFilter.stream()
                        .anyMatch(id -> id.getRealm().equals(realm) && id.getCollectionId()
                            .equals(walletResource.getCollectionName())
                            && id.getSpiKey()
                            .equals(getSpiKey()));
                }

                if (interfaceTypesToFilter != null) {
                    keep &= interfaceTypesToFilter.contains(walletResource.getInterfaceType());
                }

                if (interfaceUrisToFilter != null) {
                    keep &= shouldKeepInterfaceUri(walletResource.getInterfaceUri().toString(), interfaceUrisToFilter);
                }

                return keep;
            }).map(walletResource -> walletResource.toResource(realm, getSpiKey()))
                .collect(Collectors.toList()));
    }

    @Override
    public Mono<Resource> getResource(String realm, Id id) {
        return Mono.fromCallable(() -> {
            if (!realm.equals(id.getRealm())) {
                throw new IllegalArgumentException("Resource does not exist in this realm");
            }

            return idToResource(realm, id)
                .map(walletResource -> walletResource.toResource(realm, getSpiKey()))
                .orElseThrow(() -> new NotFoundException("Could not locate resource with id: " + id.encodeId()));

        });
    }

    @Override
    public Mono<List<Collection>> listCollections(String realm) {
        return Mono.fromCallable(() -> config.getCollections().stream()
            .map(collection -> copyCollectionFromConfig(realm, collection)).collect(Collectors.toList()));
    }

    @Override
    public Mono<Collection> getCollection(String realm, Id collectionId) {
        return Mono.fromCallable(() -> config.getCollections().stream()
            .filter(collection -> collection.getName().equals(collectionId.getCollectionId()))
            .map(collection -> copyCollectionFromConfig(realm, collection))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(
                "Could not locate collection with id: " + collectionId.encodeId())));
    }

    @Override
    public OAuthState prepareOauthState(String realm, List<Id> resources, URI postLoginRedirect, String scopes, String loginHint, String ttl) {

        Map<String, Map<String, List<Id>>> groupedResources = groupResourceByAudienceAndScope(realm, resources);

        if (groupedResources.isEmpty()) {
            throw new ResourceAuthorizationException("Could not locate resources to authorize", HttpStatus.BAD_REQUEST, resources);
        }

        OAuthState lastAuth = null;
        for (var resourcesForAudience : groupedResources.entrySet()) {
            String aud = resourcesForAudience.getKey();
            Map<String, List<Id>> scopedResources = resourcesForAudience.getValue();

            if (scopedResources.isEmpty()) {
                throw new ResourceAuthorizationException("Could not locate resources to authorize", HttpStatus.BAD_REQUEST, resources);
            }

            for (var resourcesForScope : scopedResources.entrySet()) {
                String configuredScope = resourcesForScope.getKey();
                List<Id> resourceList = resourcesForScope.getValue();

                ZonedDateTime validUntil = ZonedDateTime.now().plusMinutes(10);
                String state = UUID.randomUUID().toString();
                String scope = combineScopes(configuredScope, combineScopes(OIDC_SCOPES, scopes));
                URI authorizationUrl = getAuthorizationUrl(aud, scope, state, postLoginRedirect, loginHint, ttl);
                OAuthState oAuthState = new OAuthState(state, validUntil, ttl, realm, authorizationUrl, postLoginRedirect, resourceList);
                oAuthState.setNextState(lastAuth);
                lastAuth = oAuthState;
            }
        }

        return lastAuth;
    }

    @Override
    public Mono<List<UserCredential>> handleResponseAndGetCredentials(ServerHttpRequest exchange, URI redirectUri, OAuthState currentState, String code) {
        return Mono.defer(() -> {

            String scope = idToResource(currentState.getRealm(), currentState.getResourceList().get(0)).get()
                .getScope();

            return WebClientFactory.getWebClient()
                .post()
                .uri(config.getTokenUrl())
                .header(AUTHORIZATION, "Basic " + encodeBasicAuth(config.getClientId(), config.getClientSecret()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("scope", scope)
                    .with("code", code))
                .exchange()
                .flatMap(this::extractIdpTokens)
                .onErrorMap(ex -> new InvalidTokenException(ex.getMessage()))
                .map(tokenResponse -> tokenResponseToUserCredential(tokenResponse, currentState));
        });
    }

    private Map<String, Map<String, List<Id>>> groupResourceByAudienceAndScope(String realm, List<Id> resourceIds) {
        Map<String, Map<String, List<Id>>> groupedResources = new HashMap<>();
        for (Id id : resourceIds) {
            WalletResource resource = idToResource(realm, id)
                .orElseThrow(() -> new NotFoundException("Could not locate resource with id:  " + id.encodeId()));
            Map<String, List<Id>> idsForAudience = groupedResources
                .computeIfAbsent(resource.getAudience(), (aud) -> new HashMap<>());
            List<Id> idsForScopes = idsForAudience
                .computeIfAbsent(resource.getScope(), (scope) -> new ArrayList<>());
            idsForScopes.add(id);
        }
        return groupedResources;
    }

    private Optional<WalletResource> idToResource(String realm, Id id) {
        return config.getResources().stream()
            .filter(walletResource -> id.getRealm().equals(realm) && id.getSpiKey().equals(spiKey) && walletResource
                .idEquals(id)).findFirst();
    }

    private URI getAuthorizationUrl(String audience, String scope, String state, URI redirectUri, String loginHint, String ttl) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(config.getAuthorizationUrl())
            .queryParam("client_id", config.getClientId())
            .queryParam("audience", audience)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .queryParam("state", state)
            .queryParam("scope", scope)
            .queryParam("ttl", ttl);
        if (loginHint != null) {
            builder.queryParam("login_hint", loginHint);
        }
        return builder.build().toUri();
    }

    private String combineScopes(String configuredScopes, String requestedScopes) {
        if (requestedScopes == null) {
            return configuredScopes;
        }
        List<String> configuredScopeList = new ArrayList<>(Arrays.asList(configuredScopes.split("\\s")));
        List<String> requestedScopeList = Arrays.asList(requestedScopes.split("\\s"));

        requestedScopeList.forEach(scope -> {
            if (!configuredScopeList.contains(scope)) {
                configuredScopeList.add(scope);
            }
        });

        return String.join(" ", configuredScopeList);
    }

    private String encodeBasicAuth(String user, String password) {
        // NOTE: Copied from BaseReactiveOauthClient
        return Base64.getEncoder()
            .encodeToString((user + ":" + password).getBytes());
    }

    private Mono<TokenResponse> extractIdpTokens(ClientResponse idpTokenResponse) {
        // NOTE: Copied from BaseReactiveOauthClient
        if (idpTokenResponse.statusCode().is2xxSuccessful() && contentTypeIsApplicationJson(idpTokenResponse)) {
            return idpTokenResponse.bodyToMono(TokenResponse.class);
        } else {
            return idpTokenResponse.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new TokenExchangeException(errorBody)));
        }
    }

    private static boolean contentTypeIsApplicationJson(ClientResponse response) {
        // NOTE: Copied from BaseReactiveOauthClient
        return response.headers()
            .contentType()
            .filter(mediaType -> mediaType.isCompatibleWith(APPLICATION_JSON))
            .isPresent();
    }

    private List<UserCredential> tokenResponseToUserCredential(TokenResponse tokenResponse, OAuthState currentState) {
        if (tokenResponse.getAccessToken() == null) {
            throw new TokenExchangeException("Token response does not contain valid access token");
        }

        return currentState.getResourceList().stream().map(id -> {
            UserCredential userCredential = new UserCredential();
            Map<String, String> credentials = new HashMap<>();
            credentials.put("access_token", tokenResponse.getAccessToken());
            credentials.put("refresh_token", tokenResponse.getRefreshToken());
            credentials.put("id_token", tokenResponse.getIdToken());

            Duration ttl = currentState.getTtl();
            if (tokenResponse.getAdditionalProperties().containsKey("expires_in")) {
                int expiresIn = (int) tokenResponse.getAdditionalProperties().get("expires_in");
                ttl = Duration.ofSeconds(expiresIn);
            }
            userCredential.setExpirationTime(ZonedDateTime.now().plus(ttl));
            userCredential.setCredentials(credentials);
            userCredential.setAuthorizationId(id.encodeId());
            return userCredential;
        }).collect(Collectors.toList());
    }


    private Collection copyCollectionFromConfig(String realm, Collection collection) {
        Id collectionId = new Id();
        collectionId.setRealm(realm);
        collectionId.setSpiKey(getSpiKey());
        collectionId.setCollectionId(collection.getName());

        return Collection.newBuilder()
            .id(collectionId.encodeId())
            .name(collection.getName())
            .imageUrl(collection.getImageUrl())
            .description(collection.getDescription())
            .metadata(collection.getMetadata() != null ? new HashMap<>(collection.getMetadata()) : null)
            .build();
    }


}
