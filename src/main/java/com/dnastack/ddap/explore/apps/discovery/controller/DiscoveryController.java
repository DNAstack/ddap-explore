package com.dnastack.ddap.explore.apps.discovery.controller;

import com.dnastack.ddap.common.util.http.XForwardUtil;
import com.dnastack.ddap.explore.apps.discovery.client.ReactiveDiscoveryBeaconClient;
import com.dnastack.ddap.explore.apps.discovery.model.DiscoveryBeacon;
import com.dnastack.ddap.explore.apps.discovery.model.DiscoveryBeaconQueryResult;
import com.dnastack.ddap.explore.apps.discovery.model.DiscoveryBeaconRequestModel;
import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.Id.InterfaceId;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import com.dnastack.ddap.explore.resource.service.ResourceClientService;
import com.dnastack.ddap.explore.resource.service.UserCredentialService;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1beta/{realm}/apps/discovery")
public class DiscoveryController {


    private final UserCredentialService userCredentialService;
    private final ResourceClientService resourceClientService;
    private final ReactiveDiscoveryBeaconClient beaconClient;

    @Autowired
    public DiscoveryController(UserCredentialService userCredentialService, ResourceClientService resourceClientService, ReactiveDiscoveryBeaconClient beaconClient) {
        this.userCredentialService = userCredentialService;
        this.beaconClient = beaconClient;
        this.resourceClientService = resourceClientService;
    }


    @GetMapping("/beacon")
    public Mono<DiscoveryBeacon> getBeaconInfo(ServerHttpRequest httpRequest, WebSession session, @PathVariable String realm, @RequestParam String resource) {
        return Mono.defer(() -> {
            InterfaceId beaconId = Id.decodeInterfaceId(resource);
            return getAccessInterfaceForBeacon(realm, beaconId)
                .flatMap(accessInterface -> beaconClient.getBeaconInfo(accessInterface.getUri()));
        });
    }

    @GetMapping("/beacon/query")
    public Mono<DiscoveryBeaconQueryResult> querySingleBeacon(ServerHttpRequest httpRequest, WebSession session, @PathVariable String realm, @RequestParam(value = "resource") String interfaceId, DiscoveryBeaconRequestModel beaconRequest) {
        return Mono.defer(() -> {
            InterfaceId beaconId = Id.decodeInterfaceId(interfaceId);
            validateBeaconRequest(beaconRequest);
            return getAccessInterfaceForBeacon(realm, beaconId).flatMap(accessInterface -> {
                BeaconAccess beaconAccess = new BeaconAccess();
                beaconAccess.setAccessInterface(accessInterface);
                if (accessInterface.isAuthRequired()) {
                    Optional<UserCredential> credential = userCredentialService
                        .getAndDecryptCredentialsForResourceInterface(httpRequest, session, beaconId);
                    if (!credential.isPresent()) {
                        URI authorizeUriBase = URI.create(XForwardUtil
                            .getExternalPath(httpRequest, String
                                .format("/api/v1beta/%s/resources/authorize", realm)));
                        URI authorizationUrl = UriComponentsBuilder.fromUri(authorizeUriBase)
                            .queryParam("resource", interfaceId)
                            .build().toUri();
                        DiscoveryBeaconQueryResult queryResult = new DiscoveryBeaconQueryResult();
                        queryResult.setRequiresAdditionalAuth(true);
                        queryResult.setAuthorizationUrlBase(authorizationUrl);
                        return Mono.just(queryResult);
                    } else {
                        beaconAccess.setUserCredential(credential.get());
                    }
                }
                return beaconClient
                    .queryBeacon(beaconRequest, beaconAccess.getAccessInterface()
                        .getUri(), extractAuthToken(beaconAccess
                        .getUserCredential()));
            }).doOnNext(result -> {
                if (result.getError() != null && List.of(401, 403).contains(result.getError().getErrorCode())) {
                    log.debug("User request was not authorized, cleaning up stale credentials");
                    userCredentialService.deleteCredential(session, interfaceId);
                }
            });
        });


    }


    private Mono<AccessInterface> getAccessInterfaceForBeacon(String realm, InterfaceId beaconId) {
        return resourceClientService.getClient(beaconId.getSpiKey()).getResource(realm, beaconId.toResourceId())
            .map(beaconResource -> {
                AccessInterface accessInterface = beaconResource.getInterfaces().stream()
                    .filter(beaconInterface -> beaconInterface.getId().equals(beaconId.encodeId())).findFirst()
                    .orElseThrow(() -> new NotFoundException(
                        "Could not find Interface for beacon resource with id: " + beaconId.encodeId()));
                return accessInterface;
            });
    }

    private String extractAuthToken(UserCredential credential) {
        if (credential == null) {
            return null;
        }
        return credential.getCredentials().get("access_token");

    }

    private void validateBeaconRequest(DiscoveryBeaconRequestModel beaconRequest) {
        final Pattern validBasesPattern = Pattern.compile("N|[ACGT]+");
        if (beaconRequest == null) {
            throw new IllegalArgumentException("Invalid Beacon Request, missing request body");
        }
        if (beaconRequest.getAlternateBases() == null) {
            throw new IllegalArgumentException("Invalid Beacon Request, missing required parameter \"alternateBases\"");
        } else if (!validBasesPattern.matcher(beaconRequest.getAlternateBases()).matches()) {

            throw new IllegalArgumentException("Invalid Beacon Request, illegal bases in alternate - expecting a pattern matching \"(N|[ACGT]+)\"");
        }
        if (beaconRequest.getReferenceBases() == null) {
            throw new IllegalArgumentException("Invalid Beacon Request, missing required parameter \"referenceBases\"");
        } else if (!validBasesPattern.matcher(beaconRequest.getReferenceBases()).matches()) {

            throw new IllegalArgumentException("Invalid Beacon Request, illegal bases in reference - expecting a pattern matching \"(N|[ACGT]+)\"");
        }
        if (beaconRequest.getReferenceName() == null) {
            throw new IllegalArgumentException("Invalid Beacon Request, missing required parameter \"referenceName\"");
        }
        if (beaconRequest.getStart() == null) {
            throw new IllegalArgumentException("Invalid Beacon Request, missing required parameter \"start\"");
        } else {
            try {
                Long.parseLong(beaconRequest.getStart());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Invalid beacon Request, \"Start\" is not an integer - " + e.getMessage(), e);
            }
        }
    }

    @Data
    private static class BeaconAccess {

        AccessInterface accessInterface;
        UserCredential userCredential;
    }

}
