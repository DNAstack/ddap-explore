package com.dnastack.ddap.ic.oauth.client;

import com.dnastack.ddap.common.oauth.ReactiveOAuthClient;
import com.dnastack.ddap.ic.common.config.IdpProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.net.URI;

@Slf4j
@Component
public class ReactiveIcOAuthClient extends ReactiveOAuthClient {

    public ReactiveIcOAuthClient(IdpProperties idpProperties) {
        super(new AuthServerInfo(idpProperties.getClientId(), idpProperties.getClientSecret(), new IcEndpointResolver(idpProperties.getBaseUrl())));
    }

    public URI getAuthorizeUrl(String realm, String state, String scopes, URI redirectUri, String loginHint) {
        return getAuthorizedUriBuilder(realm, state, scopes, redirectUri)
                .queryParam("login_hint", loginHint)
                .build();
    }

    @AllArgsConstructor
    public static class IcEndpointResolver implements OAuthEndpointResolver {
        private final URI baseUrl;

        @Override
        public URI getAuthorizeEndpoint(String realm) {
            return baseUrl.resolve(new UriTemplate("/identity/v1alpha/{realm}/authorize").expand(realm));
        }

        @Override
        public URI getTokenEndpoint(String realm) {
            return baseUrl.resolve(new UriTemplate("/identity/v1alpha/{realm}/token").expand(realm));
        }

        @Override
        public URI getRevokeEndpoint(String realm) {
            return baseUrl.resolve(new UriTemplate("/identity/v1alpha/{realm}/revoke").expand(realm));
        }
    }
}
