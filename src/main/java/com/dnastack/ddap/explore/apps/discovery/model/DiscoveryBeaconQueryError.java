package com.dnastack.ddap.explore.apps.discovery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DiscoveryBeaconQueryError {

    private Integer status;
    private String message;

}
