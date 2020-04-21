package com.dnastack.ddap.explore.resource.service;

import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.dnastack.ddap.explore.resource.spi.ReactiveResourceClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ResourceClientFactory {

    private final List<ReactiveResourceClient> resourceClients;

    public ResourceClientFactory(List<ReactiveResourceClient> resourceClients) {
        this.resourceClients = resourceClients;
    }

    public ReactiveResourceClient getClient(String spiKey) {
        return resourceClients.stream().filter(client -> client.getSpiKey().equals(spiKey))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(
                "Could not locate Reactive ResourceClient for spi key: " + spiKey));
    }

    public Mono<List<Resource>> listResources(String realm, List<Id> collectionsToFilter, List<String> interfaceTypesToFilter, List<String> interfaceUrisToFilter) {
        return Flux.concat(resourceClients.stream().map(client -> client
            .listResources(realm, collectionsToFilter, interfaceTypesToFilter, interfaceUrisToFilter))
            .collect(Collectors.toList()))
            .reduce(new ArrayList<Resource>(), (resources, spiResources) -> {
                resources.addAll(spiResources);
                return resources;
            });
    }

    public Mono<List<Collection>> listCollections(String realm) {
        return Flux
            .concat(resourceClients.stream().map(client -> client.listCollections(realm)).collect(Collectors.toList()))
            .reduce((intermediate, spiCollections) -> {
                intermediate.addAll(spiCollections);
                return intermediate;
            });
    }

}
