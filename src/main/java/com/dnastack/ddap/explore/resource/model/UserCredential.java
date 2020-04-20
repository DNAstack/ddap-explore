package com.dnastack.ddap.explore.resource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class UserCredential {

    @JsonIgnore
    private String principalId;
    private String authorizationId;
    private ZonedDateTime creationTime;
    private ZonedDateTime expirationTime;
    private String token;

}
