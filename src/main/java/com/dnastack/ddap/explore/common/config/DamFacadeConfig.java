package com.dnastack.ddap.explore.common.config;

import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dam-facade")
public class DamFacadeConfig {

    @Deprecated private String resourceName;
    @Deprecated private String resourceDescription;
    @Deprecated private String viewName;
    @Deprecated private String viewDescription;
    @Deprecated private String wesServerUrl;
    @Deprecated private String wesResourceId;

    private String label;
    private String baseUrl;
    private Oauth2 oauth2;
    private Map<String, FacadeResource> resources; // alias -> resource

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

    @Data
    public static class FacadeResource {
        private String id; // formerly equivalent to wesResourceId
        private String name;
        private String description;
        private Map<String, FacadeView> views; // alias -> view
    }

    @Data
    public static class FacadeView {
        private String name;
        private String description;
        private String url;
        private String serviceTemplate;
        private String interfaceName;
        private List<String> permissions;
        private boolean controlledAccess;
    }

}


