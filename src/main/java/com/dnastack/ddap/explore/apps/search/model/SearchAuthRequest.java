package com.dnastack.ddap.explore.apps.search.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
public class SearchAuthRequest {
    String key;
    String resourceType;
    Map<String, String> resourceDescription;
}