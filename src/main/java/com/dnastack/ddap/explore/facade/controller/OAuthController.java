package com.dnastack.ddap.explore.facade.controller;

import com.dnastack.ddap.common.security.InapplicableAuthorizationFlowException;
import com.dnastack.ddap.explore.common.config.DamClientsConfig;
import com.dnastack.ddap.explore.facade.service.FacadeJwtHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@Slf4j
@RestController
public class OAuthController {
    private final DamClientsConfig damClientsConfig;
    private final FacadeJwtHandler facadeJwtHandler;


    @Autowired
    public OAuthController(DamClientsConfig damClientsConfig, FacadeJwtHandler facadeJwtHandler) {
        this.damClientsConfig = damClientsConfig;
        this.facadeJwtHandler = facadeJwtHandler;
    }

    @GetMapping("/api/v1alpha/standalone-mode/oauth2/auth")
    public Mono<ResponseEntity> authorizeInStandaloneMode(@RequestParam("response_type") String responseType,
                                                          @RequestParam("client_id") String clientId,
                                                          @RequestParam("redirect_uri") URI redirectUri,
                                                          @RequestParam("state") String state,
                                                          @RequestParam("resource") String resource) {
        if (!damClientsConfig.getDamFacadeInUse()) {
            throw new InapplicableAuthorizationFlowException("Self-authorization is forbidden here");
        }

        String code = facadeJwtHandler.generate(Map.of("magic_word", "one ring rules them all"));

        return Mono.just(ResponseEntity.status((HttpStatus.TEMPORARY_REDIRECT))
                .location(redirectUri.resolve("loggedIn?code=" + code + "&state=" + state))
                .build());
    }
}
