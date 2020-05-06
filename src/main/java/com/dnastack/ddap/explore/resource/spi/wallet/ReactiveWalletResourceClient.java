package com.dnastack.ddap.explore.resource.spi.wallet;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.common.security.InvalidTokenException;
import com.dnastack.ddap.explore.resource.exception.ResourceAuthorizationException;
import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.Id.CollectionId;
import com.dnastack.ddap.explore.resource.model.Id.InterfaceId;
import com.dnastack.ddap.explore.resource.model.Id.ResourceId;
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
    public Mono<List<Resource>> listResources(String realm, List<CollectionId> collectionsToFilter, List<String> interfaceTypesToFilter, List<String> interfaceUrisToFilter) {
        return Mono.fromCallable(() ->
            config.getResources().entrySet().stream().filter(walletResourceEntry -> {
                boolean keep = true;
                WalletResource walletResource = walletResourceEntry.getValue();
                String id = walletResourceEntry.getKey();
                if (collectionsToFilter != null && !collectionsToFilter.isEmpty()) {
                    CollectionId collectionId = getInterfaceIdFromWalletResource(realm, getSpiKey(), walletResource, id)
                        .toCollectionId();
                    keep = collectionsToFilter.stream()
                        .anyMatch(collectionId::equals);
                }

                if (interfaceTypesToFilter != null) {
                    keep &= shouldKeepInterfaceType(walletResource.getInterfaceType(), interfaceTypesToFilter);
                }

                if (interfaceUrisToFilter != null) {
                    keep &= shouldKeepInterfaceUri(walletResource.getInterfaceUri().toString(), interfaceUrisToFilter);
                }

                return keep;
            }).map(walletResource -> walletResourceToResource(realm, getSpiKey(), walletResource
                .getValue(), walletResource.getKey()))
                .collect(Collectors.toList()));
    }

    @Override
    public Mono<Resource> getResource(String realm, ResourceId id) {
        return Mono.justOrEmpty(idToResource(realm, id))
            .map(walletResource -> walletResourceToResource(realm, getSpiKey(), walletResource, id.getId()))
            .switchIfEmpty(Mono.error(new NotFoundException("Could not locate resource with id: " + id.encodeId())));
    }

    @Override
    public Mono<List<Collection>> listCollections(String realm) {
        return Mono.fromCallable(() -> config.getCollections().entrySet().stream()
            .map(collection -> copyCollectionFromConfig(realm, collection.getKey(), collection.getValue()))
            .collect(Collectors.toList()));
    }

    @Override
    public Mono<Collection> getCollection(String realm, CollectionId collectionId) {
        return Mono.justOrEmpty(idToCollection(realm, collectionId))
            .map(collection -> copyCollectionFromConfig(realm, collectionId.getId(), collection))
            .switchIfEmpty(Mono.error(new NotFoundException(
                "Could not locate collection with id: " + collectionId.encodeId())));
    }

    @Override
    public List<OAuthState> prepareOauthState(String realm, List<InterfaceId> resources, URI postLoginRedirect, String scopes, String loginHint, String ttl) {

        Map<String, Map<String, List<InterfaceId>>> groupedResources = groupResourceByAudienceAndScope(realm, resources);

        if (groupedResources.isEmpty()) {
            throw new ResourceAuthorizationException("Could not locate resources to authorize", HttpStatus.BAD_REQUEST, resources);
        }

        List<OAuthState> states = new ArrayList<>();
        for (var resourcesForAudience : groupedResources.entrySet()) {
            String aud = resourcesForAudience.getKey();
            Map<String, List<InterfaceId>> scopedResources = resourcesForAudience.getValue();

            if (scopedResources.isEmpty()) {
                throw new ResourceAuthorizationException("Could not locate resources to authorize", HttpStatus.BAD_REQUEST, resources);
            }

            for (var resourcesForScope : scopedResources.entrySet()) {
                String configuredScope = resourcesForScope.getKey();
                List<InterfaceId> resourceList = resourcesForScope.getValue();

                ZonedDateTime validUntil = ZonedDateTime.now().plusMinutes(10);
                String state = UUID.randomUUID().toString();
                String scope = combineScopes(configuredScope, combineScopes(OIDC_SCOPES, scopes));
                URI authorizationUrl = getAuthorizationUrl(aud, scope, state, postLoginRedirect, loginHint, ttl);
                states
                    .add(new OAuthState(state, validUntil, ttl, realm, authorizationUrl, postLoginRedirect, resourceList));
            }
        }

        return states;
    }

    @Override
    public Mono<List<UserCredential>> handleResponseAndGetCredentials(ServerHttpRequest exchange, URI redirectUri, OAuthState currentState, String code) {
        return Mono.defer(() -> {

            String scope = idToResource(currentState.getRealm(), currentState.getResourceList().get(0).toResourceId())
                .get()
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

    private Map<String, Map<String, List<InterfaceId>>> groupResourceByAudienceAndScope(String realm, List<InterfaceId> resourceIds) {
        Map<String, Map<String, List<InterfaceId>>> groupedResources = new HashMap<>();
        for (InterfaceId id : resourceIds) {
            WalletResource resource = idToResource(realm, id.toResourceId())
                .orElseThrow(() -> new NotFoundException("Could not locate resource with id:  " + id.encodeId()));
            Map<String, List<InterfaceId>> idsForAudience = groupedResources
                .computeIfAbsent(resource.getAudience(), (aud) -> new HashMap<>());
            List<InterfaceId> idsForScopes = idsForAudience
                .computeIfAbsent(resource.getScope(), (scope) -> new ArrayList<>());
            idsForScopes.add(id);
        }
        return groupedResources;
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
            userCredential.setInterfaceId(id.encodeId());
            return userCredential;
        }).collect(Collectors.toList());
    }


    private Optional<WalletResource> idToResource(String realm, ResourceId id) {
        return Optional.ofNullable(config.getResources().get(id.getId()));
    }

    private Optional<Collection> idToCollection(String realm, CollectionId id) {
        return Optional.ofNullable(config.getCollections().get(id.getId()));
    }

    public Resource walletResourceToResource(String realm, String spikey, WalletResource walletResource, String resourceId) {
        InterfaceId interfaceId = getInterfaceIdFromWalletResource(realm, spikey, walletResource, resourceId);
        return Resource.newBuilder()
            .id(interfaceId.toResourceId().encodeId())
            .collectionId(interfaceId.toCollectionId().encodeId())
            .name(walletResource.getName())
            .imageUrl(walletResource.getImageUrl())
            .description(walletResource.getDescription())
            .interfaces(List
                .of(new AccessInterface(walletResource.getInterfaceType(), walletResource.getInterfaceUri(), interfaceId
                    .encodeId(), false)))
            .metadata(walletResource.getMetadata() != null ? new HashMap<>(walletResource.getMetadata()) : null)
            .build();
    }

    public InterfaceId getInterfaceIdFromWalletResource(String realm, String spikey, WalletResource walletResource, String resourceId) {
        InterfaceId id = new InterfaceId();
        id.setRealm(realm);
        id.setSpiKey(spikey);
        id.setResourceId(resourceId);
        id.setCollectionId(walletResource.getCollectionId());
        id.setType(walletResource.getInterfaceType());
        return id;
    }

    private Collection copyCollectionFromConfig(String realm, String id, Collection collection) {
        CollectionId collectionId = new CollectionId();
        collectionId.setSpiKey(getSpiKey());
        collectionId.setRealm(realm);
        collectionId.setId(id);

        return Collection.newBuilder()
            .id(collectionId.encodeId())
            .name(collection.getName())
            .imageUrl(collection.getImageUrl())
            .description(collection.getDescription())
            .metadata(collection.getMetadata() != null ? new HashMap<>(collection.getMetadata()) : null)
            .build();
    }


}
