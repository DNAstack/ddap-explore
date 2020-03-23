package com.dnastack.ddap.explore.wes.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.wes.client.ReactiveDrsClient;
import com.dnastack.ddap.explore.wes.model.DrsObjectModel;
import com.dnastack.ddap.explore.wes.service.ViewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/views")
public class ViewsController {

    private final ViewsService viewsService;
    private Map<String, ReactiveDamClient> damClients;
    private ReactiveDrsClient drsClient;

    @Autowired
    public ViewsController(Map<String, ReactiveDamClient> damClients,
                           ViewsService viewsService,
                           ReactiveDrsClient drsClient) {
        this.damClients = damClients;
        this.viewsService = viewsService;
        this.drsClient = drsClient;
    }

    @PostMapping(path = "/lookup")
    public Mono<Map<String, Set<String>>> lookupViews(@PathVariable String realm,
                                                      @RequestBody List<String> urls,
                                                      ServerHttpRequest request) {
        if (urls == null || urls.isEmpty()) {
            throw new IllegalArgumentException("Urls cannot be empty or null");
        }

        final List<String> drsUrls = urls.stream()
            .distinct()
            .filter((url) -> url.contains("/ga4gh/drs/"))
            .collect(toList());
        final List<String> otherUrls = urls.stream()
            .distinct()
            .filter((url) -> !drsUrls.contains(url))
            .collect(toList());

        Mono<List<String>> gsUrlsFromDrsObjectsMono = Flux.fromStream(drsUrls.stream()
            .map(URI::create)
            .map(drsClient::getDrsObject))
            .flatMap((mono) -> mono)
            .map((drsObject) -> {
                // Skip if there is no access method for GS
                if (drsObject.getAccessMethods() == null) {
                    log.debug("No access methods for DRS Object with id {}", drsObject.getId());
                    return Optional.<String>empty();
                }

                // TODO: what if there are more GS accesses?
                return drsObject.getAccessMethods()
                    .stream()
                    .filter((accessMethod) -> accessMethod.getType().equalsIgnoreCase("gs"))
                    .map(DrsObjectModel.AccessMethod::getAccessUrl)
                    .map(DrsObjectModel.AccessUrl::getUrl)
                    .findFirst();
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collectList();

        return gsUrlsFromDrsObjectsMono
            .flatMap((gsUrlsFromDrsObjects) -> {
                final Set<String> allGsUrls = new HashSet<>(otherUrls);
                allGsUrls.addAll(gsUrlsFromDrsObjects);

                return Flux.fromStream(damClients
                    .entrySet()
                    .stream())
                    .flatMap(clientEntry -> {
                        String damId = clientEntry.getKey();
                        ReactiveDamClient damClient = clientEntry.getValue();
                        return damClient.getFlattenedViews(realm)
                            .flatMap(flatViews -> {
                                return viewsService.getRelevantViewsForUrlsInDam(damId, realm, flatViews, new ArrayList<>(allGsUrls));
                            });
                    })
                    .collectList()
                    .flatMap(viewsForAllDams -> {
                        final Map<String, Set<String>> finalViewListing = new HashMap<>();

                        for (Map<String, Set<String>> viewsForDam : viewsForAllDams) {
                            for (Map.Entry<String, Set<String>> entry : viewsForDam.entrySet()) {
                                if (finalViewListing.containsKey(entry.getKey())) {
                                    finalViewListing.get(entry.getKey()).addAll(entry.getValue());
                                } else {
                                    finalViewListing.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                        return Mono.just(finalViewListing);
                    });
            });
    }

}
