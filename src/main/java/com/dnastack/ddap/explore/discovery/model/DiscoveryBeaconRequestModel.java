package com.dnastack.ddap.explore.discovery.model;

import lombok.Data;

@Data
public class DiscoveryBeaconRequestModel {

    String assemblyId;
    String referenceName;
    String start;
    String referenceBases;
    String alternateBases;

}
