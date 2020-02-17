package com.dnastack.ddap.explore.config.controller;

import com.dnastack.ddap.explore.config.model.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1alpha/config")
public class ConfigController {

    private final AppConfig appConfig;

    @Autowired
    public ConfigController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping
    public Mono<AppConfig> getConfig() {
        return Mono.just(appConfig);
    }
}
