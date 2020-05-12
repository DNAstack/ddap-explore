package com.dnastack.ddap.explore.apps.discovery.model;

import com.dnastack.ddap.explore.resource.model.Resource;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryBeacon implements Cloneable {

    private Resource resource;

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
    private ZonedDateTime createDateTime;
    private ZonedDateTime updateDateTime;
    @NotNull
    private DiscoveryBeaconOrganization organization;
    @NotEmpty
    private List<DiscoveryBeaconDataset> datasets;
    private List<DiscoveryBeaconApiAlleleRequest> sampleAlleleRequests;
    private Map<String, String> info;


    @Override
    public DiscoveryBeacon clone() {
        return new DiscoveryBeacon(resource,id, name, apiVersion, description, version, welcomeUrl, alternativeUrl, createDateTime, updateDateTime, organization, datasets, sampleAlleleRequests, info);
    }
}
