package com.dnastack.ddap.explore.apps.discovery.model;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@EqualsAndHashCode
@ToString
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class DiscoveryBeaconDataset {

    @NotBlank
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String assemblyId;
    private String createDateTime;
    private String version;
    private Long variantCount;
    private Long callCount;
    private Long sampleCount;
    private String externalUrl;
    private Map<String, String> info;

}
