package com.dnastack.ddap.explore.common;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import dam.v1.DamService;
import reactor.core.publisher.Mono;

import java.util.Map;

public class ReactiveDamFacadeClient implements ReactiveDamClient {
    @Override
    public Mono<DamService.GetInfoResponse> getDamInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Map<String, DamService.Resource>> getResources(String realm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<DamService.Resource> getResource(String realm, String resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Map<String, DamService.View>> getResourceViews(String realm, String resourceId, String damToken, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<DamService.ResourceTokens.ResourceToken> getAccessTokenForView(String realm, String resourceId, String viewId, String damToken, String refreshToken) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Map<String, DamService.GetFlatViewsResponse.FlatView>> getFlattenedViews(String realm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<DamService.ResourceTokens> checkoutCart(String cartToken) {
        throw new UnsupportedOperationException();
    }
}
