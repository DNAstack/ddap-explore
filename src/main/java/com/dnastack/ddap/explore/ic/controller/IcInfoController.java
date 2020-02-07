package com.dnastack.ddap.explore.ic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1alpha/identity-concentrator/info")
public class IcInfoController {

    private final String icUiUrl;

    @Autowired
    public IcInfoController(@Value("${ic.ui-url}") String icUiUrl) {
        this.icUiUrl = icUiUrl;
    }

    @GetMapping
    public Mono<IcInfo> getIcInfo() {
        return Mono.just(new IcInfo(icUiUrl));
    }

}
