package com.dnastack.ddap.explore.apps.search.model;

import com.dnastack.ddap.explore.resource.model.Resource;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TableInfo implements Comparable<TableInfo> {

    private boolean requiresAdditionalAuth = false;
    private Resource resource;
    private URI authorizationUrlBase;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("data_model")
    private Map<String, Object> dataModel;

    @Override
    public int compareTo(TableInfo o) {
        return this.name.compareTo(o.name);
    }

}
