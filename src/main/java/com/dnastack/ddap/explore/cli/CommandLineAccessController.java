package com.dnastack.ddap.explore.cli;

import com.dnastack.ddap.common.security.*;
import com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieKind;
import com.dnastack.ddap.common.util.http.UriUtil;
import com.dnastack.ddap.ic.cli.model.CliLoginStatus;
import com.dnastack.ddap.ic.cli.model.TokenResponse;
import com.dnastack.ddap.ic.oauth.client.ReactiveIcOAuthClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Slf4j
@RestController
public class CommandLineAccessController {
    private static final String DEFAULT_SCOPES = "openid ga4gh_passport_v1 account_admin identities";

    private final ReactiveIcOAuthClient oAuthClient;
    private final OAuthStateHandler stateHandler;
    private Duration tokenTtl;
    private final Resource cliZip;
    private final UserTokenCookiePackager userTokenCookiePackager;
    private final JwtHandler jwtHandler;

    /*
     * IMPORTANT
     * If we intend to support CLI login in the future, this state must migrate to a data-store (BigTable, Redis, etc.).
     * This is a quick hack to support this feature in phase 1.
     */
    private final ConcurrentMap<String, LoginStatus> loginStatusByCliSessionId = new ConcurrentHashMap<>();

    @Autowired
    public CommandLineAccessController(OAuthStateHandler stateHandler,
                                       ReactiveIcOAuthClient oAuthClient,
                                       UserTokenCookiePackager userTokenCookiePackager,
                                       @Value("${ddap.command-line-service.aud}") String tokenAudience,
                                       @Value("${ddap.command-line-service.ttl}") Duration tokenTtl,
                                       @Value("${ddap.command-line-service.signingKey}") String tokenSigningKeyBase64,
                                       @Value("classpath:/static/ddap-cli.zip") Resource cliZip) {
        this.oAuthClient = oAuthClient;
        this.stateHandler = stateHandler;
        this.tokenTtl = tokenTtl;
        this.cliZip = cliZip;
        this.jwtHandler = new JwtHandler(tokenAudience,
                                         tokenTtl,
                                         Keys.hmacShaKeyFor(Base64.getMimeDecoder().decode(tokenSigningKeyBase64)));
        this.userTokenCookiePackager = userTokenCookiePackager;
    }

    @GetMapping(produces = "application/zip", path = "/api/v1alpha/cli/download")
    public Resource downloadCliZip() {
        return cliZip;
    }

    @GetMapping("/api/v1alpha/realm/{realm}/cli/login/{cliSessionId}")
    public Mono<? extends ResponseEntity<?>> initiateBrowserLogin(ServerHttpRequest request,
                                                             @PathVariable String realm,
                                                             @PathVariable String cliSessionId) {
        final LoginStatus loginStatus = loginStatusByCliSessionId.computeIfAbsent(cliSessionId, k -> {
            throw new CliSessionNotFoundException(cliSessionId);
        });
        final String state = stateHandler.generateCommandLineLoginState(cliSessionId, realm);
        final URI redirectUri = UriUtil.selfLinkToApi(request, "cli/loggedIn");
        return getActiveLoginUri(realm, loginStatus.getScope(), state, redirectUri)
                .map(webLoginUrl -> ResponseEntity.status(HttpStatus.FOUND)
                                                  .location(webLoginUrl)
                                                  .header(SET_COOKIE, userTokenCookiePackager.packageToken(state, CookieKind.OAUTH_STATE).toString())
                                                  .build());
    }

    @PostMapping("/api/v1alpha/realm/{realm}/cli/login")
    public ResponseEntity<?> commandLineLogin(ServerHttpRequest request,
                                                              @PathVariable String realm,
                                                              @RequestParam(defaultValue = DEFAULT_SCOPES) String scope) {
        final String cliSessionId = UUID.randomUUID().toString();
        loginStatusByCliSessionId.compute(cliSessionId, (id, monitor) -> {
            if (monitor == null) {
                return new LoginStatus(Instant.now().plus(tokenTtl), scope, null);
            } else {
                // TODO DISCO-2353 retry regenerating a couple times.
                throw new RuntimeException("Failed to generate unique CLI session id.");
            }
        });

        final URI statusUrl = UriUtil.selfLinkToApi(request, format("cli/login/%s/status", cliSessionId));
        final URI loginUri = UriUtil.selfLinkToApi(request, realm, format("cli/login/%s", cliSessionId));

        final String bearerToken = jwtHandler.createBuilder(JwtHandler.TokenKind.BEARER)
                                             .setSubject(cliSessionId)
                                             .claim("tokenKind", "bearer")
                                             .claim("webLoginUrl", loginUri)
                                             .compact();

        return ResponseEntity.status(200)
                             .location(statusUrl)
                             .body(new StartLoginResponse(bearerToken));
    }

    public Mono<URI> getActiveLoginUri(String realm, String scope, String state, URI redirectUri) {
        final URI webLoginUrl = oAuthClient.getAuthorizeUrl(realm,
                                                            state,
                                                            scope,
                                                            redirectUri);
        return oAuthClient.testAuthorizeEndpoint(webLoginUrl)
                                         .map(status -> {
                                             if (status.is4xxClientError()) {
                                                 // For now try the legacy login URI on client error
                                                 log.info("Authorize endpoint returned [{}] status: Falling back to legacy authorize endpoint", status);
                                                 return oAuthClient.getLegacyAuthorizeUrl(realm, state, scope, redirectUri);
                                             } else {
                                                 return webLoginUrl;
                                             }
                                         });
    }

    @GetMapping(path = "/api/v1alpha/cli/login/{cliSessionId}/status")
    public Mono<CliLoginStatus> loginStatus(@PathVariable("cliSessionId") String cliSessionId,
                                            @CookieValue("status_token") String token) {
        final Optional<String> oBearerToken = Optional.ofNullable(token);
        if (oBearerToken.isPresent()) {
            final String jwt = oBearerToken.get();
            final Jws<Claims> jws = jwtHandler.createParser(JwtHandler.TokenKind.BEARER)
                                              .requireSubject(cliSessionId)
                                              .parseClaimsJws(jwt);
            final URI webLoginUrl = URI.create(jws.getBody().get("webLoginUrl", String.class));

            final LoginStatus loginStatus = loginStatusByCliSessionId.get(cliSessionId);
            final TokenResponse tokenResponse = loginStatus.getTokenResponse();
            return Mono.just(new CliLoginStatus(tokenResponse, webLoginUrl));
        } else {
            return Mono.error(new BadCredentialsException(
                    "Authorization header was set but did not contain bearer token."));
        }
    }

    @GetMapping(path = "/api/v1alpha/cli/loggedIn")
    public Mono<? extends ResponseEntity<?>> finishCliLogin(ServerHttpRequest request,
                                                            @RequestParam("state") String state,
                                                            @CookieValue("oauth_state") String stateFromCookie,
                                                            @RequestParam("code") String code) {
        final OAuthStateHandler.ValidatedState validatedState = stateHandler.parseAndVerify(state, stateFromCookie);
        final String cliSessionId = validatedState.getCliSession()
                                                  .orElseThrow(() -> new InvalidOAuthStateException("State does not contain a login session ID", state));
        final String realm = validatedState.getRealm();
        final LoginStatus loginStatus = loginStatusByCliSessionId.get(cliSessionId);
        if (loginStatus == null) {
            return Mono.error(new CliSessionNotFoundException(cliSessionId));
        } else {
            return oAuthClient.exchangeAuthorizationCodeForTokens(realm,
                                                                  UriUtil.selfLinkToApi(request,
                                                                                        "cli/loggedIn"),
                                                                  code)
                           .doOnSuccess(tokenResponse -> {
                               loginStatusByCliSessionId.compute(cliSessionId, (id, oldStatus) -> {
                                   if (oldStatus == null) {
                                       throw new CliSessionNotFoundException(cliSessionId);
                                   } else {
                                       return new LoginStatus(oldStatus.getExpiry(),
                                                              oldStatus.getScope(),
                                                              new TokenResponse(
                                                                      userTokenCookiePackager.encodeToken(tokenResponse.getIdToken()),
                                                                      userTokenCookiePackager.encodeToken(tokenResponse.getAccessToken())
                                                              ));
                                   }
                               });
                           })
                           .thenReturn(ResponseEntity.ok().build());
        }
    }

    @Scheduled(fixedRate = 5*60*1000)
    public void cleanupCliSessions() {
        log.info("Starting cleanup of CLI sessions");
        final Instant start = Instant.now();
        final List<String> expiredSessionIds = loginStatusByCliSessionId.entrySet()
                                                                        .stream()
                                                                        .filter(e -> e.getValue()
                                                                                      .getExpiry()
                                                                                      .isBefore(start))
                                                                        .map(Map.Entry::getKey)
                                                                        .collect(Collectors.toList());

        expiredSessionIds.forEach(loginStatusByCliSessionId::remove);
        final Instant end = Instant.now();
        final Duration duration = Duration.between(start, end);
        log.info("Removed {} CLI sessions in {}ms",
                 expiredSessionIds.size(),
                 TimeUnit.NANOSECONDS.toMillis(duration.get(ChronoUnit.NANOS)));
    }

    @lombok.Value
    private static class LoginStatus {
        private Instant expiry;
        private String scope;
        private TokenResponse tokenResponse;
    }

    @lombok.Value
    @AllArgsConstructor
    static class StartLoginResponse {
        private String token;
    }
}
