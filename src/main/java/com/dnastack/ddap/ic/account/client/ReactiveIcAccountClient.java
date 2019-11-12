package com.dnastack.ddap.ic.account.client;

import com.dnastack.ddap.common.client.AuthAwareWebClientFactory;
import com.dnastack.ddap.common.client.OAuthFilter;
import com.dnastack.ddap.common.client.ProtobufDeserializer;
import com.dnastack.ddap.ic.common.config.IdpProperties;
import ic.v1.IcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieKind;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
public class ReactiveIcAccountClient {

    private IdpProperties idpProperties;
    private AuthAwareWebClientFactory webClientFactory;

    public ReactiveIcAccountClient(IdpProperties idpProperties, AuthAwareWebClientFactory webClientFactory) {
        this.idpProperties = idpProperties;
        this.webClientFactory = webClientFactory;
    }

    public Mono<IcService.AccountResponse> getAccounts(String realm, Map<CookieKind, String> tokens) {
        final UriTemplate template = new UriTemplate("/identity/v1alpha/{realm}/accounts/-" +
                "?client_id={clientId}" +
                "&client_secret={clientSecret}");
        final Map<String, Object> variables = new HashMap<>();
        variables.put("realm", realm);
        variables.put("clientId", idpProperties.getClientId());
        variables.put("clientSecret", idpProperties.getClientSecret());

        return webClientFactory.getWebClient(realm, tokens.get(CookieKind.REFRESH), OAuthFilter.Audience.IC)
                .get()
                .uri(idpProperties.getBaseUrl().resolve(template.expand(variables)))
                .header(AUTHORIZATION, "Bearer " + tokens.get(CookieKind.IC))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(json -> ProtobufDeserializer.fromJson(json, IcService.AccountResponse.getDefaultInstance()));
    }

}
