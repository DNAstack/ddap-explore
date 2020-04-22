package com.dnastack.ddap.explore.resource.spi.open;

import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.spi.ResourceClientFactory;
import com.dnastack.ddap.explore.resource.spi.SpiConfigurationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component("open-resource-client-factory")
public class ReactiveOpenResourceClientFactory extends
    ResourceClientFactory<ReactiveOpenResourceClient, OpenResourceClientConfiguration> {


    @Autowired
    protected ReactiveOpenResourceClientFactory(ObjectMapper objectMapper) {
        super(objectMapper, log);
    }

    @Override
    protected void validateConfig(String spiKey, OpenResourceClientConfiguration config) throws SpiConfigurationException {
        if (config.getCollections() == null || config.getCollections().isEmpty()) {
            throw new SpiConfigurationException(
                "Invalid SPI Configraution, missing or empty required property: \"collections\" for SPI - " + spiKey);
        }

        if (config.getResources() == null || config.getResources().isEmpty()) {
            throw new SpiConfigurationException(
                "Invalid SPI Configraution, missing or empty required property: \"resources\" for SPI - " + spiKey);
        }

        Set<String> collectionsSeen = new HashSet<>();
        for (Collection collection : config.getCollections()) {
            if (collection.getName() == null || collection.getName().isEmpty()) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, Missing or empty collection property: \"name\" for SPI - " + spiKey);
            }

            if (collection.getDescription() == null || collection.getDescription().isEmpty()) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, Missing or empty collection property: \"description\" for collection "
                        + collection.getName() + " in SPI - " + spiKey);
            }

            if (collectionsSeen.contains(collection.getName())) {
                throw new SpiConfigurationException(
                    "Duplicate collection configuration detected for collection: " + collection.getName() + " in SPI -"
                        + spiKey);
            } else {
                collectionsSeen.add(collection.getName());
            }
        }

        Set<String> resourcesSeen = new HashSet<>();
        for (OpenResource resource : config.getResources()) {
            if (resource.getName() == null || resource.getName().isEmpty()) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, Missing or empty resource property: \"name\" for SPI - " + spiKey);
            }

            if (resource.getDescription() == null || resource.getDescription().isEmpty()) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, Missing or empty resource property: \"description\" for resource "
                        + resource.getName() + " in SPI - " + spiKey);
            }

            if (resource.getCollectionName() == null || resource.getCollectionName().isEmpty()) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, Missing or empty resource property: \"collectionName\" for resource "
                        + resource.getName() + " in SPI - " + spiKey);
            }

            if (config.getCollections().stream()
                .noneMatch(col -> col.getName().equals(resource.getCollectionName()))) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, collectionName for resource " + resource.getName()
                        + " does not exist in collections in SPI - " + spiKey);
            }

            if (resource.getInterfaceType() == null || resource.getInterfaceType().isEmpty()) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, Missing or empty resource property: \"interfaceType\" for resource "
                        + resource.getName() + " in SPI - " + spiKey);
            }

            if (resource.getInterfaceUri() == null) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, Missing or empty resource property: \"interfaceUri\" for resource "
                        + resource.getName() + " in SPI - " + spiKey);
            }

            String concatString = resource.getCollectionName() + resource.getName();
            if (resourcesSeen.contains(concatString)) {
                throw new SpiConfigurationException(
                    "Duplicate resource configuration detected for resource: " + resource.getName()
                        + " with collection: " + resource.getCollectionName() + " in SPI -" + spiKey);
            } else {
                resourcesSeen.add(concatString);
            }
        }
    }

    @Override
    protected ReactiveOpenResourceClient create(String spiKey, OpenResourceClientConfiguration spiSpecificConfig) throws
        SpiConfigurationException {
        return new ReactiveOpenResourceClient(spiKey, spiSpecificConfig);
    }

    @Override
    protected TypeReference<OpenResourceClientConfiguration> getConfigType() {
        return new TypeReference<>() {
        };
    }
}
