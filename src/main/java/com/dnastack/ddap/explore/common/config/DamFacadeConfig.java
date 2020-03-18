package com.dnastack.ddap.explore.common.config;

import java.util.List;
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
    private String wesResourceId;
    private String baseUrl;
    private Oauth2 oauth2;


    @Data
    public static class Oauth2 {

        private String issuer;
        private String authorizationUrl;
        private String tokenUrl;
        private String userInfoUrl;
        private String defaultScope = null;
        private String clientId;
        private String clientSecret;
    }

}


