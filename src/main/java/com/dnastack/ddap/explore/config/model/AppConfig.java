package com.dnastack.ddap.explore.config.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "frontend-config")
public class AppConfig {

    @JsonInclude()
    private FrontendUiConfig ui;
    private String defaultRoute;
    @JsonInclude()
    private Set<FrontendApp> enabledApps;
    @JsonInclude()
    private Set<FrontendFeature> enabledFeatures;
    private boolean inStandaloneMode;
    private String googleAnalyticsId;
    private String tosUrl;
    private int listPageSize;
    @JsonInclude()
    private FrontendAppsConfig apps;

    @Data
    public static class FrontendAppsConfig {
        private FrontendAppSearchConfig search;
    }

    @Data
    public static class FrontendAppSearchConfig {
        private String defaultQuery;
    }

    @Data
    public static class FrontendAppWorkflowsConfig {
        private String trsBaseUrl;
        private List<String> trsAcceptedToolClasses;
        private List<String> trsAcceptedVersionDescriptorTypes;
    }

    @Data
    public static class FrontendUiConfig {
        private String title;
        private String logoUrl;
        private String theme;
    }

}
