package com.dnastack.ddap.explore.resource.service;

import com.dnastack.ddap.explore.common.session.PersistantSession;
import com.dnastack.ddap.explore.common.session.SessionEncryptionUtils;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.data.UserCredentialDao;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
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

    public List<UserCredential> getSessionBoundTokens(WebSession session, List<String> authorizationIds) {
        return jdbi.withExtension(UserCredentialDao.class, dao -> dao
            .getCredentialsForResources(getUserIdentifier(session), authorizationIds));
    }

    public List<UserCredential> getAndDecryptSessionBoundTokens(ServerHttpRequest request, WebSession session, List<String> authorizationIds) {
        return jdbi.withExtension(UserCredentialDao.class, dao -> dao
            .getCredentialsForResources(getUserIdentifier(session), authorizationIds))
            .stream().map(userCredential -> decryptSessionBoundToken(request, userCredential))
            .collect(Collectors.toList());
    }


    private UserCredential decryptSessionBoundToken(ServerHttpRequest request, UserCredential userCredential) {
        String privateKey = requirePrivateKeyInCookie(request);
        String decryptedToken = SessionEncryptionUtils.decryptData(privateKey, userCredential.getToken());
        userCredential.setToken(decryptedToken);
        return userCredential;
    }

    private String requirePrivateKeyInCookie(ServerHttpRequest request) {
        HttpCookie privateKey = request.getCookies().getFirst(SessionEncryptionUtils.COOKIE_NAME);
        if (privateKey == null || privateKey.getValue() == null) {
            throw new IllegalArgumentException(
                "Could not extract the Session Decryption from the Cookie: " + SessionEncryptionUtils.COOKIE_NAME
                    + ". Cookie does not exist");
        }
        return privateKey.getValue();
    }

    public void storeSessionBoundTokenForResource(WebSession session, Id authorizationId, ZonedDateTime expires, String token) {
        String encryptedToken = SessionEncryptionUtils.encryptData(session, token);
        String principal = getUserIdentifier(session);
        UserCredential credential = new UserCredential();
        credential.setCreationTime(ZonedDateTime.now());
        credential.setPrincipalId(principal);
        credential.setAuthorizationId(authorizationId.encodeId());
        credential.setExpirationTime(expires != null ? expires : ZonedDateTime.now().plusHours(1));
        credential.setToken(encryptedToken);

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
