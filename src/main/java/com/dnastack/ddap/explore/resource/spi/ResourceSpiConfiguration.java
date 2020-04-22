package com.dnastack.ddap.explore.resource.spi;

import com.dnastack.ddap.explore.resource.service.ResourceClientService;
import com.dnastack.ddap.explore.resource.spi.SpiConfig.SpiClientConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceSpiConfiguration {

    /**
     * Bean Creation method for generating the {@link ResourceClientService}
     * @param spiConfig
     * @param beanFactory
     * @return
     * @throws SpiConfigurationException
     */
    @Bean
    public ResourceClientService resourceClientFactory(SpiConfig spiConfig, BeanFactory beanFactory) throws SpiConfigurationException {
        List<ResourceClient> resourceClients = new ArrayList<>();
        if (spiConfig == null || spiConfig.getClients() == null || spiConfig.getClients().isEmpty()) {
            throw new SpiConfigurationException("No Resource SPIs are configured");
        }

        Map<String, SpiClientConfig> configs = spiConfig.getClients();
        for (var entry : configs.entrySet()) {
            String spiKey = entry.getKey();
            SpiClientConfig clientConfig = entry.getValue();
            String factoryName = clientConfig.getFactoryName();
            if (factoryName == null) {
                throw new SpiConfigurationException("class-name cannot be null for SPI configuration - " + spiKey);
            }
            // Look up the specified factory and create the resource client
            ResourceClient resourceClient = ResourceClientFactory.lookupFactory(beanFactory, factoryName)
                .create(spiKey, clientConfig);
            resourceClients.add(resourceClient);
        }
        return new ResourceClientService(resourceClients);
    }

}
