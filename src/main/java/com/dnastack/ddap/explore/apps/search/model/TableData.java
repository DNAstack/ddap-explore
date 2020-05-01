package com.dnastack.ddap.explore.apps.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class TableData {

    private boolean requiresAdditionalAuth = false;
    private URI authorizationUrlBase;

    @JsonProperty("data_model")
    private Map<String, Object> dataModel;
    private List<Map<String, Object>> data;
    private Pagination pagination;


}
