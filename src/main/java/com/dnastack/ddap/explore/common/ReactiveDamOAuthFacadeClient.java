package com.dnastack.ddap.explore.common;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.explore.common.config.DamFacadeConfig;
import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

public class ReactiveDamOAuthFacadeClient implements ReactiveDamOAuthClient {
    private final DamFacadeConfig damFacadeConfig;

    public ReactiveDamOAuthFacadeClient(DamFacadeConfig damFacadeConfig) {
        this.damFacadeConfig = damFacadeConfig;
    }

    @Override
    public URI getAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, List<URI> resources) {
        var builder = UriComponentsBuilder.fromUri(URI.create(damFacadeConfig.getOauth2Url() + "/oauth2/authorize"))
                .queryParam("response_type", "code")
                .queryParam("client_id", damFacadeConfig.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("resource", resources.toArray())
                .queryParam("state", state);

        if (scopes != null) {
            builder.queryParam("scope", scopes);
        }

        return builder.build().toUri();
    }

    @Override
    public URI getLegacyAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, List<URI> resources) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<TokenResponse> exchangeAuthorizationCodeForTokens(String realm, URI redirectUri, String code) {
        return Mono.just(TokenResponse.builder()
                .accessToken("fake access token")
                .idToken("anonymous")
                .refreshToken("unused")
                .build());
    }

    @Override
    public Mono<TokenResponse> legacyExchangeAuthorizationCodeForTokens(String realm, URI redirectUri, String code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<HttpStatus> testAuthorizeEndpoint(URI uri) {
        return WebClientFactory.getWebClient()
                .get()
                .uri(uri)
                .exchange()
                .map(ClientResponse::statusCode);
    }

    @Override
    public Mono<TokenResponse> refreshAccessToken(String realm, String refreshToken, String scope) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<TokenResponse> legacyRefreshAccessToken(String realm, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<ClientResponse> revokeRefreshToken(String realm, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<ClientResponse> legacyRevokeRefreshToken(String realm, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getAuthorizeUrl(String realm, String state, String scopes, URI redirectUri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getLegacyAuthorizeUrl(String realm, String state, String scopes, URI redirectUri) {
        throw new UnsupportedOperationException();
    }
}
