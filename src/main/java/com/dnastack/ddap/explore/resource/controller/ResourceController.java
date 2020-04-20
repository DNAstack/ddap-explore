package com.dnastack.ddap.explore.resource.controller;

import static org.springframework.http.HttpStatus.TEMPORARY_REDIRECT;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.common.config.DamsConfig;
import com.dnastack.ddap.common.security.InvalidOAuthStateException;
import com.dnastack.ddap.common.util.http.UriUtil;
import com.dnastack.ddap.common.util.http.XForwardUtil;
import com.dnastack.ddap.explore.dam.client.DamClientFactory;
import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.DamId;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.OauthState;
import com.dnastack.ddap.explore.resource.model.PaginatedResponse;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.dnastack.ddap.explore.resource.service.UserCredentialService;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import dam.v1.DamService;
import dam.v1.DamService.GetFlatViewsResponse.FlatView;
import dam.v1.DamService.ResourceResults.ResourceAccess;
import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1beta")
public class ResourceController {


    private static final String RESOURCE_LOGIN_STATE_KEY = "_resource_login_state_";

    private Map<String, ReactiveDamClient> damClients;
    private DamClientFactory damClientFactory;
    private Map<String, DamProperties> dams;
    private UserCredentialService userCredentialService;

    @Autowired
    public ResourceController(Map<String, ReactiveDamClient> damClients, DamClientFactory damClientFactory, DamsConfig damsConfig, UserCredentialService userCredentialService) {
        this.damClients = damClients;
        this.damClientFactory = damClientFactory;
        this.dams = Map.copyOf(damsConfig.getStaticDamsConfig());
        this.userCredentialService = userCredentialService;
    }


    @GetMapping(value = "/{realm}/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PaginatedResponse<Resource>> listResources(@PathVariable("realm") String realm, @RequestParam(value = "collection", required = false) List<String> collections, @RequestParam(value = "interface_type", required = false) List<String> interfaceTypesToFilter, @RequestParam(value = "interface_uri", required = false) List<String> interfaceUrisToFilter, @RequestParam(value = "page_token", required = false) String pageToken) {
        return Flux.concat(damClients.entrySet().stream().map(entry -> {
            List<Id> collectionIdsToFilter = new ArrayList<>();
            if (collections != null && !collections.isEmpty()) {
                collections.stream().map(Id::decodeId).forEach(collectionIdsToFilter::add);
            }

            return entry.getValue().getFlattenedViews(realm)
                .map(views -> views.values().stream().filter(view -> {
                    boolean keep = true;
                    if (!collectionIdsToFilter.isEmpty()) {
                        keep = collectionIdsToFilter.stream()
                            .anyMatch(id -> id.getCollectionId().equals(view.getResourceName()));
                    }
                    if (interfaceTypesToFilter != null && !interfaceTypesToFilter.isEmpty()) {
                        keep &= interfaceTypesToFilter.stream()
                            .anyMatch(type -> type.equals(view.getInterfaceName()));
                    }

                    if (interfaceUrisToFilter != null && !interfaceUrisToFilter.isEmpty()) {
                        keep &= shouldKeepInterfaceUri(view.getInterfaceUri(), interfaceUrisToFilter);
                    }
                    return keep;
                }).collect(Collectors.toList()))
                .map((views) -> {
                    LinkedHashMap<String, Resource> resources = new LinkedHashMap<>();
                    views.forEach((view) -> {
                        Id viewColId = new Id();
                        viewColId.setSpiKey(entry.getKey());
                        viewColId.setRealm(realm);
                        viewColId.setCollectionId(view.getResourceName());
                        DamId id = new DamId(viewColId);
                        id.setViewName(view.getViewName());
                        id.setRoleName(view.getRoleName());
                        String stringifiedId = id.encodeId();
                        String stringifiedColId = viewColId.encodeId();
                        Resource resource = resources
                            .computeIfAbsent(stringifiedId, (idKey -> viewToResource(stringifiedColId, stringifiedId, view)));
                        DamId authorizationId = new DamId(id);
                        authorizationId.setInterfaceType(view.getInterfaceName());
                        AccessInterface accessInterface = new AccessInterface();
                        accessInterface.setAuthorizationId(authorizationId.encodeId());
                        accessInterface.setType(view.getInterfaceName());
                        accessInterface
                            .setUri(view.getInterfaceUri() != null ? URI.create(view.getInterfaceUri()) : null);

                        resource.getInterfaces().add(accessInterface);
                    });
                    return resources.values();
                });
        }).collect(Collectors.toList()))
            .reduce(new ArrayList<Resource>(), (resources, damResources) -> {
                resources.addAll(damResources);
                return resources;
            }).map(PaginatedResponse::new);

    }


    private boolean shouldKeepInterfaceUri(String interfaceUri, List<String> testUris) {
        return testUris.stream().anyMatch(testUri -> {
            String testInerfaceUri = interfaceUri;
            if (!testInerfaceUri.endsWith("/") && testUri.length() > testInerfaceUri.length()) {
                testInerfaceUri = testInerfaceUri + "/";
            }
            return testUri.startsWith(testInerfaceUri);
        });
    }

    @GetMapping(value = "/{realm}/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Resource> getResource(@PathVariable("realm") String realm, @PathVariable("id") String resourceId) {
        DamId damId = new DamId(Id.decodeId(resourceId));
        if (!Objects.equals(realm, damId.getRealm())) {
            return Mono.error(() -> new NotFoundException("Resource does not exist within this realm"));
        }
        ReactiveDamClient damClient = damClients.get(damId.getSpiKey());
        if (damClient == null) {
            throw new IllegalArgumentException("No configured dam with id: " + damId.getSpiKey());
        }

        return damClient.getFlattenedViews(realm).map(flatViews -> {
            List<Entry<String, FlatView>> matchingEntries = flatViews.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(damId.toFlatViewPrefix()))
                .collect(Collectors.toList());

            if (matchingEntries.isEmpty()) {
                throw new NotFoundException("Could not locate resource with id: " + resourceId);
            }

            Resource resource = null;
            for (Entry<String, FlatView> entry : matchingEntries) {
                FlatView view = entry.getValue();
                if (resource == null) {
                    Id collectionId = new Id();
                    collectionId.setSpiKey(damId.getSpiKey());
                    collectionId.setRealm(realm);
                    collectionId.setCollectionId(damId.getCollectionId());
                    String stringifiedCollectionId = collectionId.encodeId();
                    resource = viewToResource(stringifiedCollectionId, resourceId, entry.getValue());
                }

                DamId authorizationId = new DamId(damId);
                authorizationId.setInterfaceType(view.getInterfaceName());
                AccessInterface accessInterface = new AccessInterface();
                accessInterface.setAuthorizationId(authorizationId.encodeId());
                accessInterface.setType(view.getInterfaceName());
                accessInterface
                    .setUri(view.getInterfaceUri() != null ? URI.create(view.getInterfaceUri()) : null);

                resource.getInterfaces().add(accessInterface);
            }
            return resource;
        });
    }


    @GetMapping(value = "/{realm}/collections", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PaginatedResponse<Collection>> listCollections(@PathVariable("realm") String realm) {
        return Flux.concat(damClients.entrySet().stream().map(entry -> entry.getValue().getResources(realm)
            .map(resources -> {
                List<Collection> collections = new ArrayList<>();
                resources.forEach((k, resource) -> {
                    Id id = new Id();
                    id.setSpiKey(entry.getKey());
                    id.setCollectionId(k);
                    id.setRealm(realm);
                    Collection collection = resourceToCollection(id, resource);
                    collections.add(collection);
                });
                return collections;
            })).collect(Collectors.toList()))
            .reduce(new ArrayList<Collection>(), (initial, collections) -> {
                initial.addAll(collections);
                return initial;
            })
            .map(PaginatedResponse::new);
    }

    @GetMapping(value = "/{realm}/collections/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Collection> getCollection(@PathVariable("realm") String realm, @PathVariable("id") String collectionId) {
        Id id = Id.decodeId(collectionId);
        if (!Objects.equals(realm, id.getRealm())) {
            return Mono.error(() -> new NotFoundException("Resource does not exist within this realm"));
        }
        ReactiveDamClient damClient = damClients.get(id.getSpiKey());
        if (damClient == null) {
            throw new IllegalArgumentException("No configured dam with id: " + id.getSpiKey());
        }
        return damClient.getResource(realm, id.getCollectionId())
            .map(resource -> resourceToCollection(id, resource));
    }

    @GetMapping("/{realm}/resources/authorize")
    public ResponseEntity<?> authorizeResources(WebSession session, ServerHttpRequest request,
        @PathVariable String realm,
        @RequestParam(required = false, name = "login_hint") String loginHint,
        @RequestParam(required = false, name = "redirect_uri") URI redirectUri,
        @RequestParam(required = false) String scope,
        @RequestParam("resource") List<String> authorizationIds,
        @RequestParam(defaultValue = "1h") String ttl) {

        final URI nonNullRedirectUri = redirectUri != null ? redirectUri : UriUtil.selfLinkToUi(request, realm, "");
        final URI postLoginEndpoint = getRedirectUri(request);
        Map<String, List<DamId>> damResourceGroups = new HashMap<>();
        authorizationIds.stream()
            .map(authorizationId -> new DamId(Id.decodeId(authorizationId)))
            .collect(Collectors.toList())
            .forEach(id -> damResourceGroups.computeIfAbsent(id.getSpiKey(), (key) -> new ArrayList<>()).add(id));

        OauthState lastState = null;
        for (var e : damResourceGroups.entrySet()) {
            final ReactiveDamOAuthClient client = damClientFactory.getDamOAuthClient(e.getKey());
            final List<DamId> resources = e.getValue();
            final List<URI> uris = resources.stream().map(id -> URI.create(String
                .format("/dam/%s/resources/%s/views/%s/roles/%s/interfaces/%s", realm, id.getCollectionId(), id
                    .getViewName(), id
                    .getRoleName(), id.getInterfaceType())))
                .map(uri -> dams.get(e.getKey()).getBaseUrl().resolve(uri)).collect(Collectors.toList());
            final String stateString = UUID.randomUUID().toString();
            final ZonedDateTime validUntil = ZonedDateTime.now().plusMinutes(10);
            final URI authUrl = client
                .getAuthorizeUrl(realm, stateString, scope, postLoginEndpoint, uris, loginHint, ttl);
            lastState = new OauthState(stateString, validUntil, parseDuration(ttl), realm, authUrl, nonNullRedirectUri, resources, lastState);
        }

        Map<String, Object> sessionAttributes = session.getAttributes();
        sessionAttributes.put(RESOURCE_LOGIN_STATE_KEY, lastState);
        return ResponseEntity.status(TEMPORARY_REDIRECT).location(lastState.getAuthUrl()).build();

    }

    @GetMapping("/_/resources/authorize/callback")
    public Mono<? extends ResponseEntity<?>> handleTokenRequest(ServerHttpRequest request, WebSession session,
        @RequestParam String code) {

        final OauthState storedState = verifyStateAndClearSession(request, session);
        final ReactiveDamOAuthClient oAuthClient = damClientFactory.getDamOAuthClient(storedState.getSpiKey());
        final URI redirectUri = getRedirectUri(request);
        final String realm = storedState.getRealm();

        return oAuthClient.exchangeAuthorizationCodeForTokens(realm, redirectUri, code)
            .flatMap(tokenResponse -> assembleResponse(request, session, storedState, tokenResponse))
            .doOnError(exception -> {
                log.info("Failed to negotiate token", exception);
                throw new IllegalArgumentException(exception);
            });
    }

    private Duration parseDuration(String period) {
        try {
            if (!period.startsWith("PT")) {
                period = "PT" + period;

            }
            return Duration.parse(period);
        } catch (Exception e) {
            return Duration.ofHours(1);
        }
    }

    private Mono<ResponseEntity<?>> assembleResponse(ServerHttpRequest request, WebSession session, OauthState currentState, TokenResponse token) {
        Set<String> missingItems = new HashSet<>();
        if (token == null) {
            missingItems.add("token");
        } else {
            if (token.getAccessToken() == null) {
                missingItems.add("access_token");
            }
        }

        if (!missingItems.isEmpty()) {
            throw new IllegalArgumentException("Incomplete token response: missing " + missingItems);
        }

        return eagerlyFetchAndPersistCartTokens(session, currentState, token.getAccessToken())
            .then(Mono.defer(() -> {

                Optional<OauthState> nextStateOpt = currentState.getNextState();
                if (nextStateOpt.isPresent()) {

                    OauthState nextState = nextStateOpt.get();
                    session.getAttributes().put(RESOURCE_LOGIN_STATE_KEY, nextState);
                    return Mono
                        .just(ResponseEntity.status(TEMPORARY_REDIRECT).location(nextState.getAuthUrl()).build());
                } else {
                    URI redirectTo = currentState.getDestinationAfterLogin()
                        .map(possiblyRelativeUrl -> UriUtil.selfLinkToUi(request, currentState.getRealm(), "")
                            .resolve(possiblyRelativeUrl))
                        .orElseGet(() -> UriUtil.selfLinkToUi(request, currentState.getRealm(), ""));

                    return Mono.just(ResponseEntity.status(TEMPORARY_REDIRECT).location(redirectTo).build());
                }
            }));
    }


    private Mono<Void> eagerlyFetchAndPersistCartTokens(WebSession session, OauthState currentState, String token) {

        return damClientFactory.getDamClient(currentState.getSpiKey()).checkoutCart(token)
            .map(result -> {
                result.getResourcesMap()
                    .forEach((key, value) -> {
                        DamId damAuthorizationId = getDamIdForResourceUri(URI.create(key), currentState
                            .getResourceList());
                        ResourceAccess resourceAccess = result.getAccessMap().get(value.getAccess());
                        String accessToken = resourceAccess.getCredentialsMap().get("access_token");
                        userCredentialService
                            .storeSessionBoundTokenForResource(session, damAuthorizationId, ZonedDateTime.now()
                                .plus(currentState.getTtl()), accessToken);
                    });
                return Mono.empty();
            }).then(Mono.empty());

    }

    private DamId getDamIdForResourceUri(URI baseUri, List<DamId> ids) {
        final String template = "/dam/%s/resources/%s/views/%s/roles/%s/interfaces/%s";
        return ids.stream().filter(id -> {
            URI toResolve = URI
                .create(String
                    .format(template, id.getRealm(), id.getCollectionId(), id.getViewName(), id.getRoleName(), id
                        .getInterfaceType()));
            return baseUri.equals(baseUri.resolve(toResolve));
        })
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Could not find dam id"));
    }


    private OauthState verifyStateAndClearSession(ServerHttpRequest request, WebSession session) {
        final String stateStringParam = request.getQueryParams()
            .getFirst("state");
        final OauthState storedState = session.getAttribute(RESOURCE_LOGIN_STATE_KEY);
        try {
            if (storedState == null) {
                throw new InvalidOAuthStateException("Missing 'state' in User's Session", null, null);
            }

            if (stateStringParam == null) {
                throw new InvalidOAuthStateException("Missing 'state' parameter", null, null);
            }

            if (!Objects.equals(stateStringParam, storedState.getStateString())) {
                throw new InvalidOAuthStateException("CSRF state cookie mismatch", null, null);
            }

            if (storedState.getValidUntil().isBefore(ZonedDateTime.now())) {
                throw new InvalidOAuthStateException("Stale 'state' parameter", null, null);
            }

            return storedState;
        } finally {
            session.getAttributes().put(RESOURCE_LOGIN_STATE_KEY, null);
        }
    }

    private URI getRedirectUri(ServerHttpRequest request) {
        return URI.create(XForwardUtil.getExternalPath(request, "/api/v1beta/_/resources/authorize/callback"));
    }

    private Collection resourceToCollection(Id id, DamService.Resource resource) {
        Collection collection = new Collection();
        collection.setId(id.encodeId());
        collection.setName(resource.getUiOrDefault("label", ""));
        String imageUrlString = resource.getUiOrDefault("imageUrl", null);
        collection.setImageUrl(imageUrlString == null ? null : URI.create(imageUrlString));
        collection.setDescription(resource.getUiOrDefault("description", ""));
        Map<String, String> metadata = new HashMap<>();
        metadata.put("applyUrl", resource.getUiOrDefault("applyUrl", null));
        metadata.put("access", resource.getUiOrDefault("access", null));
        metadata.put("infoUrl", resource.getUiOrDefault("infoUrl", null));
        metadata.put("troubleshootUrl", resource.getUiOrDefault("troubleshootUrl", null));
        metadata.put("owner", resource.getUiOrDefault("owner", null));
        metadata.put("tags", resource.getUiOrDefault("tags", null));
        collection.setMetadata(metadata);
        return collection;
    }

    private Resource viewToResource(String collectionId, String id, FlatView view) {
        String description = view.getViewUiOrDefault("description", "") + " - " + view
            .getRoleUiOrDefault("description", "");
        String name =
            view.getViewUiOrDefault("label", view.getViewName()) + " - " + view.getRoleUiOrDefault("label", view
                .getRoleName());
        String imageUrlString =
            view.getViewUiMap().containsKey("imageUrl") ? view.getViewUiMap().get("imageUrl")
                : view.getResourceUiMap().get("imageUrl");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("serviceName", view.getServiceName());
        metadata.put("platform", view.getPlatform());
        metadata.put("platformService", view.getPlatformService());
        metadata.put("contentType", view.getContentType());
        metadata.putAll(view.getLabelsMap());
        return Resource.newBuilder().id(id).collectionId(collectionId).description(description)
            .name(name).imageUrl(
                imageUrlString != null && !imageUrlString.isEmpty() ? URI.create(imageUrlString)
                    : null)
            .interfaces(new ArrayList<>())
            .metadata(metadata).build();
    }


}