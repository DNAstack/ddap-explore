package com.dnastack.ddap.explore.apps.discovery.client;

import com.dnastack.ddap.common.util.logging.LoggingFilter;
import com.dnastack.ddap.explore.apps.beacon.client.BeaconErrorException;
import com.dnastack.ddap.explore.apps.discovery.model.DiscoveryBeacon;
import com.dnastack.ddap.explore.apps.discovery.model.DiscoveryBeaconQueryResult;
import com.dnastack.ddap.explore.apps.discovery.model.DiscoveryBeaconRequestModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

@Slf4j
@Component
public class ReactiveDiscoveryBeaconClient {

    private static final WebClient webClient = WebClient.builder()
        .filter(LoggingFilter.logRequest())
        .filter(LoggingFilter.logResponse())
        .build();

    public Mono<DiscoveryBeaconQueryResult> queryBeacon(DiscoveryBeaconRequestModel beaconRequest, URI beaconUrl, String viewAccessToken) {

        RequestHeadersSpec<?> spec = webClient.get()
            .uri(getBeaconQueryUrl(beaconUrl, beaconRequest));
        if (viewAccessToken != null) {
            spec.headers(h -> h.setBearerAuth(viewAccessToken));
        }
        return spec.exchange().flatMap(this::mapResponseToBeaconResult);
    }

    public Mono<DiscoveryBeacon> getBeaconInfo(URI beaconInfoUrl) {
        return webClient.get()
            .uri(beaconInfoUrl)
            .exchange()
            .flatMap(this::mapResponseToBeacon);
    }

    private Mono<DiscoveryBeacon> mapResponseToBeacon(ClientResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(DiscoveryBeacon.class);
        } else {
            return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new BeaconErrorException(response.statusCode()
                    .value(),
                    body)));
        }
    }

    private Mono<DiscoveryBeaconQueryResult> mapResponseToBeaconResult(ClientResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(DiscoveryBeaconQueryResult.class)
                .flatMap(result -> {
                    if (result.getExists() == null) {
                        return Mono.error(Optional.ofNullable(result.getError())
                            .map(error -> new BeaconErrorException(
                                error.getErrorCode(),
                                error.getErrorMessage()))
                            .orElseGet(() -> new BeaconErrorException(
                                null,
                                null)));
                    } else {
                        return Mono.just(result);
                    }
                });
        } else {
            return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new BeaconErrorException(response.statusCode()
                    .value(),
                    body)));
        }
    }

    private URI getBeaconQueryUrl(URI beaconBaseUrl, DiscoveryBeaconRequestModel beaconRequest) {
        UriComponentsBuilder beaconUriBuilder = UriComponentsBuilder.fromUri(beaconBaseUrl).pathSegment("query")
            .queryParam("assemblyId", beaconRequest.getAssemblyId())
            .queryParam("referenceName", beaconRequest.getReferenceName())
            .queryParam("start", beaconRequest.getStart())
            .queryParam("referenceBases", beaconRequest.getReferenceBases())
            .queryParam("alternateBases", beaconRequest.getAlternateBases())
            .queryParam("includeDatasetResponses", "ALL");

        if (beaconRequest.getDatasetIds() != null && !beaconRequest.getDatasetIds().isEmpty()) {
            beaconRequest.getDatasetIds().forEach((dataset) -> {
                beaconUriBuilder.queryParam("datasetIds", dataset);
            });
        }

        return beaconUriBuilder.build()
            .toUri();
    }

}
