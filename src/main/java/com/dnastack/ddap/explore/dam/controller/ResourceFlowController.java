package com.dnastack.ddap.explore.dam.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.common.config.DamsConfig;
import com.dnastack.ddap.common.security.AuthCookieNotPresentInRequestException;
import com.dnastack.ddap.common.security.OAuthStateHandler;
import com.dnastack.ddap.common.security.TokenExchangePurpose;
import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import com.dnastack.ddap.common.security.UserTokenCookiePackager.TokenKind;
import com.dnastack.ddap.common.util.http.UriUtil;
import com.dnastack.ddap.explore.dam.client.DamClientFactory;
import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import com.dnastack.ddap.explore.security.CartTokenCookieName;
import com.dnastack.ddap.ic.oauth.client.TokenExchangeException;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import dam.v1.DamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

import static com.dnastack.ddap.common.security.UserTokenCookiePackager.BasicServices.DAM;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TEMPORARY_REDIRECT;

@Slf4j
@RestController
public class ResourceFlowController {

    private final UserTokenCookiePackager cookiePackager;
    private final OAuthStateHandler stateHandler;
    private final Map<String, DamProperties> dams;
    private final DamClientFactory damClientFactory;

    @Autowired
    public ResourceFlowController(DamsConfig damsConfig,
                                  UserTokenCookiePackager cookiePackager,
                                  OAuthStateHandler stateHandler,
                                  DamClientFactory damClientFactory) {
        this.cookiePackager = cookiePackager;
        this.stateHandler = stateHandler;
        this.dams = Map.copyOf(damsConfig.getStaticDamsConfig());
        this.damClientFactory = damClientFactory;
    }

    @GetMapping("/api/v1alpha/realm/{realm}/resources/checkout")
    public Mono<ResponseEntity<DamService.ResourceResults>> checkout(ServerHttpRequest request,
                                                                     @PathVariable String realm,
                                                                     @RequestParam("resource") List<String> damIdResourcePairs) {
        final List<URI> resources = getResourcesFrom(realm, damIdResourcePairs);

        final ReactiveDamClient damClient = lookupDamClient(resources);
        final CartTokenCookieName cartCookieName = new CartTokenCookieName(Set.copyOf(resources));
        final Optional<String> extractedCartToken = cookiePackager
            .extractTokenIgnoringInvalid(request, cartCookieName)
            .map(UserTokenCookiePackager.CookieValue::getClearText);

        return extractedCartToken.map(s -> damClient.checkoutCart(s)
                                                    .map(ResponseEntity::ok))
                                 .orElseGet(() -> Mono.error(new AuthCookieNotPresentInRequestException(cartCookieName.cookieName())));
    }

    @GetMapping("/api/v1alpha/realm/{realm}/resources/authorize")
    public ResponseEntity<?> authorizeResources(ServerHttpRequest request,
                                                                @PathVariable String realm,
                                                                @RequestParam(required = false) String loginHint,
                                                                @RequestParam(required = false) URI redirectUri,
                                                                @RequestParam(required = false) String scope,
                                                                @RequestParam("resource") List<String> damIdResourcePairs) {
        final List<URI> resources = getResourcesFrom(realm, damIdResourcePairs);
        URI cookieDomainPath = UriUtil.selfLinkToApi(request, realm, "resources");

        // FIXME better fallback page
        final URI nonNullRedirectUri = redirectUri != null ? redirectUri : cartCheckoutUrl(request, realm, resources);
        final String state = stateHandler.generateResourceState(nonNullRedirectUri, realm, resources);

        final URI postLoginTokenEndpoint = getRedirectUri(request);

        // FIXME should separate resources by DAM
        final ReactiveDamOAuthClient oAuthClient = damClientFactory.lookupDamOAuthClient(resources);
        final URI authorizeUri = oAuthClient.getAuthorizeUrl(realm, state, scope, postLoginTokenEndpoint, resources, loginHint);
        return ResponseEntity.status(TEMPORARY_REDIRECT)
                             .location(authorizeUri)
                             .header(SET_COOKIE, cookiePackager.packageToken(state, cookieDomainPath.getHost(), DAM.cookieName(TokenKind.OAUTH_STATE)).toString())
                             .build();

    }

    @GetMapping("/api/v1alpha/realm/{realm}/resources/deauthorize")
    public ResponseEntity<?> deauthorizeResources(ServerHttpRequest request,
                                                  @PathVariable String realm,
                                                  @RequestParam(required = false) URI redirectUri) {
        URI cookieDomainPath = UriUtil.selfLinkToApi(request, realm, "resources");

        // Nuke all cookies
        // This is to deal with stale cart checkout tokens.
        HttpHeaders headers = new HttpHeaders();
        request.getCookies().forEach((name, cookies) -> headers.set(SET_COOKIE, format("%s=; Max-Age=0; Path=/", name)));

        var needRedirection = redirectUri != null;
        var responseBuilder = ResponseEntity.status(needRedirection ? TEMPORARY_REDIRECT : OK)
                .headers(headers)
                .header(SET_COOKIE,
                        cookiePackager.packageToken("",
                                cookieDomainPath.getHost(),
                                DAM.cookieName(TokenKind.OAUTH_STATE)).toString()
                );

        if (needRedirection) {
            responseBuilder = responseBuilder.location(redirectUri);
        }

        return responseBuilder.build();

    }

    private URI getRedirectUri(ServerHttpRequest request) {
        return UriUtil.selfLinkToApi(request, "resources/loggedIn");
    }

    /**
     * Expecting list of strings in form: ${DAM_ID};${RESOURCE_ID}/views/${VIEW_ID}/roles/${ROLE_ID}
     *
     * Example:
     *  1;test/views/test/roles/discovery
     *
     * @param realm
     * @param damIdResourcePairs
     * @return absolute URL pointing to DAM resource
     */
    private List<URI> getResourcesFrom(String realm, List<String> damIdResourcePairs) {
        return damIdResourcePairs.stream()
            .map((damIdResourcePair) -> {
                String[] pair = damIdResourcePair.split(";");
                URI damBaseUrl = dams.get(pair[0]).getBaseUrl();
                return damBaseUrl.resolve("/dam/" + realm + "/resources/" + pair[1]);
            })
            .collect(toList());
    }

    private URI cartCheckoutUrl(ServerHttpRequest request, @PathVariable String realm, List<URI> resources) {
        final URI baseUri = UriUtil.selfLinkToApi(request, realm, "resources/checkout");
        return UriComponentsBuilder.fromUri(baseUri)
                                   .queryParam("resource", resources.toArray())
                                   .build()
                                   .toUri();
    }

    private ReactiveDamClient lookupDamClient(List<URI> resources) {
        if (resources.isEmpty()) {
            throw new IllegalArgumentException("Cannot look DAM from empty resource list");
        }
        final String testResourceUrl = resources.get(0).toString();
        return dams.entrySet()
                   .stream()
                   // FIXME make this more resilient
                   .filter(damEntry -> testResourceUrl.startsWith(damEntry.getValue()
                                                                          .getBaseUrl()
                                                                          .toString()))
                   .map(damEntry -> damClientFactory.getDamClient(damEntry.getKey()))
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException(format("Could not find DAM for resources [%s]", resources)));
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
    @GetMapping("/api/v1alpha/resources/loggedIn")
    public Mono<? extends ResponseEntity<?>> handleTokenRequest(ServerHttpRequest request,
                                                                @RequestParam String code,
                                                                @RequestParam("state") String stateParam) {
        final UserTokenCookiePackager.CookieName cookieName = DAM.cookieName(TokenKind.OAUTH_STATE);
        final OAuthStateHandler.ValidatedState validatedState = stateHandler.parseAndVerify(request, cookieName);
        final TokenExchangePurpose tokenExchangePurpose = validatedState.getTokenExchangePurpose();
        final List<URI> resources = validatedState.getResourceList()
                                                  .stream()
                                                  .flatMap(List::stream)
                                                  .map(URI::create)
                                                  .collect(toList());
        final ReactiveDamOAuthClient oAuthClient = damClientFactory.lookupDamOAuthClient(resources);
        final URI redirectUri = getRedirectUri(request);
        final String realm = validatedState.getRealm();
        return oAuthClient.exchangeAuthorizationCodeForTokens(realm, redirectUri, code)
                          .flatMap(tokenResponse -> {
                              Optional<URI> customDestination = validatedState.getDestinationAfterLogin()
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
