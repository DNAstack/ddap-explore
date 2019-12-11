package com.dnastack.ddap.explore.controller;

import com.dnastack.ddap.common.client.AuthAwareWebClientFactory;
import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.common.config.DamsConfig;
import com.dnastack.ddap.common.security.AuthCookieNotPresentInRequestException;
import com.dnastack.ddap.common.security.OAuthStateHandler;
import com.dnastack.ddap.common.security.TokenExchangePurpose;
import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import com.dnastack.ddap.common.security.UserTokenCookiePackager.CartTokenCookieName;
import com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieKind;
import com.dnastack.ddap.common.util.http.UriUtil;
import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import com.dnastack.ddap.ic.oauth.client.TokenExchangeException;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dnastack.ddap.common.util.http.XForwardUtil.getExternalPath;
import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.TEMPORARY_REDIRECT;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/{realm}/resources")
public class ResourceFlowController {

    private final UserTokenCookiePackager cookiePackager;
    private final OAuthStateHandler stateHandler;
    private final Set<DamProperties> dams;
    private AuthAwareWebClientFactory webClientFactory;

    @Autowired
    public ResourceFlowController(DamsConfig damsConfig,
                                  UserTokenCookiePackager cookiePackager,
                                  OAuthStateHandler stateHandler,
                                  AuthAwareWebClientFactory webClientFactory) {
        this.cookiePackager = cookiePackager;
        this.stateHandler = stateHandler;
        this.dams = Set.copyOf(damsConfig.getStaticDamsConfig().values());
        this.webClientFactory = webClientFactory;
    }

    @GetMapping("/checkout")
    public Mono<?> checkout(ServerHttpRequest request,
                            @RequestParam("resource") List<URI> resources) {
        final ReactiveDamClient damClient = lookupDamClient(resources);
        final CartTokenCookieName cartCookieName = new CartTokenCookieName(Set.copyOf(resources));
        final HttpCookie cartToken = request.getCookies()
                                            .getFirst(cartCookieName.cookieName());

        if (cartToken != null) {
            return damClient.checkoutCart(cartToken.getValue());
        } else {
            return Mono.error(new AuthCookieNotPresentInRequestException(cartCookieName.cookieName()));
        }
    }

    /**
     * Returns the absolute URL that points to the {@link #authorizeResources} controller method.
     *
     * @param request the current request (required for determining this service's hostname).
     * @param realm   the realm the returned URL should target.
     * @return Absolute URL of the URL an OAuth login flow should redirect to upon completion.
     */
    private static URI rootLoginRedirectUrl(ServerHttpRequest request, String realm) {
        return URI.create(getExternalPath(request,
                format("/api/v1alpha/%s/identity/login", realm)));
    }

    @GetMapping("/authorize")
    public Mono<? extends ResponseEntity<?>> authorizeResources(ServerHttpRequest request,
                                                                @PathVariable String realm,
                                                                @RequestParam(required = false) URI redirectUri,
                                                                @RequestParam(required = false) String scope,
                                                                @RequestParam("resource") List<URI> resources) {


        final URI postLoginTokenEndpoint = UriUtil.selfLinkToApi(request, realm, "resources/token");
        // FIXME better fallback page
        final URI nonNullRedirectUri = redirectUri != null ? redirectUri : cartCheckoutUrl(request, realm, resources);
        final String state = stateHandler.generateResourceState(nonNullRedirectUri, resources);
        // FIXME should separate resources by DAM
        final ReactiveDamOAuthClient oAuthClient = lookupDamOAuthClient(resources);
        final URI authorizeUri = oAuthClient.getAuthorizeUrl(realm, state, scope, postLoginTokenEndpoint, resources);

        URI cookieDomainPath = UriUtil.selfLinkToApi(request, realm, "resources");
        ResponseEntity<Object> redirectToAuthServer = ResponseEntity.status(TEMPORARY_REDIRECT)
                                                                    .location(authorizeUri)
                                                                    .header(SET_COOKIE, cookiePackager.packageToken(state, cookieDomainPath.getHost(), CookieKind.OAUTH_STATE).toString())
                                                                    .build();
        return Mono.just(redirectToAuthServer);
    }

    private URI cartCheckoutUrl(ServerHttpRequest request, @PathVariable String realm, List<URI> resources) {
        final URI baseUri = UriUtil.selfLinkToApi(request, realm, "resources/checkout");
        return UriComponentsBuilder.fromUri(baseUri)
                                   .queryParam("resource", resources.toArray())
                                   .build()
                                   .toUri();
    }

    // FIXME should move this into a client factory
    private ReactiveDamOAuthClient lookupDamOAuthClient(List<URI> resources) {
        if (resources.isEmpty()) {
            throw new IllegalArgumentException("Cannot look DAM from empty resource list");
        }
        final String testResourceUrl = resources.get(0).toString();
        return dams.stream()
                   // FIXME make this more resilient
                   .filter(dam -> testResourceUrl.startsWith(dam.getBaseUrl().toString()))
                   .map(dam -> new ReactiveDamOAuthClient(dam))
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find DAM for resource [%s]")));
    }

    // FIXME should move this into a client factory
    private ReactiveDamClient lookupDamClient(List<URI> resources) {
        if (resources.isEmpty()) {
            throw new IllegalArgumentException("Cannot look DAM from empty resource list");
        }
        final String testResourceUrl = resources.get(0).toString();
        return dams.stream()
                   // FIXME make this more resilient
                   .filter(dam -> testResourceUrl.startsWith(dam.getBaseUrl().toString()))
                   .map(dam -> new ReactiveDamClient(dam, webClientFactory))
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find DAM for resource [%s]")));
    }

    /**
     * OAuth 2 token exchange endpoint for DDAP, which acts as an OAuth 2 client to the Identity Concentrator.
     * <p>
     * This method's purpose is to handle the two HTTP exchanges involved:
     * <ol>
     * <li>the inbound request from the client (usually initiated by a redirect following successful authentication
     * with Identity Concentrator)</li>
     * <li>the outbound request to the Identity Concentrator, to exchange the code for the auth tokens</li>
     * </ol>
     * </p>
     *
     * @return a redirect to the main UI along with some set-cookie headers that store the user's authentication
     * info for subsequent requests.
     */
    @GetMapping("/token")
    public Mono<? extends ResponseEntity<?>> handleTokenRequest(ServerHttpRequest request,
                                                                @PathVariable String realm,
                                                                @RequestParam String code,
                                                                @RequestParam("state") String stateParam,
                                                                @CookieValue("oauth_state") String stateFromCookie) {
        final List<URI> resources = stateHandler.extractResource(stateParam)
                                                .stream()
                                                .flatMap(List::stream)
                                                .map(URI::create)
                                                .collect(Collectors.toList());
        final ReactiveDamOAuthClient oAuthClient = lookupDamOAuthClient(resources);
        return oAuthClient.exchangeAuthorizationCodeForTokens(realm, rootLoginRedirectUrl(request, realm), code)
                          .flatMap(tokenResponse -> {
                              TokenExchangePurpose tokenExchangePurpose = stateHandler.parseAndVerify(stateParam, stateFromCookie);
                              Optional<URI> customDestination = stateHandler.getDestinationAfterLogin(stateParam)
                                                                            .map(possiblyRelativeUrl -> UriUtil.selfLinkToUi(request, realm, "").resolve(possiblyRelativeUrl));
                              if (tokenExchangePurpose == TokenExchangePurpose.RESOURCE_AUTH) {
                                  final URI ddapDataBrowserUrl = customDestination.orElseGet(() -> UriUtil.selfLinkToUi(request, realm, ""));
                                  return Mono.just(assembleTokenResponse(ddapDataBrowserUrl, tokenResponse, Set.copyOf(resources)));
                              } else {
                                  throw new TokenExchangeException("Unrecognized purpose in token exchange");
                              }
                          })
                          .doOnError(exception -> log.info("Failed to negotiate token", exception));
    }

    /**
     * Examines the result of an auth-code-for-token exchange with the Identity Concentrator and creates a response
     * which sets the appropriate cookies on the user's client and redirects it to the appropriate part of the UI.
     *
     * @param token the token response from the outbound request we initiated with the Identity Concentrator.
     * @param resources
     * @return A response entity that sets the user's token cookies and redirects to the UI. Never null.
     */
    private ResponseEntity<?> assembleTokenResponse(URI redirectTo, TokenResponse token, Set<URI> resources) {
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
        } else {
            final String publicHost = redirectTo.getHost();
            final ResponseCookie resourceCookie = cookiePackager.packageToken(token.getAccessToken(), publicHost, new CartTokenCookieName(resources));
            return ResponseEntity.status(TEMPORARY_REDIRECT)
                                 .location(redirectTo)
                                 // FIXME remove old oauth_state cookie here
                                 .header(SET_COOKIE, resourceCookie.toString())
                                 .build();
        }
    }

}
