package com.dnastack.ddap.explore.session;

import com.dnastack.ddap.explore.session.data.SessionDao;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.session.ReactiveSessionRepository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class ReactiveJdbiSessionRepository implements ReactiveSessionRepository<PersistantSession> {

    private final Jdbi jdbi;

    public ReactiveJdbiSessionRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Mono<PersistantSession> createSession() {
        return Mono.defer(() -> {
            PersistantSession newSession = new PersistantSession();
            jdbi.useExtension(SessionDao.class, dao -> dao.createSession(newSession));
            return Mono.just(newSession);
        }).publishOn(Schedulers.elastic());
    }

    @Override
    public Mono<Void> save(PersistantSession session) {
        return Mono.fromRunnable(() -> {
            if (session.isChanged()) {
                jdbi.useExtension(SessionDao.class, dao -> dao.updateSession(session));
                session.clearChanged();
            }
        }).publishOn(Schedulers.elastic()).then(Mono.empty());
    }

    @Override
    public Mono<PersistantSession> findById(String id) {
        return Mono.defer(() -> Mono
            .justOrEmpty(jdbi.withExtension(SessionDao.class, dao -> dao.getSession(id))))
            .filter(session -> !session.isExpired())
            .switchIfEmpty(deleteById(id).then(Mono.empty()))
            .publishOn(Schedulers.elastic());
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> jdbi.useExtension(SessionDao.class, dao -> dao.deleteSession(id)))
            .publishOn(Schedulers.elastic()).then(Mono.empty());
    }

    @Scheduled(fixedDelay = 300000)
    public void cleanupSessions(){
        jdbi.useExtension(SessionDao.class,dao -> {
            log.info("Cleaning up expired sessions");
            int sessionsDeleted = dao.deleteExpiredSessions();
            log.info("Removed " + sessionsDeleted + " expired sessions");
        });
    }
}
