package com.dnastack.ddap.explore.resource.spi.dam;

import com.dnastack.ddap.common.client.AuthAwareWebClientFactory;
import com.dnastack.ddap.common.client.HttpReactiveDamClient;
import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.explore.dam.client.HttpReactiveDamOAuthClient;
import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import com.dnastack.ddap.explore.resource.exception.IllegalIdentifierException;
import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.Id.CollectionId;
import com.dnastack.ddap.explore.resource.model.Id.InterfaceId;
import com.dnastack.ddap.explore.resource.model.Id.ResourceId;
import com.dnastack.ddap.explore.resource.model.OAuthState;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import com.dnastack.ddap.explore.resource.spi.ResourceClient;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import dam.v1.DamService;
import dam.v1.DamService.GetFlatViewsResponse.FlatView;
import dam.v1.DamService.ResourceResults;
import dam.v1.DamService.ResourceResults.ResourceAccess;
import dam.v1.DamService.ResourceResults.ResourceDescriptor;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public class ReactiveDamResourceClient implements ResourceClient {

    private static final String INTERFACE_PATH_TEMPLATE = "/dam/%s/resources/%s/views/%s/roles/%s/interfaces/%s";

    @Getter
    private final String spiKey;
    private final ReactiveDamClient damClient;
    private final ReactiveDamOAuthClient damOAuthClient;
    private final DamProperties damProperties;

    public ReactiveDamResourceClient(String spiKey, DamProperties config, AuthAwareWebClientFactory authAwareWebClientFactory) {
        this.spiKey = spiKey;
        this.damClient = new HttpReactiveDamClient(config, authAwareWebClientFactory);
        this.damOAuthClient = new HttpReactiveDamOAuthClient(config);
        this.damProperties = config;
    }


    @Override
    public Mono<List<Resource>> listResources(String realm, List<CollectionId> collectionIdsToFilter, List<String> interfaceTypesToFilter, List<String> interfaceUrisToFilter) {
        return damClient.getFlattenedViews(realm).map(views -> views.values().stream().filter(view -> {
            boolean keep = true;
            if (!collectionIdsToFilter.isEmpty()) {
                CollectionId thisCollection = flatViewToInterfaceId(realm, view).toCollectionId();
                thisCollection.setAdditionalProperty(DamAdditionalPropertiesKeys.ROLE_KEY, null);
                keep = collectionIdsToFilter.stream()
                    .anyMatch(thisCollection::equals);
            }
            if (interfaceTypesToFilter != null) {
                keep &= shouldKeepInterfaceType(view.getInterfaceName(),interfaceTypesToFilter);
            }

            if (interfaceUrisToFilter != null && !interfaceUrisToFilter.isEmpty()) {
                keep &= shouldKeepInterfaceUri(view.getInterfaceUri(), interfaceUrisToFilter);
            }
            return keep;
        }).collect(Collectors.toList()))
            .map(flatViews -> {
                LinkedHashMap<String, Resource> resources = new LinkedHashMap<>();
                for (FlatView flatView : flatViews) {
                    InterfaceId interfaceId = flatViewToInterfaceId(realm, flatView);
                    ResourceId resourceId = interfaceId.toResourceId();
                    CollectionId collectionId = interfaceId.toCollectionId();
                    collectionId.setAdditionalProperty(DamAdditionalPropertiesKeys.ROLE_KEY, null);

                    Resource resource = resources
                        .computeIfAbsent(resourceId
                            .encodeId(), (key) -> flatViewToResource(collectionId, resourceId, flatView));

                    AccessInterface accessInterface = new AccessInterface();
                    accessInterface.setId(interfaceId.encodeId());
                    accessInterface.setType(flatView.getInterfaceName());
                    accessInterface
                        .setUri(flatView.getInterfaceUri() != null ? URI.create(flatView.getInterfaceUri()) : null);
                    accessInterface.setAuthRequired(true);
                    resource.getInterfaces().add(accessInterface);


                }
                return new ArrayList<>(resources.values());
            });
    }

    @Override
    public Mono<Resource> getResource(String realm, ResourceId resourceId) {
        validateDamResourceId(resourceId);
        return damClient.getFlattenedViews(realm).map(flatViews -> {
            List<Entry<String, FlatView>> matchingEntries = flatViews.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(getFlatViewPrefix(resourceId)))
                .collect(Collectors.toList());

            if (matchingEntries.isEmpty()) {
                throw new NotFoundException("Could not locate resource with id: " + resourceId.encodeId());
            }

            Resource resource = null;
            for (Entry<String, FlatView> entry : matchingEntries) {
                FlatView view = entry.getValue();
                InterfaceId interfaceId = flatViewToInterfaceId(realm, view);

                if (resource == null) {
                    CollectionId collectionId = interfaceId.toCollectionId();
                    collectionId.setAdditionalProperty(DamAdditionalPropertiesKeys.ROLE_KEY, null);
                    resource = flatViewToResource(collectionId, resourceId, entry.getValue());
                }

                AccessInterface accessInterface = new AccessInterface();
                accessInterface.setId(interfaceId.encodeId());
                accessInterface.setType(view.getInterfaceName());
                accessInterface
                    .setUri(view.getInterfaceUri() != null ? URI.create(view.getInterfaceUri()) : null);
                accessInterface.setAuthRequired(true);
                resource.getInterfaces().add(accessInterface);
            }
            return resource;
        });
    }

    @Override
    public Mono<List<Collection>> listCollections(String realm) {
        return damClient.getResources(realm)
            .map(resources -> {
                List<Collection> collections = new ArrayList<>();
                resources.forEach((k, resource) -> {
                    CollectionId id = new CollectionId();
                    id.setSpiKey(getSpiKey());
                    id.setName(k);
                    id.setRealm(realm);
                    Collection collection = resourceToCollection(id, resource);
                    collections.add(collection);
                });
                return collections;
            });
    }

    @Override
    public Mono<Collection> getCollection(String realm, CollectionId collection) {
        return damClient.getResource(realm, collection.getName())
            .map(resource -> resourceToCollection(collection, resource));

    }

    @Override
    public OAuthState prepareOauthState(String realm, List<InterfaceId> resources, URI postLoginUri, String scopes, String loginHint, String ttl) {
        List<URI> uris = resources.stream().peek(this::validateDamResourceId).map(this::idToInterfaceUri)
            .collect(Collectors.toList());
        String stateString = UUID.randomUUID().toString();
        ZonedDateTime validUntil = ZonedDateTime.now().plusMinutes(10);
        URI authorizatioUrl = damOAuthClient
            .getAuthorizeUrl(realm, stateString, scopes, postLoginUri, uris, loginHint, ttl);
        return new OAuthState(stateString, validUntil, ttl, realm, authorizatioUrl, null, resources);
    }


    private URI idToInterfaceUri(InterfaceId id) {
        return damProperties.getBaseUrl().resolve(URI.create(String
            .format(INTERFACE_PATH_TEMPLATE, id.getRealm(), id.getCollectionName(), id.getResourceName(), id
                .getAdditionalProperty(DamAdditionalPropertiesKeys.ROLE_KEY), id
                .getType())));
    }

    @Override
    public Mono<List<UserCredential>> handleResponseAndGetCredentials(ServerHttpRequest exchange, URI redirectUri, OAuthState currentState, String code) {
        return damOAuthClient.exchangeAuthorizationCodeForTokens(currentState.getRealm(), redirectUri, code)
            .map(this::validateTokenResposne)
            .flatMap(tokenResponse -> damClient.checkoutCart(tokenResponse.getAccessToken()))
            .map(resourceResults -> convertResourceResultToUserCredential(resourceResults, currentState));
    }

    private TokenResponse validateTokenResposne(TokenResponse tokenResponse) {
        Set<String> missingItems = new HashSet<>();
        if (tokenResponse == null) {
            missingItems.add("token");
        } else {
            if (tokenResponse.getAccessToken() == null) {
                missingItems.add("access_token");
            }
        }

        if (!missingItems.isEmpty()) {
            throw new IllegalArgumentException("Incomplete token response: missing " + missingItems);
        }
        return tokenResponse;
    }

    private List<UserCredential> convertResourceResultToUserCredential(ResourceResults resourceResults, OAuthState currentState) {
        return resourceResults.getResourcesMap()
            .entrySet().stream().map(entry -> {
                String key = entry.getKey();
                ResourceDescriptor value = entry.getValue();
                InterfaceId interfaceId = getInterfaceIdForDamResouce(URI.create(key), currentState.getResourceList());
                ResourceAccess resourceAccess = resourceResults.getAccessMap().get(value.getAccess());
                UserCredential userCredential = new UserCredential();
                userCredential.setInterfaceId(interfaceId.encodeId());
                userCredential.setExpirationTime(ZonedDateTime.now().plus(currentState.getTtl()));
                userCredential.setCredentials(resourceAccess.getCredentialsMap());
                return userCredential;
            }).collect(Collectors.toList());
    }

    private InterfaceId getInterfaceIdForDamResouce(URI baseUri, List<InterfaceId> ids) {
        final String template = "/dam/%s/resources/%s/views/%s/roles/%s/interfaces/%s";
        return ids.stream().filter(id -> {
            URI toResolve = URI
                .create(String
                    .format(template, id.getRealm(), id.getCollectionName(), id.getResourceName(), id
                        .getAdditionalProperty(DamAdditionalPropertiesKeys.ROLE_KEY), id
                        .getType()));
            return baseUri.equals(baseUri.resolve(toResolve));
        })
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Could not find dam id"));
    }

    private Resource flatViewToResource(CollectionId collectionId, ResourceId id, FlatView view) {
        String description = view.getViewUiOrDefault("description", "") + " - " + view
            .getRoleUiOrDefault("description", "");
        String name =
            view.getViewUiOrDefault("label", view.getViewName()) + " - " + view.getRoleUiOrDefault("label", view
                .getRoleName());
        String imageUrlString =
            view.getViewUiMap().containsKey("imageUrl") ? view.getViewUiMap().get("imageUrl")
                : view.getResourceUiMap().get("imageUrl");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("serviceName", view.getServiceName());
        metadata.put("platform", view.getPlatform());
        metadata.put("platformService", view.getPlatformService());
        metadata.put("contentType", view.getContentType());
        metadata.putAll(view.getLabelsMap());
        return Resource.newBuilder().id(id.encodeId()).collectionId(collectionId.encodeId()).description(description)
            .name(name).imageUrl(
                imageUrlString != null && !imageUrlString.isEmpty() ? URI.create(imageUrlString)
                    : null)
            .interfaces(new ArrayList<>())
            .metadata(metadata).build();
    }

    private Collection resourceToCollection(CollectionId id, DamService.Resource resource) {
        Collection collection = new Collection();
        collection.setId(id.encodeId());
        collection.setName(resource.getUiOrDefault("label", ""));
        String imageUrlString = resource.getUiOrDefault("imageUrl", null);
        collection.setImageUrl(imageUrlString == null ? null : URI.create(imageUrlString));
        collection.setDescription(resource.getUiOrDefault("description", ""));
        Map<String, String> metadata = new HashMap<>();
        metadata.put("applyUrl", resource.getUiOrDefault("applyUrl", null));
        metadata.put("access", resource.getUiOrDefault("access", null));
        metadata.put("infoUrl", resource.getUiOrDefault("infoUrl", null));
        metadata.put("troubleshootUrl", resource.getUiOrDefault("troubleshootUrl", null));
        metadata.put("owner", resource.getUiOrDefault("owner", null));
        metadata.put("tags", resource.getUiOrDefault("tags", null));
        collection.setMetadata(metadata);
        return collection;
    }

    private InterfaceId flatViewToInterfaceId(String realm, FlatView flatView) {
        InterfaceId id = new InterfaceId();
        id.setSpiKey(getSpiKey());
        id.setRealm(realm);
        id.setCollectionName(flatView.getResourceName());
        id.setResourceName(flatView.getViewName());
        id.setAdditionalProperty(DamAdditionalPropertiesKeys.ROLE_KEY, flatView.getRoleName());
        id.setType(flatView.getInterfaceName());

        return id;
    }

    private String getFlatViewPrefix(ResourceId resourceId) {
        return String.format("/%s/%s/%s", resourceId.getCollectionName(), resourceId.getName(), resourceId
            .getAdditionalProperty(DamAdditionalPropertiesKeys.ROLE_KEY));
    }

    private void validateDamResourceId(Id id) {
        if (id.toResourceId().getAdditionalProperty(DamAdditionalPropertiesKeys.ROLE_KEY) == null) {
            throw new IllegalIdentifierException("Missing required identifier attribute: role");
        }
    }

    public static class DamAdditionalPropertiesKeys {

        static final String ROLE_KEY = "ro";
    }
}
