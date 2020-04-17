package com.dnastack.ddap.explore.session;

import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;

@Component
public class UserStateService {

    public String getUserIdentifier(WebSession session) {
        String userIdentifier = session.getAttribute(PersistantSession.USER_IDENTIFIER_KEY);
        if (userIdentifier == null){
            throw new IllegalArgumentException("User does not exist within a session");
        }
        return userIdentifier;
    }
}
