package com.dnastack.ddap.explore.apps.dataset.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlatView {

    private String resourcePath;
    private String umbrella;
    private String resourceName;
    private String viewName;
    private String roleName;
    private String interfaceName;
    private String interfaceUri;
    private String contentType;
    private String version;
    private String topic;
    private String partition;
    private String fidelity;
    private String geoLocation;
    private String targetAdapter;
    private String platform;
    private String platformService;
    private String maxTokenTtl;
    private Map<String, String> resourceUi;
    private Map<String, String> viewUi;


}

