package com.dnastack.ddap.explore.session;

import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.session.web.server.session.SpringSessionWebSessionStore;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;
import org.springframework.web.server.session.WebSessionManager;

@Configuration
@EnableSpringWebSession
public class SessionConfig {


    @Bean
    public ReactiveSessionRepository<? extends Session> reactiveSessionRepository(Jdbi jdbi) {
        return new ReactiveJdbiSessionRepository(jdbi);
    }

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        return new CookieWebSessionIdResolver();
    }

    @Bean(WebHttpHandlerBuilder.WEB_SESSION_MANAGER_BEAN_NAME)
    public WebSessionManager eagerWebSessionManager(ReactiveSessionRepository<? extends Session> repository, WebSessionIdResolver idResolver) {
        SpringSessionWebSessionStore<? extends Session> sessionStore = new SpringSessionWebSessionStore<>(repository);
        EagerWebSessionManager sessionManager = new EagerWebSessionManager();
        sessionManager.setSessionStore(sessionStore);
        sessionManager.setSessionIdResolver(idResolver);
        return sessionManager;
    }
}
