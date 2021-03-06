package com.dnastack.ddap.explore.apps.search.model;

import java.util.Map;
import lombok.Data;

@Data
public class TableError {

    private String source;
    private String message;
    private ErrorCode code;
    private Map<String,String> attributes;

    public enum ErrorCode {
        AUTH_CHALLENGE,TIMEOUT
    }
}
