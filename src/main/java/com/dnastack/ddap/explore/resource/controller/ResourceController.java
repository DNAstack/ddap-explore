package com.dnastack.ddap.explore.resource.controller;

import static org.springframework.http.HttpStatus.TEMPORARY_REDIRECT;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.util.http.UriUtil;
import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.PaginatedResponse;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dam.v1.DamService;
import dam.v1.DamService.GetFlatViewsResponse.FlatView;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1beta")
public class ResourceController {


    @Autowired
    private Map<String, ReactiveDamClient> damClients;
    @Autowired
    private ObjectMapper mapper;


    @GetMapping(value = "/{realm}/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PaginatedResponse<Resource>> listResources(@PathVariable("realm") String realm, @RequestParam(value = "collection", required = false) String collection, @RequestParam(value = "interface_type", required = false) String interfaceType, @RequestParam(value = "interface_uri", required = false) String interfaceUri, @RequestParam(value = "page_token", required = false) String pageToken) {
        Id collectionId = collection != null ? decodeId(collection) : null;
        return Flux.concat(damClients.entrySet().stream().map(entry -> {
            return entry.getValue().getFlattenedViews(realm)
                .map(views -> {
                    return views.entrySet().stream().map(Entry::getValue).filter(view -> {
                        boolean keep = true;
                        if (collectionId != null) {
                            keep &= collectionId.getCollectionId().equals(view.getResourceName());
                        }
                        if (interfaceType != null) {
                            keep &= interfaceType.equals(view.getInterfaceName());
                        }

                        if (interfaceUri != null) {
                            keep &= interfaceUri.equals(view.getInterfaceUri());
                        }
                        return keep;
                    }).collect(Collectors.toList());
                })
                .map((views) -> {
                    LinkedHashMap<String, Resource> resources = new LinkedHashMap<>();
                    views.forEach((view) -> {
                        Id viewColId = new Id();
                        viewColId.setSpiKey(entry.getKey());
                        viewColId.setCollectionId(view.getResourceName());
                        DamId id = new DamId(viewColId);
                        id.setViewName(view.getViewName());
                        id.setRoleName(view.getRoleName());
                        String stringifiedId = encodeId(id);
                        String stringifiedColId = encodeId(viewColId);

                        Resource resource = resources
                            .computeIfAbsent(stringifiedId, (idKey) -> viewToResource(stringifiedColId, stringifiedId, view));
                        DamId authorizationId = new DamId(id);
                        authorizationId.setInterfaceType(view.getInterfaceName());
                        AccessInterface accessInterface = new AccessInterface();
                        accessInterface.setAuthorizationId(encodeId(authorizationId));
                        accessInterface.setType(view.getInterfaceName());
                        accessInterface
                            .setUri(view.getInterfaceUri() != null ? URI.create(view.getInterfaceUri()) : null);

                        resource.getInterfaces().add(accessInterface);
                        return;
                    });
                    return resources.values();
                });
        }).collect(Collectors.toList()))
            .reduce(new ArrayList<Resource>(), (resources, damResources) -> {
                resources.addAll(damResources);
                return resources;
            }).map(PaginatedResponse::new);

    }


    @GetMapping(value = "/{realm}/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Resource> getResource(@PathVariable("realm") String realm, @PathVariable("id") String resourceId) {
        DamId damId = new DamId(decodeId(resourceId));
        ReactiveDamClient damClient = damClients.get(damId.getSpiKey());
        if (damClient == null) {
            throw new IllegalArgumentException("No configured dam with id: " + damId.getSpiKey());
        }

        return damClient.getFlattenedViews(realm).map(flatViews -> {
            List<Entry<String, FlatView>> matchingEntries = flatViews.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(damId.toFlatViewPrefix()))
                .collect(Collectors.toList());

            if (matchingEntries.isEmpty()) {
                throw new NotFoundException("Could not locate resource with id: " + resourceId);
            }

            Resource resource = null;
            for (Entry<String, FlatView> entry : matchingEntries) {
                FlatView view = entry.getValue();
                if (resource == null) {
                    Id collectionId = new Id();
                    collectionId.setSpiKey(damId.getSpiKey());
                    collectionId.setCollectionId(damId.getCollectionId());
                    String stringifiedCollectionId = encodeId(collectionId);
                    resource = viewToResource(stringifiedCollectionId, resourceId, entry.getValue());
                }

                DamId authorizationId = new DamId(damId);
                authorizationId.setInterfaceType(view.getInterfaceName());
                AccessInterface accessInterface = new AccessInterface();
                accessInterface.setAuthorizationId(encodeId(authorizationId));
                accessInterface.setType(view.getInterfaceName());
                accessInterface
                    .setUri(view.getInterfaceUri() != null ? URI.create(view.getInterfaceUri()) : null);

                resource.getInterfaces().add(accessInterface);
            }
            return resource;
        });
    }


    @GetMapping(value = "/{realm}/collections", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PaginatedResponse<Collection>> listCollections(@PathVariable("realm") String realm) {
        return Flux.concat(damClients.entrySet().stream().map(entry -> {
            return entry.getValue().getResources(realm)
                .map(resources -> {
                    List<Collection> collections = new ArrayList<>();
                    resources.forEach((k, resource) -> {
                        Id id = new Id();
                        id.setSpiKey(entry.getKey());
                        id.setCollectionId(k);
                        Collection collection = resourceToCollection(id, resource);
                        collections.add(collection);
                    });
                    return collections;
                });
        }).collect(Collectors.toList()))
            .reduce(new ArrayList<Collection>(), (initial, collections) -> {
                initial.addAll(collections);
                return initial;
            })
            .map(PaginatedResponse::new);
    }

    @GetMapping(value = "/{realm}/collections/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Collection> getCollection(@PathVariable("realm") String realm, @PathVariable("id") String collectionId) {
        Id id = decodeId(collectionId);
        ReactiveDamClient damClient = damClients.get(id.getSpiKey());
        if (damClient == null) {
            throw new IllegalArgumentException("No configured dam with id: " + id.getSpiKey());
        }
        return damClient.getResource(realm, id.getCollectionId())
            .map(resource -> resourceToCollection(id, resource));
    }

    @GetMapping("/{realm}/resources/checkout")
    public ResponseEntity<?> checkout(ServerHttpRequest request,
        @PathVariable String realm,
        @RequestParam("resource") List<String> authorizationIds) {

        final String damResourceTemplate = "%s;%s/views/%s/roles/%s/interfaces/%s";
        List<DamId> damAuthorizationIds = authorizationIds.stream()
            .map(authorizationId -> new DamId(decodeId(authorizationId)))
            .collect(Collectors.toList());
        URI v1AlphaCheckoutUri = UriUtil.selfLinkToApi(request, realm, "resources/checkout");
        UriComponentsBuilder v1AlphaCheckoutUriBuilder = UriComponentsBuilder.fromUri(v1AlphaCheckoutUri);
        damAuthorizationIds.stream().forEach(id -> {
            String damIdResourcePair = String
                .format(damResourceTemplate, id.getSpiKey(), id.getCollectionId(), id.getViewName(), id
                    .getRoleName(), id.getInterfaceType());
            v1AlphaCheckoutUriBuilder.queryParam("resource", damIdResourcePair);
        });

        return ResponseEntity.status(TEMPORARY_REDIRECT)
            .location(v1AlphaCheckoutUriBuilder.build().toUri())
            .build();
    }


    @GetMapping("/{realm}/resources/authorize")
    public ResponseEntity<?> authorizeResources(ServerHttpRequest request,
        @PathVariable String realm,
        @RequestParam(required = false, name = "login_hint") String loginHint,
        @RequestParam(required = false, name = "redirect_uri") URI redirectUri,
        @RequestParam(required = false) String scope,
        @RequestParam("resource") List<String> authorizationIds,
        @RequestParam(defaultValue = "1h") String ttl) {

        final String damResourceTemplate = "%s;%s/views/%s/roles/%s/interfaces/%s";
        List<DamId> damAuthorizationIds = authorizationIds.stream()
            .map(authorizationId -> new DamId(decodeId(authorizationId)))
            .collect(Collectors.toList());
        URI v1AlphaAuthorizeUri = UriUtil.selfLinkToApi(request, realm, "resources/authorize");
        UriComponentsBuilder v1AlphaAuthorizeUriBuilder = UriComponentsBuilder.fromUri(v1AlphaAuthorizeUri);
        if (loginHint != null && !loginHint.isEmpty()) {
            v1AlphaAuthorizeUriBuilder.queryParam("loginHint", loginHint);
        }

        if (redirectUri != null) {
            v1AlphaAuthorizeUriBuilder.queryParam("redirectUri", redirectUri.toString());
        }

        if (scope != null) {
            v1AlphaAuthorizeUriBuilder.queryParam("scope", scope);
        }

        if (ttl != null) {
            v1AlphaAuthorizeUriBuilder.queryParam("ttl", ttl);
        }

        damAuthorizationIds.stream().forEach(id -> {
            String damIdResourcePair = String
                .format(damResourceTemplate, id.getSpiKey(), id.getCollectionId(), id.getViewName(), id
                    .getRoleName(), id.getInterfaceType());
            v1AlphaAuthorizeUriBuilder.queryParam("resource", damIdResourcePair);
        });

        return ResponseEntity.status(TEMPORARY_REDIRECT)
            .location(v1AlphaAuthorizeUriBuilder.build().toUri())
            .build();
    }


    private Collection resourceToCollection(Id id, DamService.Resource resource) {
        Collection collection = new Collection();
        collection.setId(encodeId(id));
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

    private Resource viewToResource(String collectionId, String id, FlatView view) {
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


    public Id decodeId(String idString) {
        String decodedIdString = new String(Base64.getUrlDecoder().decode(idString), StandardCharsets.UTF_8);
        try {
            return mapper.readValue(decodedIdString, Id.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String encodeId(Id id) {
        try {
            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mapper.writeValueAsString(id).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    public static class Id {

        /**
         * SPI Skey
         */
        @JsonProperty("k")
        String spiKey;

        /**
         * Collection Id
         */
        @JsonProperty("c")
        String collectionId;

        @JsonIgnore
        Map<String, String> additionalProperties;

        @JsonAnyGetter
        public Map<String, String> getAdditionalProperties() {
            return additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperties(String key, String value) {
            if (additionalProperties == null) {
                additionalProperties = new HashMap<>();
            }
            additionalProperties.put(key, value);
        }

        @JsonAnySetter
        public void setAdditionalProperties(Map<String, String> additionalProperties) {
            if (this.additionalProperties == null) {
                this.additionalProperties = additionalProperties;
            } else {
                this.additionalProperties.putAll(additionalProperties);
            }
        }

    }


    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DamId extends Id {

        public DamId(Id id) {
            spiKey = id.getSpiKey();
            collectionId = id.getCollectionId();
            if (id.getAdditionalProperties() == null) {
                additionalProperties = new HashMap<>();
            } else {
                additionalProperties = id.getAdditionalProperties();
            }
        }

        @JsonIgnore
        public String getViewName() {
            return additionalProperties.get("v");
        }

        @JsonIgnore
        public String getRoleName() {
            return additionalProperties.get("r");
        }

        @JsonIgnore
        public String getInterfaceType() {
            return additionalProperties.get("i");
        }

        @JsonIgnore
        public void setViewName(String view) {
            additionalProperties.put("v", view);
        }

        @JsonIgnore
        public void setRoleName(String role) {
            additionalProperties.put("r", role);
        }

        @JsonIgnore
        public void setInterfaceType(String interfaceType) {
            additionalProperties.put("i", interfaceType);
        }

        public String toFlatViewPrefix() {
            return String.format("/%s/%s/%s", collectionId, getViewName(), getRoleName());
        }

    }

}
