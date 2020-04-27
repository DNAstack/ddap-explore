package com.dnastack.ddap.explore.apps.wes.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowExecutionRunRequestModel {

    private String wdl;
    private Map<String, JsonNode> inputsJson;
    private Map<String, String> tokensJson;

}
