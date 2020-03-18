package com.dnastack.ddap.explore.common;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.common.security.InvalidTokenException;
import com.dnastack.ddap.explore.common.config.DamFacadeConfig;
import com.dnastack.ddap.explore.common.config.DamFacadeConfig.Oauth2;
import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import com.dnastack.ddap.ic.oauth.client.TokenExchangeException;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
public class ReactiveDamOAuthFacadeClient implements ReactiveDamOAuthClient {

    private final DamFacadeConfig damFacadeConfig;
    private final Oauth2 oauth2Config;

    public ReactiveDamOAuthFacadeClient(DamFacadeConfig damFacadeConfig) {
        this.damFacadeConfig = damFacadeConfig;
        this.oauth2Config = damFacadeConfig.getOauth2();
    }

    @Override
    public URI getAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, List<URI> resources, String loginHint) {
        // authorization_endpoint
        var builder = UriComponentsBuilder.fromUri(URI.create(oauth2Config.getAuthorizationUrl()))
            .queryParam("response_type", "code")
            .queryParam("client_id", oauth2Config.getClientId())
            .queryParam("redirect_uri", redirectUri)
            .queryParam("resource", damFacadeConfig.getWesResourceId())
            .queryParam("nonce", state)
            .queryParam("state", state);

        if (scopes != null && !scopes.equals("")) {
            builder.queryParam("scope", scopes);
        } else if (oauth2Config.getDefaultScope() != null) {
            builder.queryParam("scope", oauth2Config.getDefaultScope());
        }
        if (loginHint != null) {
            builder.queryParam("login_hint", loginHint);
        }

        return builder.build().toUri();
    }

    @Override
    public Mono<TokenResponse> exchangeAuthorizationCodeForTokens(String realm, URI redirectUri, String code) {
        // NOTE: Based on BaseReactiveOauthClient
        return WebClientFactory.getWebClient()
            .post()
            .uri(oauth2Config.getTokenUrl())
            .header(AUTHORIZATION,
                "Basic " + encodeBasicAuth(oauth2Config.getClientId(), oauth2Config.getClientSecret()))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                //                        .with("redirect_uri", redirectUri.toString())
                .with("code", code))
            .exchange()
            .flatMap(this::extractIdpTokens)
            .onErrorMap(ex -> new InvalidTokenException(ex.getMessage()));
    }

    @Override
    public Mono<TokenResponse> refreshAccessToken(String realm, String refreshToken, String scope) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<ClientResponse> revokeRefreshToken(String realm, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, String loginHint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Object> getUserInfo(String realm, String accessToken) {
        throw new UnsupportedOperationException();
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
}
