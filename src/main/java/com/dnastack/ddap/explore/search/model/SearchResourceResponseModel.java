package com.dnastack.ddap.explore.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResourceResponseModel {

    private String resourceUrl;
    private Map<String, String> ui;

}
