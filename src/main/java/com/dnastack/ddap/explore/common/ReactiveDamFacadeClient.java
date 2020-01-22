package com.dnastack.ddap.explore.common;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.common.config.DamFacadeConfig;
import dam.v1.DamService;
import reactor.core.publisher.Mono;

import java.util.Map;

public class ReactiveDamFacadeClient implements ReactiveDamClient {
    private final DamFacadeConfig damFacadeConfig;

    public ReactiveDamFacadeClient(DamFacadeConfig damFacadeConfig) {
        this.damFacadeConfig = damFacadeConfig;
    }

    public Mono<DamService.GetInfoResponse> getDamInfo() {
        return Mono.just(DamService.GetInfoResponse.newBuilder()
                .putUi("label", "DAM Facade")
                .build());
    }

    public Mono<Map<String, DamService.Resource>> getResources(String realm) {
        var view = DamService.View.newBuilder()
                .putUi("label", damFacadeConfig.getViewDescription())
                .putUi("description", damFacadeConfig.getViewDescription())
                // Fixed access policy
                .putAccessRoles("execute", DamService.AccessRole.newBuilder()
                        .putComputedPolicyBasis("ControlledAccessGrants", true)
                        .build())
                .setServiceTemplate("wes")
                .setVersion("1")
                .putComputedInterfaces("http:wes", DamService.Interface.newBuilder()
                        .addUri(damFacadeConfig.getWesServerUrl())
                        .build())
                .build();
        return Mono.just(Map.of(
                "wes",
                DamService.Resource
                        .newBuilder()
                        .putUi("label", damFacadeConfig.getResourceName())
                        .putUi("description", damFacadeConfig.getResourceDescription())
                        .putViews("wes", view)
                        .build()
        ));
    }

    public Mono<DamService.Resource> getResource(String realm, String resourceId) {
        throw new UnsupportedOperationException();
    }

    public Mono<Map<String, DamService.View>> getResourceViews(String realm, String resourceId, String damToken, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    public Mono<DamService.ResourceTokens.ResourceToken> getAccessTokenForView(String realm, String resourceId, String viewId, String damToken, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    public Mono<Map<String, DamService.GetFlatViewsResponse.FlatView>> getFlattenedViews(String realm) {
        // In this mode, all input data are assumed to be accessible as given without additional authorizations.
        return Mono.just(Map.of());
    }

    public Mono<DamService.ResourceTokens> checkoutCart(String cartToken) {
        throw new UnsupportedOperationException();
    }
}
