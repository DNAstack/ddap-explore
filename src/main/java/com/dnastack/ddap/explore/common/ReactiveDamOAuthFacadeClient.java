package com.dnastack.ddap.explore.common;

import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

public class ReactiveDamOAuthFacadeClient implements ReactiveDamOAuthClient {
    @Override
    public URI getAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, List<URI> resources) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getLegacyAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, List<URI> resources) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<TokenResponse> exchangeAuthorizationCodeForTokens(String realm, URI redirectUri, String code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<TokenResponse> legacyExchangeAuthorizationCodeForTokens(String realm, URI redirectUri, String code) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<HttpStatus> testAuthorizeEndpoint(URI uri) {
        throw new UnsupportedOperationException();
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
