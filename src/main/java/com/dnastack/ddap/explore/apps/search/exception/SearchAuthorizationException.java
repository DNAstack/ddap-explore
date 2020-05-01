package com.dnastack.ddap.explore.apps.search.exception;

import com.dnastack.ddap.explore.apps.search.model.SearchAuthRequest;
import com.dnastack.ddap.explore.resource.model.AccessInterface;
import lombok.Data;

@Data
public class SearchAuthorizationException extends RuntimeException {

    SearchAuthRequest searchAuthRequest;
    AccessInterface accessInterfaceToAuthorize;

    public SearchAuthorizationException(SearchAuthRequest searchAuthRequest) {
        super();
        this.searchAuthRequest = searchAuthRequest;
        this.accessInterfaceToAuthorize = null;
    }

    public SearchAuthorizationException(AccessInterface accessInterface) {
        super();
        this.accessInterfaceToAuthorize = accessInterface;
        this.searchAuthRequest = null;
    }
}
