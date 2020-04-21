package com.dnastack.ddap.explore.resource.config;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import com.dnastack.ddap.explore.resource.service.ReactiveDamResourceClient;
import com.dnastack.ddap.explore.resource.service.ResourceClientFactory;
import com.dnastack.ddap.explore.resource.spi.ReactiveResourceClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceSpiConfig {


    @Bean
    public ResourceClientFactory resourceClientFactory(@Qualifier("dams") Map<String, DamProperties> damProperties, Map<String, ReactiveDamClient> damClients, Map<String, ReactiveDamOAuthClient> oauthClients) {
        List<ReactiveResourceClient> resourceClients = new ArrayList<>();
        damProperties.keySet()
            .forEach(key -> resourceClients.add(new ReactiveDamResourceClient(key, damClients
                .get(key), oauthClients.get(key), damProperties.get(key))));

        return new ResourceClientFactory(resourceClients);
    }

}
