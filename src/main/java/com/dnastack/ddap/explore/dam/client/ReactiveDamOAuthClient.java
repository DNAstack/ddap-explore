package com.dnastack.ddap.explore.dam.client;

import com.dnastack.ddap.common.oauth.ReactiveOAuthClient;

import java.net.URI;
import java.util.List;

public interface ReactiveDamOAuthClient extends ReactiveOAuthClient {
    URI getAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, List<URI> resources, String loginHint);

    URI getLegacyAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, List<URI> resources, String loginHint);
}
