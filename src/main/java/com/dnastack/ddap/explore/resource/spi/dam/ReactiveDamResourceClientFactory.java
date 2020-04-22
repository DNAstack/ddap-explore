package com.dnastack.ddap.explore.resource.spi.dam;

import com.dnastack.ddap.common.client.AuthAwareWebClientFactory;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.explore.resource.spi.ResourceClientFactory;
import com.dnastack.ddap.explore.resource.spi.SpiConfigurationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("dam-resource-client-factory")
public class ReactiveDamResourceClientFactory extends ResourceClientFactory<ReactiveDamResourceClient, DamProperties> {

    private final AuthAwareWebClientFactory webClientFactory;

    @Autowired
    public ReactiveDamResourceClientFactory( AuthAwareWebClientFactory webClientFactory, ObjectMapper objectMapper) {
        super(objectMapper, log);
        this.webClientFactory = webClientFactory;
    }

    @Override
    protected void validateConfig(String spiKey,DamProperties damProperties) throws SpiConfigurationException {
        if (damProperties.getBaseUrl() == null) {
            throw new SpiConfigurationException(
                "Invalid SPI configuration, missing required property: \"base-url\" for SPI - " + spiKey);
        }
        if (damProperties.getClientId() == null) {
            throw new SpiConfigurationException(
                "Invalid SPI configuration, missing required property: \"client-id\" for SPI - " + spiKey);
        }
        if (damProperties.getClientSecret() == null) {
            throw new SpiConfigurationException(
                "Invalid SPI configuration, missing required property: \"client-secret\" for SPI - " + spiKey);
        }
    }

    @Override
    protected ReactiveDamResourceClient create(String spiKey, DamProperties damProperties) throws SpiConfigurationException {
        return new ReactiveDamResourceClient(spiKey, damProperties, webClientFactory);
    }

    @Override
    protected TypeReference<DamProperties> getConfigType() {
        return new TypeReference<>() {
        };
    }


}
