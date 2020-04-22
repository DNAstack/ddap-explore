package com.dnastack.ddap.explore.resource.model;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessInterface {

    private String type;
    private URI uri;
    private String authorizationId;
}
