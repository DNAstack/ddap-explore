package com.dnastack.ddap.explore.apps.wes.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DrsObjectModel {

    private String id;
    private String name;
    @JsonProperty("access_methods")
    private List<AccessMethod> accessMethods;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccessMethod {

        private String type;
        @JsonProperty("access_url")
        private AccessUrl accessUrl;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccessUrl {

        private String url;

    }

}
