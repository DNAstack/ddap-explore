package com.dnastack.ddap.explore.apps.discovery.model;

import java.util.List;
import lombok.Data;

@Data
public class DiscoveryBeaconApiAlleleResponse {

    private String beaconId;
    private String apiVersion;
    private Boolean exists;
    private DiscoveryBeaconApiAlleleRequest alleleRequest;
    private List<DiscoveryBeaconApiDatasetAlleleResponse> datasetAlleleResponses;
    private DiscoveryBeaconApiError error;

}
