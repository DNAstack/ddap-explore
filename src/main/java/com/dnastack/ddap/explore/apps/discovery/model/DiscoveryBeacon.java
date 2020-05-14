package com.dnastack.ddap.explore.apps.discovery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryBeacon implements Cloneable {

    @NotBlank
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String apiVersion;
    private String description;
    private String version;
    private String welcomeUrl;
    private String alternativeUrl;
    private String createDateTime;
    private String updateDateTime;
    @NotNull
    private DiscoveryBeaconOrganization organization;
    @NotEmpty
    private List<DiscoveryBeaconDataset> datasets;
    private List<DiscoveryBeaconApiAlleleRequest> sampleAlleleRequests;
    private Map<String, String> info;


    @Override
    public DiscoveryBeacon clone() {
        return new DiscoveryBeacon(id, name, apiVersion, description, version, welcomeUrl, alternativeUrl, createDateTime, updateDateTime, organization, datasets, sampleAlleleRequests, info);
    }
}
