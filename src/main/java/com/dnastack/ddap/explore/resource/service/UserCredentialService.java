package com.dnastack.ddap.explore.resource.service;

import com.dnastack.ddap.explore.resource.controller.ResourceController.Id;
import com.dnastack.ddap.explore.resource.data.UserCredentialDao;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import com.dnastack.ddap.explore.session.PersistantSession;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;

@Component
@Slf4j
public class UserCredentialService {

    @Autowired
    private Jdbi jdbi;


    public String getUserIdentifier(WebSession session) {
        String userIdentifier = session.getAttribute(PersistantSession.USER_IDENTIFIER_KEY);
        if (userIdentifier == null) {
            throw new IllegalArgumentException("User does not exist within a session");
        }
        return userIdentifier;
    }


    public Optional<UserCredential> getSessionBoundTokenForResource(WebSession session, Id resourceId) {
        return jdbi.withExtension(UserCredentialDao.class, dao -> dao
            .getCredentialForResource(getUserIdentifier(session), resourceId.encodeId()));
    }

    public List<UserCredential> getSessionBoundTokens(WebSession session) {
        return jdbi.withExtension(UserCredentialDao.class, dao -> dao
            .getUserCredentials(getUserIdentifier(session)));
    }

    public List<UserCredential> getSessionBountdokens(WebSession session, List<Id> resourceIds) {
        List<String> ids = resourceIds.stream().map(Id::encodeId).collect(Collectors.toList());
        return getSessionBoundTokens(session, ids);
    }

    public List<UserCredential> getSessionBoundTokens(WebSession session, List<String> ids) {
        return jdbi.withExtension(UserCredentialDao.class, dao -> dao
            .getCredentialsForResources(getUserIdentifier(session), ids));
    }


    public void storeSessionBoundTokenForResource(WebSession session, Id authorizationId, ZonedDateTime expires, String token) {
        String principal = getUserIdentifier(session);
        UserCredential credential = new UserCredential();
        credential.setCreationTime(ZonedDateTime.now());
        credential.setPrincipalId(principal);
        credential.setAuthorizationId(authorizationId.encodeId());
        credential.setExpirationTime(expires != null ? expires : ZonedDateTime.now().plusHours(1));
        credential.setToken(token);

        jdbi.useExtension(UserCredentialDao.class, dao -> {
            dao.getCredentialForResource(principal, credential.getAuthorizationId()).ifPresent(_cred -> {
                dao.deleteCredential(principal, credential.getAuthorizationId());
            });
            dao.createUserCredential(credential);
        });
    }

    @Scheduled(fixedDelay = 300000)
    public void deleteSessionBoundTokens() {
        jdbi.useExtension(UserCredentialDao.class, dao -> {
            log.info("Cleaning up expired or orphaned session resource tokens");
            int tokensDeleted = dao.deleteExpiredCredentials();
            log.info("Removed " + tokensDeleted + " expired or orphaned tokens");
        });

    }
}
