package com.dnastack.ddap.explore.apps.discovery.model;

import java.net.URI;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class DiscoveryBeaconQueryResult extends DiscoveryBeaconApiAlleleResponse {

    private DiscoveryBeaconQueryError queryError;
    private boolean requiresAdditionalAuth = false;
    private URI authorizationUrlBase;

}
