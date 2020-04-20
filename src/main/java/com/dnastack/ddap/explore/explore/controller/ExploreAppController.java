package com.dnastack.ddap.explore.explore.controller;

import com.dnastack.ddap.common.util.http.XForwardUtil;
import com.dnastack.ddap.explore.explore.model.ExploreTokenResponse;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import com.dnastack.ddap.explore.resource.service.UserCredentialService;
import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1beta/{realm}/apps/explore")
public class ExploreAppController {


    private final UserCredentialService userCredentialService;

    @Autowired
    public ExploreAppController(UserCredentialService userCredentialService) {
        this.userCredentialService = userCredentialService;
    }

    @GetMapping(value = "/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ExploreTokenResponse> getTokensForResources(ServerHttpRequest httpRequest, WebSession session, @PathVariable("realm") String realm,
        @RequestParam("resource") List<String> authorizationIds,
        @RequestParam(value = "minimum_ttl", defaultValue = "10m") String minimumTtl) {

        return Mono.defer(() -> {
            final Duration enforceTtl = parseDuration(minimumTtl);
            final ExploreTokenResponse tokenResponse = new ExploreTokenResponse();
            List<UserCredential> existingCredentials = userCredentialService
                .getAndDecryptSessionBoundTokens(httpRequest, session, authorizationIds);
            List<String> reauthenticate = new ArrayList<>();
            authorizationIds.forEach(id -> {
                Optional<UserCredential> existingOpt = existingCredentials.stream()
                    .filter(credenential -> credenential.getAuthorizationId().equals(id)).findFirst();
                existingOpt.ifPresentOrElse(existing -> {
                    if (existing.getExpirationTime().isBefore(ZonedDateTime.now().plus(enforceTtl))) {
                        reauthenticate.add(id);
                    }
                }, () -> reauthenticate.add(id));
            });

            if (!reauthenticate.isEmpty()) {
                URI authorizeUriBase = URI.create(XForwardUtil
                    .getExternalPath(httpRequest, String.format("/api/v1beta/%s/resources/authorize", realm)));
                UriComponentsBuilder componentsBuilder = UriComponentsBuilder.fromUri(authorizeUriBase);
                for (var id : reauthenticate) {
                    componentsBuilder.queryParam("resource", id);
                }

                tokenResponse.setRequiresAdditionalAuth(true);
                tokenResponse.setAuthorizationUrlBase(componentsBuilder.build().toUri());
            } else {
                Map<String, String> tokens = new HashMap<>();
                existingCredentials
                    .forEach(credential -> tokens.put(credential.getAuthorizationId(), credential.getToken()));
                tokenResponse.setTokens(tokens);
            }

            return Mono.just(tokenResponse);
        });
    }

    private Duration parseDuration(String period) {
        try {
            if (!period.startsWith("PT")) {
                period = "PT" + period;

            }
            return Duration.parse(period);
        } catch (Exception e) {
            return Duration.ofHours(1);
        }
    }

}
