package com.dnastack.ddap.explore.common.session;

import java.util.List;
import lombok.Getter;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.InMemoryWebSessionStore;
import org.springframework.web.server.session.WebSessionIdResolver;
import org.springframework.web.server.session.WebSessionManager;
import org.springframework.web.server.session.WebSessionStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class EagerWebSessionManager implements WebSessionManager {

    @Getter
    private WebSessionIdResolver sessionIdResolver = new CookieWebSessionIdResolver();

    @Getter
    private WebSessionStore sessionStore = new InMemoryWebSessionStore();


    /**
     * Configure the id resolution strategy.
     * <p>By default an instance of {@link CookieWebSessionIdResolver}.
     * @param sessionIdResolver the resolver to use
     */
    public void setSessionIdResolver(WebSessionIdResolver sessionIdResolver) {
        Assert.notNull(sessionIdResolver, "WebSessionIdResolver is required");
        this.sessionIdResolver = sessionIdResolver;
    }

    /**
     * Configure the persistence strategy.
     * <p>By default an instance of {@link InMemoryWebSessionStore}.
     * @param sessionStore the persistence strategy to use
     */
    public void setSessionStore(WebSessionStore sessionStore) {
        Assert.notNull(sessionStore, "WebSessionStore is required");
        this.sessionStore = sessionStore;
    }



    @Override
    public Mono<WebSession> getSession(ServerWebExchange exchange) {
        return Mono.defer(() -> retrieveSession(exchange)
            .switchIfEmpty(this.sessionStore.createWebSession())
            .doOnNext(session -> exchange.getResponse().beforeCommit(() -> save(exchange, session))));
    }

    private Mono<WebSession> retrieveSession(ServerWebExchange exchange) {
        return Flux.fromIterable(getSessionIdResolver().resolveSessionIds(exchange))
            .concatMap(this.sessionStore::retrieveSession)
            .next();
    }

    private Mono<Void> save(ServerWebExchange exchange, WebSession session) {
        List<String> ids = getSessionIdResolver().resolveSessionIds(exchange);

        if (session.isExpired()) {
            if (!ids.isEmpty()) {
                // Expired on retrieve or while processing request, or invalidated..
                this.sessionIdResolver.expireSession(exchange);
            }
            return Mono.empty();
        }

        if (!session.isStarted()){
            session.start();
        }

        if (session.getAttribute(SessionEncryptionUtils.SESSION_ENCRYPT_KEY_NAME) == null){
            SessionEncryptionUtils.setSessionEncryption(exchange,session);
        }

        if (ids.isEmpty() || !session.getId().equals(ids.get(0))) {
            this.sessionIdResolver.setSessionId(exchange, session.getId());
        }

        return session.save();
    }
}
