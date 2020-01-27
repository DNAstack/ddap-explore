package com.dnastack.ddap.explore.facade.service;

import com.dnastack.ddap.common.security.InvalidOAuthStateException;
import com.dnastack.ddap.common.security.JwtHandler;
import com.dnastack.ddap.common.security.OAuthStateHandler;
import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import static com.dnastack.ddap.common.security.JwtHandler.TokenKind.INTER_SERVICE;
import static java.lang.String.format;

@Service
public class FacadeJwtHandler {
    private final JwtHandler jwtHandler;

    @Autowired
    public FacadeJwtHandler(@Value("${dam-facade.jwt.aud}") String tokenAudience,
                            @Value("${dam-facade.jwt.ttl}") Duration tokenTtl,
                            @Value("${dam-facade.jwt.signing-key}") String tokenSigningKeyBase64) {
        var signingKey = Keys.hmacShaKeyFor(Base64.getMimeDecoder().decode(tokenSigningKeyBase64));
        this.jwtHandler = new JwtHandler(tokenAudience, tokenTtl, signingKey);
    }

    public String generate(Map<String, Object> claims) {
        return jwtHandler.createBuilder(INTER_SERVICE)
                .claim("purpose", "standalone-mode")
                .addClaims(claims)
                .compact();
    }
}
