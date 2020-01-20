package com.dnastack.ddap.explore.security;

import com.dnastack.ddap.common.security.UserTokenCookiePackager;

import java.net.URI;
import java.util.Set;

@lombok.Value
public class CartTokenCookieName implements UserTokenCookiePackager.CookieName {
    private Set<URI> resources;

    @Override
    public String cookieName() {
        return "cart_" + resources.hashCode();
    }
}
