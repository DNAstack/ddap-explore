package com.dnastack.ddap.explore.dam.client;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.common.config.DamsConfig;
import com.dnastack.ddap.explore.common.ReactiveDamOAuthFacadeClient;
import com.dnastack.ddap.explore.common.config.DamClientsConfig;
import com.dnastack.ddap.explore.common.config.DamFacadeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
@Component
public class DamClientFactory {

    private final Map<String, ReactiveDamClient> damClients;
    private final Map<String, ReactiveDamOAuthClient> damOAuthClients;
    private final Map<String, DamProperties> dams;
    private final DamClientsConfig damClientsConfig;
    private final DamFacadeConfig damFacadeConfig;

    @Autowired
    public DamClientFactory(DamsConfig damsConfig,
                            Map<String, ReactiveDamClient> damClients,
                            Map<String, ReactiveDamOAuthClient> damOAuthClients,
                            DamClientsConfig damClientsConfig, DamFacadeConfig damFacadeConfig) {
        this.damClients = damClients;
        this.damOAuthClients = damOAuthClients;
        this.dams = Map.copyOf(damsConfig.getStaticDamsConfig());
        this.damClientsConfig = damClientsConfig;
        this.damFacadeConfig = damFacadeConfig;
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

    public ReactiveDamOAuthClient lookupDamOAuthClient(List<URI> resources) {
        if (resources.isEmpty()) {
            throw new IllegalArgumentException("Cannot look DAM from empty resource list");
        }

        final String testResourceUrl = resources.get(0).toString();

        if (damClientsConfig.getDamFacadeInUse()) {
            // TODO check if (testResourceUrl.startsWith(the base url of DDAP))
            return new ReactiveDamOAuthFacadeClient(damFacadeConfig);
        }

        return dams.values().stream()
                // FIXME make this more resilient
                .filter(dam -> testResourceUrl.startsWith(dam.getBaseUrl().toString()))
                .map(HttpReactiveDamOAuthClient::new)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Could not find DAM for resource [%s]", resources)));
    }
}
