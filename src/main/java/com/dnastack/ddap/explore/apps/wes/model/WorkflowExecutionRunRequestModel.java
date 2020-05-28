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
    private Map<String, CredentialsModel> credentials;
    private Map<String, JsonNode> inputsJson;

    @Data
    public static class CredentialsModel {
        private String accessKeyId;
        private String accessToken;
        private String sessionToken;
    }

}
