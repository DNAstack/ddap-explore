package com.dnastack.ddap.explore.resource.spi.open;

import com.dnastack.ddap.explore.resource.exception.ResourceAuthorizationException;
import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.Id.CollectionId;
import com.dnastack.ddap.explore.resource.model.Id.InterfaceId;
import com.dnastack.ddap.explore.resource.model.Id.ResourceId;
import com.dnastack.ddap.explore.resource.model.OAuthState;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import com.dnastack.ddap.explore.resource.spi.ResourceClient;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public class ReactiveOpenResourceClient implements ResourceClient {


    private final String spikey;
    private final OpenResourceClientConfiguration config;

    public ReactiveOpenResourceClient(String spikey, OpenResourceClientConfiguration config) {
        this.spikey = spikey;
        this.config = config;
    }

    @Override
    public String getSpiKey() {
        return spikey;
    }

    @Override
    public boolean resourceRequiresAutorization(InterfaceId resourceId) {
        return false;
    }

    @Override
    public Mono<List<Resource>> listResources(String realm, List<CollectionId> collectionsToFilter, List<String> interfaceTypesToFilter, List<String> interfaceUrisToFilter) {
        return Mono.fromCallable(() ->
            config.getResources().stream().filter(openResource -> {
                boolean keep = true;
                if (collectionsToFilter != null && !collectionsToFilter.isEmpty()) {
                    CollectionId thisCollection = openResource.getInterfaceId(realm,getSpiKey()).toCollectionId();
                    keep = collectionsToFilter.stream()
                        .anyMatch(thisCollection::equals);
                }

                if (interfaceTypesToFilter != null) {
                    keep &= shouldKeepInterfaceType(openResource.getInterfaceType(),interfaceTypesToFilter);
                }

                if (interfaceUrisToFilter != null) {
                    keep &= shouldKeepInterfaceUri(openResource.getInterfaceUri().toString(), interfaceUrisToFilter);
                }

                return keep;
            }).map(openResource -> openResource.toResource(realm, getSpiKey()))
                .collect(Collectors.toList()));
    }

    @Override
    public Mono<Resource> getResource(String realm, ResourceId id) {
        return Mono.fromCallable(() -> {
            if (!realm.equals(id.getRealm())) {
                throw new IllegalArgumentException("Resource does not exist in this realm");
            }

            return idToResource(realm, id)
                .map(openResource -> openResource.toResource(realm, getSpiKey()))
                .orElseThrow(() -> new NotFoundException("Could not locate resource with id: " + id.encodeId()));

        });
    }

    @Override
    public Mono<List<Collection>> listCollections(String realm) {
        return Mono.fromCallable(() -> config.getCollections().stream()
            .map(collection -> copyCollectionFromConfig(realm, collection)).collect(Collectors.toList()));
    }

    @Override
    public Mono<Collection> getCollection(String realm, CollectionId collectionId) {
        return Mono.fromCallable(() -> config.getCollections().stream()
            .filter(collection -> collection.getName().equals(collectionId.getName()))
            .map(collection -> copyCollectionFromConfig(realm, collection))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(
                "Could not locate collection with id: " + collectionId.encodeId())));
    }

    @Override
    public OAuthState prepareOauthState(String realm, List<InterfaceId> resources, URI postLoginRedirect, String scopes, String loginHint, String ttl) {
        throw new ResourceAuthorizationException(
            "Resource Authorizatization is not enabled for this client:"
                + getSpiKey(), HttpStatus.BAD_REQUEST, resources);
    }

    @Override
    public Mono<List<UserCredential>> handleResponseAndGetCredentials(ServerHttpRequest exchange, URI redirectUri, OAuthState currentState, String code) {
        return Mono.error(() -> new ResourceAuthorizationException(
            "Resource Authorizatization is not enabled for this client:"
                + getSpiKey(), HttpStatus.BAD_REQUEST, currentState.getResourceList()));
    }

    private Optional<OpenResource> idToResource(String realm, ResourceId id) {
        return config.getResources().stream()
            .filter(resource -> resource.getInterfaceId(realm, getSpiKey()).toResourceId().equals(id))
            .findFirst();
    }

    private Collection copyCollectionFromConfig(String realm, Collection collection) {
        CollectionId collectionId = new CollectionId();
        collectionId.setRealm(realm);
        collectionId.setSpiKey(getSpiKey());
        collectionId.setName(collection.getName());

        return Collection.newBuilder()
            .id(collectionId.encodeId())
            .name(collection.getName())
            .imageUrl(collection.getImageUrl())
            .description(collection.getDescription())
            .metadata(collection.getMetadata() != null ? new HashMap<>(collection.getMetadata()) : null)
            .build();
    }

}
