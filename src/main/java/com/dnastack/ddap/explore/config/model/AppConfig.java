package com.dnastack.ddap.explore.config.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "ui")
public class AppConfig {

    private String title;
    private String logoUrl;
    private String googleAnalyticsId;
    private String theme;
    private String defaultModule;
    private String tosUrl;
    private boolean inStandaloneMode;
    private boolean authorizationOnInitRequired;
    private boolean featureRealmInputEnabled;
    private boolean featureAdministrationEnabled;
    private boolean featureTermsEnabled;
    private boolean featureExploreDataEnabled;
    private boolean featureDiscoveryEnabled;
    private boolean featureWorkflowsEnabled;
    private boolean featureSearchEnabled;
    private boolean featureBeaconsEnabled;
    private String trsBaseUrl;
    private List<String> trsAcceptedToolClasses;
    private List<String> trsAcceptedVersionDescriptorTypes;
    private int listPageSize;
    private String covidBeaconUrl;

    private SearchConfig search;

    @Data
    public static class SearchConfig {
        private String defaultQuery;
    }

}
