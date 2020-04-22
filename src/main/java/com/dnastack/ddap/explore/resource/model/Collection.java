package com.dnastack.ddap.explore.resource.model;

import java.net.URI;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "newBuilder")
public class Collection {

    private String id;
    private String name;
    private String description;
    private URI imageUrl;
    private Map<String,String> metadata;

}
