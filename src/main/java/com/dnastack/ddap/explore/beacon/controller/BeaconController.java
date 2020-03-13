package com.dnastack.ddap.explore.beacon.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.beacon.client.BeaconErrorException;
import com.dnastack.ddap.explore.beacon.client.ReactiveBeaconClient;
import com.dnastack.ddap.explore.beacon.model.BeaconInfo;
import com.dnastack.ddap.explore.beacon.model.BeaconQueryError;
import com.dnastack.ddap.explore.beacon.model.BeaconQueryResult;
import com.dnastack.ddap.explore.beacon.model.BeaconRequestModel;
import com.dnastack.ddap.explore.dam.client.DamClientFactory;
import dam.v1.DamService.Resource;
import dam.v1.DamService.View;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;


@Slf4j
@RestController
@RequestMapping(value = "/api/v1alpha/realm/{realm}/resources")
class BeaconController {

    private static final String BEACON_INTERFACE = "http:beacon";

    private ReactiveBeaconClient beaconClient;
    private DamClientFactory damClientFactory;
    private Map<String, ReactiveDamClient> damClients;

    @Autowired
    public BeaconController(ReactiveBeaconClient beaconClient,
                            DamClientFactory damClientFactory,
                            Map<String, ReactiveDamClient> damClients) {
        this.beaconClient = beaconClient;
        this.damClientFactory = damClientFactory;
        this.damClients = damClients;
    }

    @GetMapping(value = "/search")
    public Flux<BeaconQueryResult> aggregateBeaconSearch(@PathVariable String realm,
                                                         BeaconRequestModel beaconRequest) {
        return Flux.fromStream(damClients.entrySet().stream())
            .flatMap(clientEntry -> {
                final String damId = clientEntry.getKey();
                final ReactiveDamClient damClient = clientEntry.getValue();
                return damClient.getResources(realm)
                    .flux()
                    .flatMap((damResources) -> {
                        return maybePerformBeaconQueries(damId, beaconRequest, damResources.entrySet(), null);
                    });
            });
    }

    @GetMapping(value = "{damId}/{resourceId}/search")
    public Flux<BeaconQueryResult> singleResourceBeaconSearch(@PathVariable String realm,
                                                              @PathVariable String damId,
                                                              @PathVariable String resourceId,
                                                              @RequestParam(required = false) String accessToken,
                                                              BeaconRequestModel beaconRequest) {
        final ReactiveDamClient damClient = damClientFactory.getDamClient(damId);

        return damClient.getResource(realm, resourceId)
            .flux()
            .flatMap((damResource) -> {
                Map.Entry<String, Resource> resource = Map.entry(resourceId, damResource);
                return maybePerformBeaconQueries(damId, beaconRequest, Collections.singleton(resource), accessToken);
            });
    }

    private static Stream<? extends BeaconView> filterBeaconView(String damId, Map.Entry<String, Resource> resource) {
        return resource.getValue()
            .getViewsMap()
            .entrySet()
            .stream()
            .flatMap(view -> {
                final List<String> uris = Optional.ofNullable(view.getValue()
                    .getComputedInterfacesMap()
                    .get(BEACON_INTERFACE))
                    .map(iface -> (List<String>) iface.getUriList())
                    .orElseGet(Collections::emptyList);
                return uris.stream()
                    .findFirst()
                    .stream()
                    .map(uri -> new BeaconView(
                        damId,
                        resource.getKey(),
                        resource.getValue(),
                        view.getKey(),
                        view.getValue(),
                        uri
                    ));
            });
    }

    private Flux<BeaconQueryResult> maybePerformBeaconQueries(String damId,
                                                              BeaconRequestModel beaconRequest,
                                                              Collection<Map.Entry<String, Resource>> resourceEntries,
                                                              String viewAccessToken) {
        return resourceEntries
            .stream()
            .flatMap((Map.Entry<String, Resource> resources) -> filterBeaconView(damId, resources))
            .map(beaconView -> {
                if (viewAccessToken != null && !viewAccessToken.isEmpty()) {
                    return maybePerformSingleBeaconViewQuery(beaconView, beaconRequest, viewAccessToken);
                }
                return unauthorizedBeaconApiAlleleResponse(beaconView);
            })
            .map(Mono::flux)
            .reduce(Flux::merge)
            .orElse(Flux.empty());
    }

    private Mono<BeaconQueryResult> unauthorizedBeaconApiAlleleResponse(BeaconView beaconView) {
        final String message = format("Unauthenticated: Cannot access view [%s/%s]",
            beaconView.getResourceId(),
            beaconView.getViewId()
        );
        log.info(message);

        return Mono.just(createErrorBeaconResult(createBeaconInfo(beaconView), 401, message));
    }

    private Mono<BeaconQueryResult> maybePerformSingleBeaconViewQuery(BeaconView beaconView,
                                                                      BeaconRequestModel beaconRequest,
                                                                      String viewAccessToken) {

        final BeaconInfo beaconInfo = createBeaconInfo(beaconView);

        log.debug("About to query: {} beacon at {}", beaconView.getViewId(), beaconView.getUri());

        return beaconClient.queryBeacon(beaconRequest, beaconView.getUri(), viewAccessToken)
            .map(result -> formatBeaconServerPayload(beaconInfo, result))
            .onErrorResume(BeaconErrorException.class, ex -> {
                final BeaconQueryResult result = createErrorBeaconResult(beaconInfo,
                    ex.getStatus(),
                    ex.getMessage());
                return Mono.just(result);
            })
            .onErrorResume(ex -> {
                final BeaconQueryResult result = createErrorBeaconResult(beaconInfo,
                    500,
                    ex.getMessage());
                return Mono.just(result);
            });
    }

    private BeaconInfo createBeaconInfo(BeaconView beaconView) {
        final String viewLabel = getLabelFromUiMap(beaconView.getView().getUiMap());
        final String resourceLabel = getLabelFromUiMap(beaconView.getResource().getUiMap());
        final String role = beaconView.getView().getRolesMap().keySet().stream().findFirst().get(); // Expecting just one role
        final String resourcePath = String.format("%s/views/%s/roles/%s",
            beaconView.getResourceId(), beaconView.getViewId(), role
        );

        return BeaconInfo.builder()
            .damId(beaconView.damId)
            .resourceId(beaconView.getResourceId())
            .viewId(beaconView.getViewId())
            .resourcePath(resourcePath)
            .name(StringUtils.isEmpty(viewLabel) ? beaconView.getViewId() : viewLabel)
            .resourceLabel(StringUtils.isEmpty(resourceLabel) ? beaconView.getResourceId() : resourceLabel)
            .build();
    }

    private String getLabelFromUiMap(Map<String, String> uiMap) {
        if (uiMap == null) {
            return null;
        }
        return uiMap.get("label");
    }

    private BeaconQueryResult createErrorBeaconResult(BeaconInfo beaconInfo, int errorStatus, String errorMessage) {
        BeaconQueryResult fallback = new BeaconQueryResult();
        final BeaconQueryResult result = formatBeaconServerPayload(beaconInfo, fallback);
        result.setQueryError(new BeaconQueryError(errorStatus, errorMessage));
        return result;
    }


    private BeaconQueryResult formatBeaconServerPayload(BeaconInfo infoResponse,
        BeaconQueryResult queryResponse) {
        log.debug("Formatting {} {}", infoResponse, queryResponse);
        queryResponse.setBeaconInfo(infoResponse);

        return queryResponse;
    }

    @Value
    private static class BeaconView {

        String damId;
        String resourceId;
        Resource resource;
        String viewId;
        View view;
        String uri;
    }
}
