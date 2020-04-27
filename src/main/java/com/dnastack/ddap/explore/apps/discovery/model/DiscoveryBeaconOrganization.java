package com.dnastack.ddap.explore.apps.discovery.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DiscoveryBeaconOrganization {

    @NotBlank
    private String id;
    @NotBlank
    private String name;
    private String description;
    private String address;
    private String welcomeUrl;
    private String contactUrl;
    private String logoUrl;
    private Map<String, String> info;

}
