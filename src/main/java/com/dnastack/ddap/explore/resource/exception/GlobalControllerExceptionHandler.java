package com.dnastack.ddap.explore.resource.exception;

import com.dnastack.ddap.explore.resource.exception.ResourceAuthorizationException.ResourceAuthorizationExceptionResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(ResourceAuthorizationException.class)
    public ResponseEntity<ResourceAuthorizationExceptionResponse> handleResourceAuthorziationException(ResourceAuthorizationException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getResponse());
    }

}
