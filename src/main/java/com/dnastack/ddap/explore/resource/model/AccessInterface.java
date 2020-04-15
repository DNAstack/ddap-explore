package com.dnastack.ddap.explore.resource.model;

import java.net.URI;
import lombok.Data;

@Data
public class AccessInterface {

    private String type;
    private URI uri;
    private String authorizationId;
}
