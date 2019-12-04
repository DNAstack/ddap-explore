package com.dnastack.ddap.ic.oauth.client;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.common.security.InvalidTokenException;
import com.dnastack.ddap.ic.common.config.IdpProperties;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class ReactiveOAuthClient {

    private IdpProperties idpProperties;

    public ReactiveOAuthClient(IdpProperties idpProperties) {
        this.idpProperties = idpProperties;
    }

    public Mono<TokenResponse> personaLogin(String realm, String scopes, String personaName, URI redirectUri) {
        final UriTemplate template = new UriTemplate("/identity/v1alpha/{realm}/personas/{persona}" +
                "?client_id={clientId}" +
                "&client_secret={clientSecret}" +
                "&scope={scopes}" +
                "&redirect_uri={redirectUri}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("realm", realm);
        variables.put("persona", personaName);
        variables.put("scopes", scopes);
        variables.put("redirectUri", redirectUri);
        variables.put("clientId", idpProperties.getClientId());
        variables.put("clientSecret", idpProperties.getClientSecret());

        return WebClientFactory.getWebClient()
                .get()
                .uri(idpProperties.getBaseUrl().resolve(template.expand(variables)))
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is3xxRedirection()) {
                        final String location = clientResponse.headers().header("Location").get(0);
                        final String code = UriComponentsBuilder.fromHttpUrl(location)
                                .build()
                                .getQueryParams()
                                .getFirst("code");
                        return Mono.just(code);
                    } else {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new TokenExchangeException(format(
                                        "Unexpected result doing persona login: [%s] %s",
                                        clientResponse.statusCode(),
                                        body))));
                    }
                }).flatMap(code -> exchangeAuthorizationCodeForTokens(realm, redirectUri, code));
    }

    public Mono<TokenResponse> exchangeAuthorizationCodeForTokens(String realm, URI redirectUri, String code) {
        final UriTemplate template = new UriTemplate("/identity/v1alpha/{realm}/token" +
                "?grant_type=authorization_code" +
                "&code={code}" +
                "&redirect_uri={redirectUri}" +
                "&clientId={clientId}" +
                "&clientSecret={clientSecret}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("realm", realm);
        variables.put("code", code);
        variables.put("redirectUri", redirectUri);
        variables.put("clientId", idpProperties.getClientId());
        variables.put("clientSecret", idpProperties.getClientSecret());

        return WebClientFactory.getWebClient()
                .post()
                .uri(idpProperties.getBaseUrl().resolve(template.expand(variables)))
                .header(AUTHORIZATION, "Bearer " + code)
                .exchange()
                .flatMap(this::extractIdpTokens)
                .onErrorMap(ex ->  new InvalidTokenException(ex.getMessage()));
    }

    public Mono<TokenResponse> refreshAccessToken(String realm, String refreshToken) {
        final UriTemplate template = new UriTemplate("/identity/v1alpha/{realm}/token" +
                "?grant_type=refresh_token" +
                "&refresh_token={refreshToken}" +
                "&clientId={clientId}" +
                "&clientSecret={clientSecret}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("realm", realm);
        variables.put("refreshToken", refreshToken);
        variables.put("clientId", idpProperties.getClientId());
        variables.put("clientSecret", idpProperties.getClientSecret());

        return WebClientFactory.getWebClient()
                .post()
                .uri(idpProperties.getBaseUrl().resolve(template.expand(variables)))
                .exchange()
                .flatMap(this::extractIdpTokens)
                .onErrorMap(ex ->  new InvalidTokenException(ex.getMessage()));
    }

    public Mono<ClientResponse> revokeRefreshToken(String realm, String refreshToken) {
        final UriTemplate template = new UriTemplate("/identity/v1alpha/{realm}/revoke" +
                "?token={refreshToken}" +
                "&clientId={clientId}" +
                "&clientSecret={clientSecret}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("realm", realm);
        variables.put("refreshToken", refreshToken);
        variables.put("clientId", idpProperties.getClientId());
        variables.put("clientSecret", idpProperties.getClientSecret());

        return WebClientFactory.getWebClient()
                .post()
                .uri(idpProperties.getBaseUrl().resolve(template.expand(variables)))
                .exchange();
    }

    private Mono<TokenResponse> extractIdpTokens(ClientResponse idpTokenResponse) {
        if (idpTokenResponse.statusCode().is2xxSuccessful() && contentTypeIsApplicationJson(idpTokenResponse)) {
            return idpTokenResponse.bodyToMono(TokenResponse.class);
        } else {
            return idpTokenResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> Mono.error(new TokenExchangeException(errorBody)));
        }
    }

    private static boolean contentTypeIsApplicationJson(ClientResponse response) {
        return response.headers()
                .contentType()
                .filter(mediaType -> mediaType.isCompatibleWith(APPLICATION_JSON))
                .isPresent();
    }

    public URI getAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, String loginHint) {
        final UriTemplate template = new UriTemplate("/identity/v1alpha/{realm}/authorize" +
                "?response_type=code" +
                "&clientId={clientId}" +
                "&redirect_uri={redirectUri}" +
                "&state={state}" +
                "&scope={scopes}" +
                "&login_hint={loginHint}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("realm", realm);
        variables.put("state", state);
        variables.put("scopes", scopes);
        variables.put("redirectUri", redirectUri);
        variables.put("loginHint", loginHint);
        variables.put("clientId", idpProperties.getClientId());

        return idpProperties.getBaseUrl().resolve(template.expand(variables));
    }

}
