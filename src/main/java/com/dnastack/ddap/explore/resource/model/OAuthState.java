package com.dnastack.ddap.explore.resource.model;

import java.io.Serializable;
import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.Data;

@Data
public class OAuthState implements Serializable {

    private static final long serialVersionUID = 2882933228196739671L;
    private final String stateString;
    private final ZonedDateTime validUntil;
    private final Duration ttl;
    private final String realm;
    private final URI authUrl;
    private URI destinationAfterLogin;
    private final List<Id> resourceList;
    private OAuthState nextState;

    public OAuthState(String stateString, ZonedDateTime validUntil, String ttl, String realm, URI authUrl, URI destinationAfterLogin, List<Id> resourceList) {
        this(stateString, validUntil, parseDuration(ttl), realm, authUrl, destinationAfterLogin, resourceList);
    }

    public OAuthState(String stateString, ZonedDateTime validUntil, Duration ttl, String realm, URI authUrl, URI destinationAfterLogin, List<Id> resourceList) {
        this.stateString = stateString;
        this.validUntil = validUntil;
        this.ttl = ttl;
        this.realm = realm;
        this.authUrl =authUrl;
        this.destinationAfterLogin = destinationAfterLogin;
        this.resourceList =resourceList;
    }

    public String getSpiKey() {
        return resourceList != null ? resourceList.get(0).getSpiKey() : null;
    }

    public Optional<URI> getDestinationAfterLogin() {
        return Optional.of(destinationAfterLogin);
    }

    public Optional<OAuthState> getNextState() {
        return Optional.ofNullable(nextState);
    }

    private static Duration parseDuration(String period) {
        try {
            if (!period.startsWith("PT")) {
                period = "PT" + period;

            }
            return Duration.parse(period);
        } catch (Exception e) {
            return Duration.ofHours(1);
        }
    }
}
