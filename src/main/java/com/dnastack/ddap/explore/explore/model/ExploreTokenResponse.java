package com.dnastack.ddap.explore.explore.model;

import com.dnastack.ddap.explore.resource.model.UserCredential;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ExploreTokenResponse {

    private boolean requiresAdditionalAuth = false;
    private URI authorizationUrlBase;
    private Map<String,UserCredential> access;

}
