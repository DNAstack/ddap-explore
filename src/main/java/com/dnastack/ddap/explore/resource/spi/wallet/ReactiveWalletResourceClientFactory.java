package com.dnastack.ddap.explore.resource.spi.wallet;

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
@Component("wallet-resource-client-factory")
public class ReactiveWalletResourceClientFactory extends
    ResourceClientFactory<ReactiveWalletResourceClient, WalletResourceClientConfig> {

    @Autowired
    public ReactiveWalletResourceClientFactory(ObjectMapper objectMapper) {
        super(objectMapper, log);
    }

    @Override
    protected ReactiveWalletResourceClient create(String spiKey, WalletResourceClientConfig walletConfig) throws SpiConfigurationException {
        return new ReactiveWalletResourceClient(spiKey, walletConfig);
    }

    @Override
    protected void validateConfig(String spiKey, WalletResourceClientConfig walletConfig) throws SpiConfigurationException {
        if (walletConfig.getClientId() == null) {
            throw new SpiConfigurationException(
                "Invalid SPI Configraution, missing required property: \"clientId\" for SPI - " + spiKey);
        }

        if (walletConfig.getClientSecret() == null) {
            throw new SpiConfigurationException(
                "Invalid SPI Configraution, missing required property: \"clientSecret\" for SPI - " + spiKey);
        }

        if (walletConfig.getAuthorizationUrl() == null) {
            throw new SpiConfigurationException(
                "Invalid SPI Configraution, missing required property: \"authorizationUrl\" for SPI - " + spiKey);
        }

        if (walletConfig.getTokenUrl() == null) {
            throw new SpiConfigurationException(
                "Invalid SPI Configraution, missing required property: \"tokenUrl\" for SPI - " + spiKey);
        }

        if (walletConfig.getCollections() == null || walletConfig.getCollections().isEmpty()) {
            throw new SpiConfigurationException(
                "Invalid SPI Configraution, missing or empty required property: \"collections\" for SPI - " + spiKey);
        }

        if (walletConfig.getResources() == null || walletConfig.getResources().isEmpty()) {
            throw new SpiConfigurationException(
                "Invalid SPI Configraution, missing or empty required property: \"resources\" for SPI - " + spiKey);
        }

        Set<String> collectionsSeen = new HashSet<>();
        for (Collection collection : walletConfig.getCollections()) {
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
        for (WalletResource resource : walletConfig.getResources()) {
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

            if (walletConfig.getCollections().stream()
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

            if (resource.getScope() == null || resource.getScope().isEmpty()) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, Missing or empty resource property: \"scope\" for resource "
                        + resource.getName() + " in SPI - " + spiKey);
            }

            if (resource.getAudience() == null || resource.getAudience().isEmpty()) {
                throw new SpiConfigurationException(
                    "Invalid SPI Configraution, Missing or empty resource property: \"audience\" for resource "
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
    protected TypeReference<WalletResourceClientConfig> getConfigType() {
        return new TypeReference<>() {
        };
    }


}
