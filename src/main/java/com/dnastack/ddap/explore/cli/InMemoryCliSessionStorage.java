package com.dnastack.ddap.explore.cli;

import com.dnastack.ddap.explore.resource.model.UserCredential;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpCookie;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * IMPORTANT
 * If we intend to support CLI login in the future, this state must migrate to a data-store (BigTable, Redis, etc.).
 * This is a quick hack to support this feature in phase 1.
 */
public class InMemoryCliSessionStorage {

    public static final ConcurrentMap<String, AuthorizeStatus> cartCookies = new ConcurrentHashMap<>();

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class AuthorizeStatus {
        private String resource;
        private Instant expiresIn;
        private UserCredential userCredential;
    }

}
