package com.dnastack.ddap.explore.common.session;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.session.Session;


/**
 * Session Implementation meant to be persisted
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PersistantSession implements Session {

    public static final String USER_IDENTIFIER_KEY = "_session_user_identifier";
    public static final int DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS = 3600;

    private String principalId;
    private String sessionId;
    private Instant lastAccessedTime;
    private Instant creationTime;
    private Duration maxInactiveInterval;
    private SessionAttributes attributes;

    private boolean changed = false;

    /**
     * Create a new Persistant session
     */
    public PersistantSession() {
        this.principalId = generateId();
        this.sessionId = generateId();
        this.attributes = new SessionAttributes();
        this.creationTime = Instant.now();
        this.lastAccessedTime = creationTime;
        this.maxInactiveInterval = Duration.ofSeconds(DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS);
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public String changeSessionId() {
        String changedId = generateId();
        setSessionId(changedId);
        this.changed = true;
        return changedId;
    }

    @Override
    public void setLastAccessedTime(Instant lastAccessedTime) {
        this.changed = true;
        this.lastAccessedTime = lastAccessedTime;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attributeName) {
        if (Objects.equals(attributeName,USER_IDENTIFIER_KEY)){
            return (T) principalId;
        }
        return (T) this.attributes.get(attributeName);
    }

    @Override
    public Set<String> getAttributeNames() {
        Set<String> attributeNames = new HashSet<>(this.attributes.keySet());
        attributeNames.add(USER_IDENTIFIER_KEY);
        return attributeNames;
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        if (Objects.equals(attributeName,USER_IDENTIFIER_KEY)){
            throw new IllegalArgumentException("Cannot set the user identifier");
        }
        if (attributeValue == null) {
            removeAttribute(attributeName);
        } else {
            Object currentAttribute = this.attributes.get(attributeName);
            if (!Objects.equals(currentAttribute, attributeValue)) {
                this.changed = true;
                this.attributes.put(attributeName, attributeValue);
            }
        }
    }

    @Override
    public void removeAttribute(String attributeName) {
        if (Objects.equals(attributeName,USER_IDENTIFIER_KEY)){
            throw new IllegalArgumentException("Cannot remove the user identifier");
        }
        this.changed = true;
        this.attributes.remove(attributeName);
    }

    @Override
    public boolean isExpired() {
        return isExpired(Instant.now());
    }

    boolean isExpired(Instant now) {
        if (this.maxInactiveInterval.isNegative()) {
            return false;
        }
        return now.minus(this.maxInactiveInterval).compareTo(this.lastAccessedTime) >= 0;
    }

    private static String generateId() {
        return UUID.randomUUID().toString();
    }

    void clearChanged() {
        changed = false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Session && this.sessionId.equals(((Session) obj).getId());
    }


    @Override
    public int hashCode() {
        return this.sessionId.hashCode();
    }

    public static class SessionAttributes extends HashMap<String,Object> implements Serializable {
        private static final long serialVersionUID = -4838006304397121256L;
    }
}
