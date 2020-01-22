package com.dnastack.ddap.explore.common;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import dam.v1.DamService;
import reactor.core.publisher.Mono;

import java.util.Map;

public class ReactiveDamFacadeClient implements ReactiveDamClient {
    public ReactiveDamFacadeClient() {
    }

    public Mono<DamService.GetInfoResponse> getDamInfo() {
        return Mono.just(DamService.GetInfoResponse.newBuilder()
                                                   .putUi("label", "DAM Facade")
                                                   .build());
    }

    public Mono<Map<String, DamService.Resource>> getResources(String realm) {
        return Mono.just(Map.of(
                "wes",
                DamService.Resource
                        .newBuilder()
                        .putUi("label", "WES")
                        .putUi("description", "WES")
                        .putViews("wes", DamService.View.newBuilder()
                                                        .putUi("label", "WES")
                                                        .putUi("description", "WES")
                                                        .setServiceTemplate("wes")
                                                        .build())
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
