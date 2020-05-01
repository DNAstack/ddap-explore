package com.dnastack.ddap.explore.apps.search.model;

import com.dnastack.ddap.explore.resource.model.Resource;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ListTables {

    @JsonIgnore
    private List<String> interfaceIdsForAdditionalAuth;


    private URI authorizationUrlBase;
    private boolean requiresAdditionalAuth = false;

    @JsonProperty("resource")
    private Resource resource;

    @JsonProperty("tables")
    private List<TableInfo> tableInfos;

    @JsonProperty("pagination")
    private Pagination pagination;

}
