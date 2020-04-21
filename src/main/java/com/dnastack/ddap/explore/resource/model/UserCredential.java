package com.dnastack.ddap.explore.resource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.Data;

@Data
public class UserCredential {

    @JsonIgnore
    private String principalId;
    @JsonIgnore
    private String authorizationId;
    private ZonedDateTime creationTime;
    private ZonedDateTime expirationTime;

    @JsonIgnore
    private String encryptedCredentials;

    private Map<String, String> credentials;

}
