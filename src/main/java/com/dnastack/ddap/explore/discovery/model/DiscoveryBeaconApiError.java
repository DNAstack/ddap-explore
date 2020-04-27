package com.dnastack.ddap.explore.discovery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DiscoveryBeaconApiError {

    private Integer errorCode;
    private String errorMessage;

}
