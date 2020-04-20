package com.dnastack.ddap.explore.resource.model;

import com.dnastack.ddap.explore.resource.controller.ResourceController;
import java.io.Serializable;
import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.Data;

@Data
public class OauthState implements Serializable {

    private static final long serialVersionUID = 2882933228196739671L;
    private final String stateString;
    private final ZonedDateTime validUntil;
    private final Duration ttl;
    private final String realm;
    private final URI authUrl;
    private final URI destinationAfterLogin;
    private final List<DamId> resourceList;
    private final OauthState nextState;

    public String getSpiKey() {
        return resourceList != null ? resourceList.get(0).getSpiKey() : null;
    }

    public Optional<URI> getDestinationAfterLogin() {
        return Optional.of(destinationAfterLogin);
    }

    public Optional<OauthState> getNextState() {
        return Optional.ofNullable(nextState);
    }
}
