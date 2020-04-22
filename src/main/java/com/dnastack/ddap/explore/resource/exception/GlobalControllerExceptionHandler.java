package com.dnastack.ddap.explore.resource.exception;

import com.dnastack.ddap.explore.resource.exception.ResourceAuthorizationException.ResourceAuthorizationExceptionResponse;
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

    @ExceptionHandler(ResourceIdEncodingException.class)
    public ResponseEntity<?> handleResourceAuthorziationException(ResourceIdEncodingException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }


}
