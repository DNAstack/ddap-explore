package com.dnastack.ddap.explore.beacon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BeaconInfo {

    private String name;
    private String resourceLabel;
    private String damId;
    private String resourceId;
    private String viewId;
    private String resourcePath;

}
