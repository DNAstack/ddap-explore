package com.dnastack.ddap.ic.common.controller;

import com.dnastack.ddap.ic.common.config.IdpProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1alpha/icInfo")
public class IcInfoController {
    private IdpProperties idpProperties;

    @Autowired
    public IcInfoController(IdpProperties idpProperties) {
        this.idpProperties = idpProperties;
    }

    @GetMapping
    public Mono<IcInfo> getIcInfo() {
        String icUiUrl = idpProperties.getUiUrl().toString();
        return Mono.just(new IcInfo(icUiUrl));
    }
}
