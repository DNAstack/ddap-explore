package com.dnastack.ddap.explore.wes.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
