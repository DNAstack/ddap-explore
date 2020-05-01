package com.dnastack.ddap.explore.resource.service;

import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.Id.CollectionId;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.dnastack.ddap.explore.resource.spi.ResourceClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ResourceClientService {

    private final List<ResourceClient> resourceClients;

    public ResourceClientService(List<ResourceClient> resourceClients) {
        this.resourceClients = resourceClients;
    }

    public ResourceClient getClient(String spiKey) {
        return resourceClients.stream().filter(client -> client.getSpiKey().equals(spiKey))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(
                "Could not locate Reactive ResourceClient for spi key: " + spiKey));
    }

    public Mono<List<Resource>> listResources(String realm, List<CollectionId> collectionsToFilter, List<String> interfaceTypesToFilter, List<String> interfaceUrisToFilter) {
        return Flux.fromIterable(resourceClients)
            .flatMap(client -> client.listResources(realm, collectionsToFilter, interfaceTypesToFilter, interfaceUrisToFilter))
            .reduce((resources, spiResources) -> {
                resources.addAll(spiResources);
                return resources;
            });
    }

    public Mono<List<Collection>> listCollections(String realm) {
        return Flux.fromIterable(resourceClients)
            .flatMap(client -> client.listCollections(realm))
            .reduce((intermediate, spiCollections) -> {
                intermediate.addAll(spiCollections);
                return intermediate;
            });
    }

}
