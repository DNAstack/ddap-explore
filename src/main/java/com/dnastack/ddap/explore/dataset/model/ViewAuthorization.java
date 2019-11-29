package com.dnastack.ddap.explore.dataset.model;

import dam.v1.DamService.ResourceTokens.ResourceToken;
import lombok.Data;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class ViewAuthorization {
    @Data
    public static class ViewAuthorizationResponse {

        private final String view;
        private final ResourceToken locationAndToken;

        public ResourceToken getLocationAndToken() {
            return locationAndToken;
        }

        private final ViewAuthorizationError exception;

        public ViewAuthorizationResponse(String view, ResourceToken locationAndToken) {
            this.view = view;
            this.locationAndToken = locationAndToken;
            this.exception = null;
        }

        public ViewAuthorizationResponse(String view, Throwable exception) {
            this.view = view;
            this.exception = ViewAuthorizationError.from(exception);
            this.locationAndToken = null;
        }
    }

    @Data
    public static class ViewAuthorizationError {

        private final String message;
        private final String exceptionClass;
        private final int statusCode;

        ViewAuthorizationError(Throwable throwable) {
            this(500, throwable);
        }

        ViewAuthorizationError(int status, Throwable throwable) {
            this.message = throwable.getMessage();
            this.exceptionClass = throwable.getClass().getName();
            this.statusCode = status;
        }

        static ViewAuthorizationError from(Throwable throwable) {
            if (throwable instanceof WebClientResponseException) {
                return new ViewAuthorizationError(((WebClientResponseException) throwable).getRawStatusCode(),
                        throwable);
            } else {
                return new ViewAuthorizationError(throwable);
            }
        }

    }
}
