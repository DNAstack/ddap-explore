package com.dnastack.ddap.explore.beacon.model;

import lombok.Data;

import java.util.List;

@Data
public class BeaconApiAlleleResponse {

    private String beaconId;
    private String apiVersion;
    private Boolean exists;
    private BeaconApiAlleleRequest alleleRequest;
    private List<BeaconApiDatasetAlleleResponse> datasetAlleleResponses;
    private BeaconApiError error;

}
