package com.dnastack.ddap.explore.common;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.common.config.DamFacadeConfig;
import dam.v1.DamService;
import dam.v1.DamService.ResourceResults;
import dam.v1.DamService.ResourceResults.ResourceAccess;
import dam.v1.DamService.View;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactiveDamFacadeClient implements ReactiveDamClient {
    private final DamFacadeConfig damFacadeConfig;
    private final String defaultPlatform = "ddap-explore-standalone-mode";
    private final int maxTokenTtl = 3600;
    @Deprecated private final String interfaceName = "http:wes";

    public ReactiveDamFacadeClient(DamFacadeConfig damFacadeConfig) {
        this.damFacadeConfig = damFacadeConfig;
    }

    public Mono<DamService.GetInfoResponse> getDamInfo() {
        return Mono.just(DamService.GetInfoResponse.newBuilder()
                .putUi("label", damFacadeConfig.getLabel())
                .build());
    }

    public Mono<Map<String, DamService.Resource>> getResources(String realm) {
        HashMap<String, DamService.Resource> resources = new HashMap<>();
        damFacadeConfig.getResources().forEach((resourceAlias, facadeResource) -> {
            facadeResource.getViews().forEach((viewAlias, facadeView) -> {
                var view = View.newBuilder()
                        .putUi("label", facadeView.getDescription())
                        .putUi("description", facadeView.getDescription())
                        // Fixed access policy
                        .putRoles("execute", DamService.ViewRole.newBuilder()
                                .putComputedPolicyBasis("ControlledAccessGrants", true)
                                .build())
                        .setServiceTemplate(facadeView.getServiceTemplate())
                        .putLabels("version", "1")
                        .putLabels("accessControlType", getAccessControlType(facadeView))
                        .putComputedInterfaces(facadeView.getInterfaceName(), makeWesInterface())
                        .build();

                var resource = DamService.Resource
                        .newBuilder()
                        .putUi("label", facadeResource.getName())
                        .putUi("description", facadeResource.getDescription())
                        .putViews(viewAlias, view)
                        .build();

                resources.put(resourceAlias, resource);
            });
        });

        return Mono.just(resources);
    }

    public Mono<DamService.Resource> getResource(String realm, String resourceId) {
        throw new UnsupportedOperationException();
    }

    public Mono<Map<String, View>> getResourceViews(String realm, String resourceId, String damToken, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    public Mono<Map<String, DamService.GetFlatViewsResponse.FlatView>> getFlattenedViews(String realm) {
        // In this mode, all input data are assumed to be accessible as given without additional authorizations.
        HashMap<String, DamService.GetFlatViewsResponse.FlatView> flattenViews = new HashMap<>();
        damFacadeConfig.getResources().forEach((resourceAlias, resource) -> {
            resource.getViews().forEach((viewAlias, view) -> {
                flattenViews.put(
                        String.format("%s/%s", resourceAlias, viewAlias),
                        DamService.GetFlatViewsResponse.FlatView.newBuilder()
                                .setResourcePath("")
                                .setUmbrella("")
                                .setResourceName(resource.getName())
                                .setViewName(view.getName())
                                .setRoleName("")
                                .setPlatform(defaultPlatform)
                                .setPlatformService("")
                                .setInterfaceName(view.getInterfaceName())
                                .setInterfaceUri(view.getUrl())
                                .setContentType("")
                                .setMaxTokenTtl(String.valueOf(maxTokenTtl))
                                .putViewUi("label", view.getDescription())
                                .putViewUi("description", view.getDescription())
                                .putLabels("accessControlType", getAccessControlType(view))
                            .build()
                );
            });
        });
        return Mono.just(flattenViews);
    }

    private String getAccessControlType(DamFacadeConfig.FacadeView view) {
        return view.isControlledAccess() ? "standalone-protected" : "none";
    }

    public Mono<ResourceResults> checkoutCart(String cartToken) {
        var builder = ResourceResults.newBuilder();
        damFacadeConfig.getResources().forEach((resourceAlias, resource) -> {
            updateResourceResultsBuilder(builder, resourceAlias, resource, cartToken);
        });
        return Mono.just(builder.setEpochSeconds(maxTokenTtl).build());
        // return Mono.just(makeResourceResults(cartToken));
    }

    @Deprecated
    private ResourceResults makeResourceResults(String cartToken) {
        String resourcePath = String.format("%s/views/%s/roles/execute", damFacadeConfig.getResourceName(), damFacadeConfig.getViewName()).toLowerCase();
        return ResourceResults.newBuilder()
                .putResources(resourcePath, ResourceResults.ResourceDescriptor.newBuilder()
                        .putInterfaces(interfaceName, makeWesInterfaceEntry())
                        .setAccess("0")
                        .addAllPermissions(List.of("list", "metadata", "read", "write"))
                        .build())
                .putAccess("0", ResourceAccess.newBuilder()
                        .putLabels("account", "standalone user")
                        .putLabels("platform", defaultPlatform)
                        .putCredentials("access_token", cartToken)
                        .setExpiresIn(maxTokenTtl)
                        .build())
                .setEpochSeconds(maxTokenTtl)
                .build();
    }

    private void updateResourceResultsBuilder(ResourceResults.Builder builder, String resourceAlias, DamFacadeConfig.FacadeResource resource, String accessToken) {
        var counter = new AtomicInteger();

        resource.getViews().forEach((viewAlias, view) -> {
            Integer index = Integer.valueOf(counter.getAndAdd(1));
            String resourcePath = String.format("%s/views/%s/roles/execute", resourceAlias, viewAlias);

            builder.putResources(
                    resourcePath,
                    ResourceResults.ResourceDescriptor.newBuilder()
                                   .putInterfaces(view.getInterfaceName(), makeInterfaceEntry(view))
                                   .setAccess(index.toString())
                                   .addAllPermissions(view.getPermissions())
                                   .build()
            );

            builder.putAccess(
                    index.toString(),
                    ResourceAccess.newBuilder()
                                  .putLabels("account", "standalone user")
                                  .putLabels("platform", defaultPlatform)
                                  .putCredentials("access_token", accessToken)
                                  .setExpiresIn(maxTokenTtl)
                                  .build()
            );
        });
    }

    @Deprecated
    private ResourceResults.InterfaceEntry makeWesInterfaceEntry() {
        return ResourceResults.InterfaceEntry.newBuilder()
                                             .addItems(ResourceResults.ResourceInterface.newBuilder()
                                                                                        .setUri(damFacadeConfig.getWesServerUrl())
                                                                                        .build())
                                             .build();
    }

    private ResourceResults.InterfaceEntry makeInterfaceEntry(DamFacadeConfig.FacadeView view) {
        return ResourceResults.InterfaceEntry.newBuilder()
                .addItems(ResourceResults.ResourceInterface.newBuilder()
                        .setUri(view.getUrl())
                        .build())
                .build();
    }

    @Deprecated
    private DamService.Interface makeWesInterface() {
        return DamService.Interface.newBuilder()
                                   .addUri(damFacadeConfig.getWesServerUrl())
                                   .build();
    }

    private DamService.Interface makeInterface(DamFacadeConfig.FacadeView view) {
        return DamService.Interface.newBuilder()
                .addUri(view.getUrl())
                .build();
    }
}
