package com.dnastack.ddap.explore.apps.discovery.model;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class DiscoveryBeaconApiAlleleRequest {

    @NotBlank
    @Pattern(regexp = "[1-9]{1}|1[0-9]{1}|2[0-2]{1}|X{1}|Y{1}|MT{1}")
    private String referenceName;
    @NotBlank
    @Pattern(regexp = "[ACGT]*")
    private String referenceBases;
    @Pattern(regexp = "N|[ACGT]*")
    private String alternateBases;
    @Pattern(regexp = "DEL|INS|DUP|INV|CNV|DUP:TANDEM|DEL:ME|INS:ME")
    private String variantType;
    private String assemblyId;
    @PositiveOrZero
    private Long start;
    @PositiveOrZero
    private Long end;
    @PositiveOrZero
    private Long startMin;
    @PositiveOrZero
    private Long startMax;
    @PositiveOrZero
    private Long endMin;
    @PositiveOrZero
    private Long endMax;
    private DiscoveryBeaconApiIncludeDatasetResponseType includeDatasetResponses;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> datasetIds;

}
