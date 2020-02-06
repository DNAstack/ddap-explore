package com.dnastack.ddap.explore.config.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ui")
public class AppConfig {
    private String title;
    private String defaultModule;
    private boolean inStandaloneMode;
    private boolean authorizationOnInitRequired;
    private boolean sidebarEnabled;
    private boolean featureAdministrationEnabled;
    private boolean featureExploreDataEnabled;
    private boolean featureWorkflowsEnabled;
}
