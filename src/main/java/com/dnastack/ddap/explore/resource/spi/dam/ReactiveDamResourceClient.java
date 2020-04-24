package com.dnastack.ddap.explore.resource.spi.dam;

import com.dnastack.ddap.common.client.AuthAwareWebClientFactory;
import com.dnastack.ddap.common.client.HttpReactiveDamClient;
import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.explore.dam.client.HttpReactiveDamOAuthClient;
import com.dnastack.ddap.explore.dam.client.ReactiveDamOAuthClient;
import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.OAuthState;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import com.dnastack.ddap.explore.resource.spi.ResourceClient;
import com.dnastack.ddap.ic.oauth.model.TokenResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
    public Mono<List<Resource>> listResources(String realm, List<Id> collectionIdsToFilter, List<String> interfaceTypesToFilter, List<String> interfaceUrisToFilter) {
        return damClient.getFlattenedViews(realm).map(views -> views.values().stream().filter(view -> {
            boolean keep = true;
            if (!collectionIdsToFilter.isEmpty()) {
                keep = collectionIdsToFilter.stream()
                    .anyMatch(id -> id.getRealm().equals(realm) && id.getCollectionId().equals(view.getResourceName())
                        && id.getSpiKey()
                        .equals(getSpiKey()));
            }
            if (interfaceTypesToFilter != null && !interfaceTypesToFilter.isEmpty()) {
                keep &= interfaceTypesToFilter.stream()
                    .anyMatch(type -> type.equals(view.getInterfaceName()));
            }

            if (interfaceUrisToFilter != null && !interfaceUrisToFilter.isEmpty()) {
                keep &= shouldKeepInterfaceUri(view.getInterfaceUri(), interfaceUrisToFilter);
            }
            return keep;
        }).collect(Collectors.toList()))
            .map(flatViews -> {
                LinkedHashMap<String, Resource> resources = new LinkedHashMap<>();
                for (FlatView flatView : flatViews) {
                    String collectionId = flatViewToDamId(realm, flatView, DamResourceType.COLLECTION).encodeId();
                    String resourceId = flatViewToDamId(realm, flatView, DamResourceType.RESOURCE).encodeId();
                    Resource resource = resources
                        .computeIfAbsent(resourceId, (key) -> flatViewToResource(collectionId, resourceId, flatView));
                    String interfaceId = flatViewToDamId(realm, flatView, DamResourceType.INTERFACE).encodeId();
                    AccessInterface accessInterface = new AccessInterface();
                    accessInterface.setId(interfaceId);
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
    public Mono<Resource> getResource(String realm, Id resourceId) {
        DamId damId = new DamId(resourceId, DamResourceType.RESOURCE);
        return damClient.getFlattenedViews(realm).map(flatViews -> {
            List<Entry<String, FlatView>> matchingEntries = flatViews.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(damId.toFlatViewPrefix()))
                .collect(Collectors.toList());

            if (matchingEntries.isEmpty()) {
                throw new NotFoundException("Could not locate resource with id: " + resourceId.encodeId());
            }

            Resource resource = null;
            for (Entry<String, FlatView> entry : matchingEntries) {
                FlatView view = entry.getValue();
                if (resource == null) {
                    String collectionId = flatViewToDamId(realm, view, DamResourceType.COLLECTION).encodeId();
                    resource = flatViewToResource(collectionId, resourceId.encodeId(), entry.getValue());
                }

                String interfaceId = flatViewToDamId(realm, view, DamResourceType.INTERFACE).encodeId();
                AccessInterface accessInterface = new AccessInterface();
                accessInterface.setId(interfaceId);
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
                    DamId id = new DamId();
                    id.setSpiKey(getSpiKey());
                    id.setCollectionId(k);
                    id.setRealm(realm);
                    id.validate(DamResourceType.COLLECTION);
                    Collection collection = resourceToCollection(id, resource);
                    collections.add(collection);
                });
                return collections;
            });
    }

    @Override
    public Mono<Collection> getCollection(String realm, Id collection) {
        DamId id = new DamId(collection, DamResourceType.COLLECTION);
        return damClient.getResource(realm, id.getCollectionId())
            .map(resource -> resourceToCollection(id, resource));

    }

    @Override
    public OAuthState prepareOauthState(String realm, List<Id> resources, URI postLoginUri, String scopes, String loginHint, String ttl) {
        List<URI> uris = resources.stream().map(resourceId -> new DamId(resourceId, DamResourceType.INTERFACE))
            .map(this::idToInterfaceUri).collect(Collectors.toList());
        String stateString = UUID.randomUUID().toString();
        ZonedDateTime validUntil = ZonedDateTime.now().plusMinutes(10);
        URI authorizatioUrl = damOAuthClient
            .getAuthorizeUrl(realm, stateString, scopes, postLoginUri, uris, loginHint, ttl);
        return new OAuthState(stateString, validUntil, ttl, realm, authorizatioUrl, null, resources);
    }


    private URI idToInterfaceUri(DamId id) {
        return damProperties.getBaseUrl().resolve(URI.create(String
            .format(INTERFACE_PATH_TEMPLATE, id.getRealm(), id.getCollectionId(), id.getViewName(), id.getRoleName(), id
                .getInterfaceType())));
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
        List<DamId> damIds = currentState.getResourceList().stream().map(id -> new DamId(id, DamResourceType.INTERFACE))
            .collect(Collectors.toList());
        return resourceResults.getResourcesMap()
            .entrySet().stream().map(entry -> {
                String key = entry.getKey();
                ResourceDescriptor value = entry.getValue();
                DamId interfaceId = getDamIdForResourceUri(URI.create(key), damIds);
                ResourceAccess resourceAccess = resourceResults.getAccessMap().get(value.getAccess());
                UserCredential userCredential = new UserCredential();
                userCredential.setInterfaceId(interfaceId.encodeId());
                userCredential.setExpirationTime(ZonedDateTime.now().plus(currentState.getTtl()));
                userCredential.setCredentials(resourceAccess.getCredentialsMap());
                return userCredential;
            }).collect(Collectors.toList());
    }

    private DamId getDamIdForResourceUri(URI baseUri, List<DamId> ids) {
        final String template = "/dam/%s/resources/%s/views/%s/roles/%s/interfaces/%s";
        return ids.stream().filter(id -> {
            URI toResolve = URI
                .create(String
                    .format(template, id.getRealm(), id.getCollectionId(), id.getViewName(), id.getRoleName(), id
                        .getInterfaceType()));
            return baseUri.equals(baseUri.resolve(toResolve));
        })
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Could not find dam id"));
    }

    private Resource flatViewToResource(String collectionId, String id, FlatView view) {
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
        return Resource.newBuilder().id(id).collectionId(collectionId).description(description)
            .name(name).imageUrl(
                imageUrlString != null && !imageUrlString.isEmpty() ? URI.create(imageUrlString)
                    : null)
            .interfaces(new ArrayList<>())
            .metadata(metadata).build();
    }

    private Collection resourceToCollection(Id id, DamService.Resource resource) {
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

    private DamId flatViewToDamId(String realm, FlatView flatView, DamResourceType resourceType) {
        DamId id = new DamId();
        id.setSpiKey(getSpiKey());
        id.setRealm(realm);
        id.setCollectionId(flatView.getResourceName());
        if (resourceType.equals(DamResourceType.RESOURCE)) {
            id.setViewName(flatView.getViewName());
            id.setRoleName(flatView.getRoleName());
            id.validate(DamResourceType.RESOURCE);
        } else if (resourceType.equals(DamResourceType.INTERFACE)) {
            id.setViewName(flatView.getViewName());
            id.setRoleName(flatView.getRoleName());
            id.setInterfaceType(flatView.getInterfaceName());
            id.validate(DamResourceType.INTERFACE);
        }
        return id;
    }


    public enum DamResourceType {
        COLLECTION, RESOURCE, INTERFACE
    }


    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DamId extends Id {

        private static final long serialVersionUID = 5610629031925135439L;


        public DamId() {
            super();
        }

        public DamId(Id id, DamResourceType resource) {
            super(id);
            validate(resource);
        }

        private void validate(DamResourceType resource) {
            switch (resource) {
                case COLLECTION:
                    Objects
                        .requireNonNull(getCollectionId(), "Invalid DamId, missing required attribute: collectionId");
                    break;
                case RESOURCE:
                    validate(DamResourceType.COLLECTION);
                    Objects.requireNonNull(getViewName(), "Invalid DamID, missing required attribute: view");
                    Objects.requireNonNull(getRoleName(), "Invalid DamId, missing required attribute: roleName");
                    break;
                case INTERFACE:
                    validate(DamResourceType.RESOURCE);
                    Objects
                        .requireNonNull(getInterfaceType(), "Invalid DamId, missing required attribute: interfaceType");
            }
        }

        @JsonIgnore
        public String getViewName() {
            return getResourceId();
        }

        @JsonIgnore
        public String getRoleName() {
            return getAdditionalProperties().get("ro");
        }

        @JsonIgnore
        public void setViewName(String view) {
            setResourceId(view);
        }

        @JsonIgnore
        public void setRoleName(String role) {
            setAdditionalProperties("ro", role);
        }

        public String toFlatViewPrefix() {
            return String.format("/%s/%s/%s", getCollectionId(), getViewName(), getRoleName());
        }
    }
}
