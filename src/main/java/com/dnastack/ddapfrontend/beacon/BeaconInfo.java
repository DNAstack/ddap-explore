package com.dnastack.ddapfrontend.beacon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BeaconInfo {
    private String name;
    private String resourceLabel;
    private String resourceId;
    private String viewId;
}
