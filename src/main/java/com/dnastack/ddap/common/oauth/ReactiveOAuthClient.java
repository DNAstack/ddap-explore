package com.dnastack.ddap.common.oauth;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.common.security.InvalidTokenException;
import com.dnastack.ddap.ic.oauth.client.TokenExchangeException;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AllArgsConstructor
public class ReactiveOAuthClient {
    private final AuthServerInfo authServerInfo;

    @Value
    public static class AuthServerInfo {
        private String clientId;
        private String clientSecret;
        private OAuthEndpointResolver resolver;
    }

    public interface OAuthEndpointResolver {
        URI getAuthorizeEndpoint(String realm);
        URI getTokenEndpoint(String realm);
        URI getRevokeEndpoint(String realm);
    }

    public Mono<TokenResponse> exchangeAuthorizationCodeForTokens(String realm, URI redirectUri, String code) {
        final UriTemplate template = new UriTemplate("{tokenEndpoint}" +
                                                             "?grant_type=authorization_code" +
                                                             "&code={code}" +
                                                             "&redirect_uri={redirectUri}" +
                                                             "&clientId={clientId}" +
                                                             "&clientSecret={clientSecret}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("tokenEndpoint", authServerInfo.getResolver().getTokenEndpoint(realm));
        variables.put("realm", realm);
        variables.put("code", code);
        variables.put("redirectUri", redirectUri);
        variables.put("clientId", authServerInfo.getClientId());
        variables.put("clientSecret", authServerInfo.getClientSecret());

        return WebClientFactory.getWebClient()
                               .post()
                               .uri(template.expand(variables))
                               .header(AUTHORIZATION, "Bearer " + code)
                               .exchange()
                               .flatMap(this::extractIdpTokens)
                               .onErrorMap(ex ->  new InvalidTokenException(ex.getMessage()));
    }

    public Mono<TokenResponse> refreshAccessToken(String realm, String refreshToken) {
        final UriTemplate template = new UriTemplate("{tokenEndpoint}" +
                                                             "?grant_type=refresh_token" +
                                                             "&refresh_token={refreshToken}" +
                                                             "&clientId={clientId}" +
                                                             "&clientSecret={clientSecret}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("tokenEndpoint", authServerInfo.getResolver().getTokenEndpoint(realm));
        variables.put("realm", realm);
        variables.put("refreshToken", refreshToken);
        variables.put("clientId", authServerInfo.getClientId());
        variables.put("clientSecret", authServerInfo.getClientSecret());

        return WebClientFactory.getWebClient()
                               .post()
                               .uri(template.expand(variables))
                               .exchange()
                               .flatMap(this::extractIdpTokens)
                               .onErrorMap(ex ->  new InvalidTokenException(ex.getMessage()));
    }

    public Mono<ClientResponse> revokeRefreshToken(String realm, String refreshToken) {
        final UriTemplate template = new UriTemplate("{revokeEndpoint}" +
                                                             "?token={refreshToken}" +
                                                             "&clientId={clientId}" +
                                                             "&clientSecret={clientSecret}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("revokeEndpoint", authServerInfo.getResolver().getRevokeEndpoint(realm));
        variables.put("realm", realm);
        variables.put("refreshToken", refreshToken);
        variables.put("clientId", authServerInfo.getClientId());
        variables.put("clientSecret", authServerInfo.getClientSecret());

        return WebClientFactory.getWebClient()
                               .post()
                               .uri(template.expand(variables))
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
        final UriTemplate template = new UriTemplate("{authorizeEndpoint}" +
                                                             "?response_type=code" +
                                                             "&clientId={clientId}" +
                                                             "&redirect_uri={redirectUri}" +
                                                             "&state={state}" +
                                                             "&scope={scopes}" +
                                                             "&login_hint={loginHint}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("authorizeEndpoint", authServerInfo.getResolver().getAuthorizeEndpoint(realm));
        variables.put("realm", realm);
        variables.put("state", state);
        variables.put("scopes", scopes);
        variables.put("redirectUri", redirectUri);
        variables.put("loginHint", loginHint);
        variables.put("clientId", authServerInfo.getClientId());

        return template.expand(variables);
    }
}
