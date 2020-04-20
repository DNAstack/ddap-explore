package com.dnastack.ddap.explore.explore.model;

import java.net.URI;
import java.util.Map;
import lombok.Data;

@Data
public class ExploreTokenResponse {

    private boolean requiresAdditionalAuth = false;
    private URI authorizationUrlBase;
    private Map<String, String> tokens;

}
