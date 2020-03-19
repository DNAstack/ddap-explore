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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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

    public ReactiveDamOAuthClient lookupSingleOAuthClient(List<URI> resources) {
        final Map<ReactiveDamOAuthClient, List<URI>> resourcesByClient = lookupDamOAuthClient(resources);
        if (resourcesByClient.keySet().size() != 1) {
            throw new IllegalArgumentException(format("Expected exactly 1 client but found %d for resources [%s]", resourcesByClient.keySet().size(), resources));
        } else {
            return resourcesByClient.keySet().iterator().next();
        }
    }

    public Map<ReactiveDamOAuthClient, List<URI>> lookupDamOAuthClient(List<URI> resources) {
        if (resources.isEmpty()) {
            log.warn("The DAM OAuth client has been requested with no resources specified");
        }

        if (damClientsConfig.getDamFacadeInUse()) {
            // TODO check if (testResourceUrl.startsWith(the base url of DDAP))
            return Map.of(new ReactiveDamOAuthFacadeClient(damFacadeConfig), resources);
        }

        final Map<ReactiveDamOAuthClient, List<URI>> retVal = dams.values()
                                                                  .stream()
                                                                  .map(dam -> entry(new HttpReactiveDamOAuthClient(dam),
                                                                                    resources.stream()
                                                                                             .filter(testResourceUrl -> testResourceUrl.toString()
                                                                                                                                       .startsWith(dam.getBaseUrl().toString()))
                                                                                             .collect(toList())))
                                                                  .filter(e -> !e.getValue().isEmpty())
                                                                  .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        final long matchedUrls = retVal.values()
                                       .stream()
                                       .mapToLong(Collection::size)
                                       .sum();

        if (matchedUrls < resources.size()) {
            final List<URI> unmatched = resources.stream()
                                                 .filter(url -> retVal.values()
                                                                      .stream()
                                                                      .noneMatch(l -> l.contains(url)))
                                                 .collect(toList());

            throw new IllegalArgumentException(format("Could not find DAM for resources %s", unmatched));
        } else {
            return retVal;
        }
    }
}
