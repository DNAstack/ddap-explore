package com.dnastack.ddap.explore.discovery.model;

import com.dnastack.ddap.explore.beacon.model.BeaconApiAlleleResponse;
import com.dnastack.ddap.explore.beacon.model.BeaconInfo;
import com.dnastack.ddap.explore.beacon.model.BeaconQueryError;
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
