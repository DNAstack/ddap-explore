package com.dnastack.ddap.explore.apps.discovery.model;

import lombok.Data;

import java.util.List;

@Data
public class DiscoveryBeaconRequestModel {

    private String assemblyId;
    private String referenceName;
    private String start;
    private String referenceBases;
    private String alternateBases;
    private List<String> datasetIds;

}
