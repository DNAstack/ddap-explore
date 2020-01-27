package com.dnastack.ddap.explore.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dam-facade")
public class DamFacadeConfig {
    private String resourceName;
    private String resourceDescription;
    private String viewName;
    private String viewDescription;
    private String wesServerUrl;
    private String baseUrl;
    private String oauth2Url;
    private String clientId;
    private String clientSecret;
}
