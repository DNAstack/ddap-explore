package com.dnastack.ddap.explore.resource.model;


import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "newBuilder")
public class Resource {

    private String id;
    private String collectionId;
    private String name;
    private String description;
    private URI imageUrl;
    private List<AccessInterface> interfaces;
    private Map<String, String> metadata;
}
