package com.dnastack.ddap.explore.resource.controller;

import static org.springframework.http.HttpStatus.TEMPORARY_REDIRECT;

import com.dnastack.ddap.common.util.http.UriUtil;
import com.dnastack.ddap.common.util.http.XForwardUtil;
import com.dnastack.ddap.explore.resource.exception.ResourceAuthorizationException;
import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.OAuthState;
import com.dnastack.ddap.explore.resource.model.PaginatedResponse;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.dnastack.ddap.explore.resource.service.ResourceClientService;
import com.dnastack.ddap.explore.resource.service.UserCredentialService;
import com.dnastack.ddap.explore.resource.spi.ResourceClient;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1beta")
public class ResourceController {


    private static final String RESOURCE_LOGIN_STATE_KEY = "_resource_login_state_";

    private ResourceClientService resourceClientService;
    private UserCredentialService userCredentialService;

    @Autowired
    public ResourceController(ResourceClientService resourceClientService, UserCredentialService userCredentialService) {
        this.resourceClientService = resourceClientService;
        this.userCredentialService = userCredentialService;
    }


    @GetMapping(value = "/{realm}/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PaginatedResponse<Resource>> listResources(@PathVariable("realm") String realm, @RequestParam(value = "collection", required = false) List<String> collections, @RequestParam(value = "interface_type", required = false) List<String> interfaceTypesToFilter, @RequestParam(value = "interface_uri", required = false) List<String> interfaceUrisToFilter, @RequestParam(value = "page_token", required = false) String pageToken) {
        List<Id> collectionIdsToFilter = new ArrayList<>();
        if (collections != null) {
            collectionIdsToFilter.addAll(collections.stream().map(Id::decodeId).collect(Collectors.toList()));
        }
        return resourceClientService
            .listResources(realm, collectionIdsToFilter, interfaceTypesToFilter, interfaceUrisToFilter)
            .map(PaginatedResponse::new);

    }

    @GetMapping(value = "/{realm}/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Resource> getResource(@PathVariable("realm") String realm, @PathVariable("id") String resourceId) {
        Id id = Id.decodeId(resourceId);
        return resourceClientService.getClient(id.getSpiKey()).getResource(realm, id);
    }


    @GetMapping(value = "/{realm}/collections", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PaginatedResponse<Collection>> listCollections(@PathVariable("realm") String realm) {
        return resourceClientService.listCollections(realm).map(PaginatedResponse::new);
    }

    @GetMapping(value = "/{realm}/collections/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Collection> getCollection(@PathVariable("realm") String realm, @PathVariable("id") String collectionId) {
        Id id = Id.decodeId(collectionId);
        return resourceClientService.getClient(id.getSpiKey()).getCollection(realm, id);
    }

    @GetMapping("/{realm}/resources/authorize")
    public ResponseEntity<?> authorizeResources(WebSession session, ServerHttpRequest request,
        @PathVariable String realm,
        @RequestParam(required = false, name = "login_hint") String loginHint,
        @RequestParam(required = false, name = "redirect_uri") URI redirectUri,
        @RequestParam(required = false) String scope,
        @RequestParam("resource") List<String> authorizationIds,
        @RequestParam(defaultValue = "1h") String ttl) {

        //Guarantee there was not an issue sent from the front end
        final URI nonNullRedirectUri = redirectUri != null ? redirectUri : UriUtil.selfLinkToUi(request, realm, "");
        final URI postLoginEndpoint = getRedirectUri(request);
        Map<String, List<Id>> spiResourcesToAuthorize = new HashMap<>();
        authorizationIds.stream()
            .filter(id -> !Objects.equals("undefined", id))
            .map(Id::decodeId)
            .filter(id -> resourceClientService.getClient(id.getSpiKey()).resourceRequiresAutorization(id))
            .collect(Collectors.toList())
            .forEach(id -> spiResourcesToAuthorize.computeIfAbsent(id.getSpiKey(), (key) -> new ArrayList<>()).add(id));

        OAuthState lastState = null;
        for (var entry : spiResourcesToAuthorize.entrySet()) {
            OAuthState state = resourceClientService.getClient(entry.getKey())
                .prepareOauthState(realm, entry.getValue(), postLoginEndpoint, scope, loginHint, ttl);
            state.setNextState(lastState);
            state.setDestinationAfterLogin(nonNullRedirectUri);
            lastState = state;
        }

        if (lastState == null) {
            return ResponseEntity.status(TEMPORARY_REDIRECT).location(nonNullRedirectUri).build();
        } else {
            Map<String, Object> sessionAttributes = session.getAttributes();
            sessionAttributes.put(RESOURCE_LOGIN_STATE_KEY, lastState);
            return ResponseEntity.status(TEMPORARY_REDIRECT).location(lastState.getAuthUrl()).build();
        }

    }

    @GetMapping("/_/resources/authorize/callback")
    public Mono<? extends ResponseEntity<?>> handleTokenRequest(ServerHttpRequest request, WebSession session,
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String error,
        @RequestParam(value = "error_description", required = false) String errorDescription) {

        return Mono.defer(() -> {

            final OAuthState storedState = verifyStateAndClearSession(request, session);
            final ResourceClient resourceClient = resourceClientService.getClient(storedState.getSpiKey());
            final URI redirectUri = getRedirectUri(request);

            if (error != null) {
                return Mono.error(() -> {
                    List<String> resourceList = storedState.getResourceList().stream().map(Id::encodeId)
                        .collect(Collectors.toList());
                    return new ResourceAuthorizationException(errorDescription, resourceList, HttpStatus.UNAUTHORIZED);
                });
            } else {
                if (code == null) {
                    return Mono
                        .error(() -> new IllegalArgumentException("Could not complete authorization callback, missing required propert: code, in repsosne"));
                }
                return resourceClient.handleResponseAndGetCredentials(request, redirectUri, storedState, code)
                    .doOnNext(userCredentials -> {
                        if (userCredentials != null && !userCredentials.isEmpty()) {
                            userCredentialService.storeSessionBoundCredentialsForResource(session, userCredentials);
                        }
                    }).then(Mono.defer(() -> {
                        Optional<OAuthState> nextStateOpt = storedState.getNextState();
                        if (nextStateOpt.isPresent()) {
                            OAuthState nextState = nextStateOpt.get();
                            session.getAttributes().put(RESOURCE_LOGIN_STATE_KEY, nextState);
                            return Mono
                                .just(ResponseEntity.status(TEMPORARY_REDIRECT).location(nextState.getAuthUrl())
                                    .build());
                        } else {
                            URI redirectTo = storedState.getDestinationAfterLogin()
                                .map(possiblyRelativeUrl -> UriUtil.selfLinkToUi(request, storedState.getRealm(), "")
                                    .resolve(possiblyRelativeUrl))
                                .orElseGet(() -> UriUtil.selfLinkToUi(request, storedState.getRealm(), ""));

                            return Mono.just(ResponseEntity.status(TEMPORARY_REDIRECT).location(redirectTo).build());
                        }
                    }))
                    .doOnError(exception -> {
                        log.info("Encountered an error while handling authorization response", exception);
                        throw new IllegalArgumentException(exception);
                    });
            }
        });
    }


    private OAuthState verifyStateAndClearSession(ServerHttpRequest request, WebSession session) {
        final String stateStringParam = request.getQueryParams()
            .getFirst("state");
        final OAuthState storedState = session.getAttribute(RESOURCE_LOGIN_STATE_KEY);
        if (storedState == null) {
            throw new ResourceAuthorizationException("User's session does not have a valid 'state'", null, HttpStatus.BAD_REQUEST);
        }
        try {
            if (stateStringParam == null) {
                throw new ResourceAuthorizationException("Missing 'state' parameter", null, HttpStatus.BAD_REQUEST);
            }

            if (!Objects.equals(stateStringParam, storedState.getStateString())) {
                throw new ResourceAuthorizationException("CSRF state cookie mismatch", null, HttpStatus.FORBIDDEN);
            }

            if (storedState.getValidUntil().isBefore(ZonedDateTime.now())) {
                throw new ResourceAuthorizationException("Stale 'state' parameter", null, HttpStatus.BAD_REQUEST);
            }
            return storedState;
        } finally {
            session.getAttributes().put(RESOURCE_LOGIN_STATE_KEY, null);
        }
    }

    private URI getRedirectUri(ServerHttpRequest request) {
        return URI.create(XForwardUtil.getExternalPath(request, "/api/v1beta/_/resources/authorize/callback"));
    }


}