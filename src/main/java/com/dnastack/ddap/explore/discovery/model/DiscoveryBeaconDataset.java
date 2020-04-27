package com.dnastack.ddap.explore.discovery.model;

import java.time.ZonedDateTime;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

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
    private ZonedDateTime createDateTime;
    private String version;
    private Long variantCount;
    private Long callCount;
    private Long sampleCount;
    private String externalUrl;
    private Map<String, String> info;

}
