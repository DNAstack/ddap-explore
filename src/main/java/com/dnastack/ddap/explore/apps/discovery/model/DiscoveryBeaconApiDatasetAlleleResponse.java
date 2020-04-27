package com.dnastack.ddap.explore.apps.discovery.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.NonNull;

@Data
public class DiscoveryBeaconApiDatasetAlleleResponse {

    private String datasetId;
    private Boolean exists;
    private Long variantCount;
    private Long callCount;
    private Long sampleCount;
    private Double frequency;
    private String note;
    private String externalUrl;
    private DiscoveryBeaconApiError error;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();


    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        if (additionalProperties == null) {
            additionalProperties = new TreeMap<>();
        }
        additionalProperties.put(key, value);
    }

    @JsonAnySetter
    public void setAdditionalProperties(@NonNull Map<String, Object> additionalProperties) {
        if (this.additionalProperties == null) {
            this.additionalProperties = additionalProperties;
        } else {
            this.additionalProperties.putAll(additionalProperties);
        }
    }


    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

}
