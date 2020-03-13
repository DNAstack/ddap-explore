package com.dnastack.ddap.explore.common;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.common.config.DamFacadeConfig;
import dam.v1.DamService;
import dam.v1.DamService.ResourceResults;
import dam.v1.DamService.ResourceResults.ResourceAccess;
import dam.v1.DamService.View;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class ReactiveDamFacadeClient implements ReactiveDamClient {
    private final DamFacadeConfig damFacadeConfig;
    private final String interfaceName = "http:wes";

    public ReactiveDamFacadeClient(DamFacadeConfig damFacadeConfig) {
        this.damFacadeConfig = damFacadeConfig;
    }

    public Mono<DamService.GetInfoResponse> getDamInfo() {
        return Mono.just(DamService.GetInfoResponse.newBuilder()
                .putUi("label", "DAM Facade")
                .build());
    }

    public Mono<Map<String, DamService.Resource>> getResources(String realm) {
        String resourceId = damFacadeConfig.getResourceName().toLowerCase();
        String viewId = damFacadeConfig.getViewName().toLowerCase();

        var view = View.newBuilder()
                       .putUi("label", damFacadeConfig.getViewDescription())
                       .putUi("description", damFacadeConfig.getViewDescription())
                       // Fixed access policy
                       .putRoles("execute", DamService.ViewRole.newBuilder()
                                                               .putComputedPolicyBasis("ControlledAccessGrants", true)
                                                               .build())
                       .setServiceTemplate("wes")
                       .putLabels("version", "1")
                       .putComputedInterfaces(interfaceName, makeWesInterface())
                       .build();
        return Mono.just(Map.of(
                resourceId,
                DamService.Resource
                        .newBuilder()
                        .putUi("label", damFacadeConfig.getResourceName())
                        .putUi("description", damFacadeConfig.getResourceDescription())
                        .putViews(viewId, view)
                        .build()
        ));
    }

    public Mono<DamService.Resource> getResource(String realm, String resourceId) {
        throw new UnsupportedOperationException();
    }

    public Mono<Map<String, View>> getResourceViews(String realm, String resourceId, String damToken, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    public Mono<Map<String, DamService.GetFlatViewsResponse.FlatView>> getFlattenedViews(String realm) {
        // In this mode, all input data are assumed to be accessible as given without additional authorizations.
        return Mono.just(Map.of());
    }

    public Mono<ResourceResults> checkoutCart(String cartToken) {
        String resourcePath = String.format("%s/views/%s/roles/execute", damFacadeConfig.getResourceName(), damFacadeConfig.getViewName()).toLowerCase();
        return Mono.just(ResourceResults.newBuilder()
                                        .putResources(resourcePath, ResourceResults.ResourceDescriptor.newBuilder()
                                                                                                      .putInterfaces(interfaceName, makeWesInterfaceEntry())
                                                                                                      .setAccess("0")
                                                                                                      .addAllPermissions(List.of("list", "metadata", "read", "write"))
                                                                                                      .build())
                                        .putAccess("0", ResourceAccess.newBuilder()
                                                                      .putLabels("account", "standalone user")
                                                                      .putLabels("platform", "ddap-explore-standalone-mode")
                                                                      .putCredentials("access_token", cartToken)
                                                                      .setExpiresIn(3600)
                                                                      .build())
                                        .setEpochSeconds(3600)
                                        .build());
    }

    private ResourceResults.InterfaceEntry makeWesInterfaceEntry() {
        return ResourceResults.InterfaceEntry.newBuilder()
                                             .addItems(ResourceResults.ResourceInterface.newBuilder()
                                                                                        .setUri(damFacadeConfig.getWesServerUrl())
                                                                                        .build())
                                             .build();
    }

    private DamService.Interface makeWesInterface() {
        return DamService.Interface.newBuilder()
                                   .addUri(damFacadeConfig.getWesServerUrl())
                                   .build();
    }
}
