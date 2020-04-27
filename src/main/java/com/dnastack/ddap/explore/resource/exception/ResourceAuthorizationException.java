package com.dnastack.ddap.explore.resource.exception;

import com.dnastack.ddap.explore.resource.model.Id;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
public class ResourceAuthorizationException extends RuntimeException {

    private ResourceAuthorizationExceptionResponse response;
    private HttpStatus status;

    public ResourceAuthorizationException(String reason, List<String> resources, HttpStatus status) {
        super();
        this.status = status;
        response = new ResourceAuthorizationExceptionResponse(status.value(), reason, resources);
    }

    public <T extends Id> ResourceAuthorizationException(String reason,HttpStatus status,  List<T> resources) {
        super();
        this.status = status;
        response = new ResourceAuthorizationExceptionResponse(status.value(), reason, resources.stream().map(Id::encodeId).collect(Collectors.toList()));
    }

    @Value
    public static class ResourceAuthorizationExceptionResponse {

        int status;
        String reason;
        List<String> resources;
    }
}
