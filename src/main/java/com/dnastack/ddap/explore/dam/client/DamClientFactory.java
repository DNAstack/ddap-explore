package com.dnastack.ddap.explore.dam.client;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.String.format;

@Component
public class DamClientFactory {

    private Map<String, ReactiveDamClient> damClients;
    private final Map<String, ReactiveDamOAuthClient> damOAuthClients;

    @Autowired
    public DamClientFactory(Map<String, ReactiveDamClient> damClients,
                            Map<String, ReactiveDamOAuthClient> damOAuthClients) {
        this.damClients = damClients;
        this.damOAuthClients = damOAuthClients;
    }

    public ReactiveDamClient getDamClient(String damId) {
        if (!damClients.containsKey(damId)) {
            throw new IllegalArgumentException(format("Unknown damId [%s]", damId));
        }

        return damClients.get(damId);
    }

    public ReactiveDamOAuthClient getDamOAuthClient(String damId) {
        if (!damOAuthClients.containsKey(damId)) {
            throw new IllegalArgumentException(format("Unknown damId [%s]", damId));
        }

        return damOAuthClients.get(damId);
    }
}
