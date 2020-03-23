package com.dnastack.ddap.explore.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResourceResponseModel {

    private String resourcePath;
    private String resourceName;
    private String viewName;
    private Optional<String> roleName;
    private Optional<String> interfaceName;
    private Optional<Boolean> isSearchView;
    private Map<String, String> ui;

}
