package com.dnastack.ddap.explore.resource.service;

import com.dnastack.ddap.explore.common.session.PersistantSession;
import com.dnastack.ddap.explore.common.session.SessionEncryptionUtils;
import com.dnastack.ddap.explore.resource.data.UserCredentialDao;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.Id.InterfaceId;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
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

    private final Jdbi jdbi;

    private final ObjectMapper mapper;


    @Autowired
    public UserCredentialService(Jdbi jdbi, ObjectMapper mapper) {
        this.mapper = mapper;
        this.jdbi = jdbi;
    }


    public String getUserIdentifier(WebSession session) {
        String userIdentifier = session.getAttribute(PersistantSession.USER_IDENTIFIER_KEY);
        if (userIdentifier == null) {
            throw new IllegalArgumentException("User does not exist within a session");
        }
        return userIdentifier;
    }

    public Optional<UserCredential> getSessionBoundCredentialsForResourceInterface(WebSession session, InterfaceId resourceId) {
        return jdbi.withExtension(UserCredentialDao.class, dao -> dao
            .getCredentialForResource(getUserIdentifier(session), resourceId.encodeId()));
    }

    public List<UserCredential> getSessionBoundCredentials(WebSession session, List<String> interfaceIds) {
        return jdbi.withExtension(UserCredentialDao.class, dao -> dao
            .getCredentialsForResources(getUserIdentifier(session), interfaceIds));
    }

    public List<UserCredential> getAndDecryptSessionBoundCredentials(ServerHttpRequest request, WebSession session, List<String> interfaceIds) {
        return jdbi.withExtension(UserCredentialDao.class, dao -> dao
            .getCredentialsForResources(getUserIdentifier(session), interfaceIds))
            .stream().map(userCredential -> decryptSessionBoundCredentials(request, userCredential))
            .collect(Collectors.toList());
    }


    private UserCredential decryptSessionBoundCredentials(ServerHttpRequest request, UserCredential userCredential) {
        String privateKey = requirePrivateKeyInCookie(request);
        String decryptedCredentials = SessionEncryptionUtils
            .decryptData(privateKey, userCredential.getEncryptedCredentials());
        userCredential.setCredentials(parseCredentialString(decryptedCredentials));
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

    public void storeSessionBoundCredentialsForResource(WebSession session, List<UserCredential> userCredentials) {
        String principal = getUserIdentifier(session);
        List<String> ids = userCredentials.stream().map(UserCredential::getInterfaceId)
            .collect(Collectors.toList());
        userCredentials.forEach(userCredential -> {
            userCredential.setPrincipalId(getUserIdentifier(session));
            String encryptedCredentials = SessionEncryptionUtils
                .encryptData(session, serializeCredentialMap(userCredential.getCredentials()));
            userCredential.setEncryptedCredentials(encryptedCredentials);
            userCredential.setCreationTime(ZonedDateTime.now());
            if (userCredential.getExpirationTime() == null) {
                userCredential.setExpirationTime(ZonedDateTime.now().plusHours(1));
            }
        });

        jdbi.useExtension(UserCredentialDao.class, dao -> {

            List<UserCredential> credentials = dao.getCredentialsForResources(principal, ids);
            if (credentials != null && !credentials.isEmpty()) {
                List<String> idsToDelete = credentials.stream().map(UserCredential::getInterfaceId)
                    .collect(Collectors.toList());
                dao.deleteCredentials(principal, idsToDelete);
            }
            dao.createUserCredentials(userCredentials);
        });
    }

    private Map<String, String> parseCredentialString(String credentialString) {
        try {
            TypeReference<Map<String, String>> typeReference = new TypeReference<>() {
            };
            return mapper.readValue(credentialString, typeReference);
        } catch (IOException e) {
            log.error(
                "Encountered an error while parsing credential string: " + e.getMessage() + ", returning empty Map", e);
            return Map.of();
        }
    }

    private String serializeCredentialMap(Map<String, String> credentialMap) {
        try {
            return mapper.writeValueAsString(credentialMap);
        } catch (IOException e) {
            log.error(
                "Encountered an error while parsing credential string: " + e.getMessage()
                    + ", returning empty representation", e);
            return "{}";
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void deleteSessionBoundCredentialss() {
        jdbi.useExtension(UserCredentialDao.class, dao -> {
            log.info("Cleaning up expired or orphaned session resource tokens");
            int tokensDeleted = dao.deleteExpiredCredentials();
            log.info("Removed " + tokensDeleted + " expired or orphaned tokens");
        });

    }


}
