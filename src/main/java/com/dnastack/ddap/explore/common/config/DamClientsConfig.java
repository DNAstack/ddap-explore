package com.dnastack.ddap.explore.common.config;

import com.dnastack.ddap.common.client.AuthAwareWebClientFactory;
import com.dnastack.ddap.common.client.HttpReactiveDamClient;
import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.explore.common.ReactiveDamFacadeClient;
import com.dnastack.ddap.explore.common.ReactiveDamOAuthFacadeClient;
import com.dnastack.ddap.explore.dam.client.HttpReactiveDamOAuthClient;
import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class DamClientsConfig {

    private final AuthAwareWebClientFactory webClientFactory;
    private final Boolean damFacadeInUse;
    private final DamFacadeConfig damFacadeConfig;

    @Autowired
    public DamClientsConfig(AuthAwareWebClientFactory webClientFactory,
                            @Value("${ddap.dam-facade}") Boolean damFacadeInUse,
                            DamFacadeConfig damFacadeConfig) {
        this.webClientFactory = webClientFactory;
        this.damFacadeInUse = damFacadeInUse;
        this.damFacadeConfig = damFacadeConfig;
    }

    @Bean
    public Map<String, ReactiveDamClient> getDamClients(@Qualifier("dams") Map<String, DamProperties> dams)  {
        if (damFacadeInUse) {
            return Map.of("1", new ReactiveDamFacadeClient(damFacadeConfig));
        } else {
            return dams.entrySet().stream()
                       .map(damEntry -> {
                           DamProperties properties = damEntry.getValue();
                           return Map.of(damEntry.getKey(), new HttpReactiveDamClient(properties, webClientFactory));
                       })
                       .flatMap(map -> map.entrySet().stream())
                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    @Bean
    public Map<String, ReactiveDamOAuthClient> getDamOAuthClients(@Qualifier("dams") Map<String, DamProperties> dams)  {
        if (damFacadeInUse) {
            return Map.of("1", new ReactiveDamOAuthFacadeClient());
        } else {
            return dams.entrySet().stream()
                       .map(damEntry -> {
                           DamProperties properties = damEntry.getValue();
                           return Map.of(damEntry.getKey(), new HttpReactiveDamOAuthClient(properties));
                       })
                       .flatMap(map -> map.entrySet().stream())
                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

}
