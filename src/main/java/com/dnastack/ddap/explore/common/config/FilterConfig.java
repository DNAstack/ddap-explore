package com.dnastack.ddap.explore.common.config;

import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import com.dnastack.ddap.common.security.UserTokenCookiePackager.TokenKind;
import com.dnastack.ddap.common.security.filter.UserTokenStatusFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.dnastack.ddap.common.security.UserTokenCookiePackager.BasicServices.IC;

@Configuration
public class FilterConfig {
    @Bean
    public UserTokenStatusFilter userTokenStatusFilter(UserTokenCookiePackager userTokenCookiePackager) {
        return new UserTokenStatusFilter(userTokenCookiePackager, IC.cookieName(TokenKind.ACCESS));
    }
}
