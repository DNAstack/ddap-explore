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
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

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

        Mono<Map<String, String>> drsUrisByGcsAccessMethodUri =
                Flux.fromStream(drsUrls.stream()
                                       .map(URI::create)
                                       .map(drsServerUrl -> Map.entry(drsServerUrl, drsClient.getDrsObject(drsServerUrl))))
                    .flatMap((e) -> e.getValue()
                                     .map(drsObject -> Map.entry(e.getKey(), drsObject)))
                    .map(e -> {
                        final URI drsUri = e.getKey();
                        final DrsObjectModel drsObject = e.getValue();
                        // Skip if there is no access method for GS
                        if (drsObject.getAccessMethods() == null) {
                            log.debug("No access methods for DRS Object with id {}", drsObject.getId());
                        }

                        // TODO: what if there are more GS accesses?
                        return Optional.ofNullable(drsObject.getAccessMethods())
                                       .stream()
                                       .flatMap(Collection::stream)
                                       .filter((accessMethod) -> accessMethod.getType().equalsIgnoreCase("gs"))
                                       .map(DrsObjectModel.AccessMethod::getAccessUrl)
                                       .map(DrsObjectModel.AccessUrl::getUrl)
                                       .map(url -> Map.entry(url, drsUri.toString()))
                                       .findFirst();
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collectMap(Entry::getKey, Entry::getValue);

        return drsUrisByGcsAccessMethodUri
                .flatMap((gsUrlsFromDrsObjects) -> {
                    final Set<String> allGsUrls = new HashSet<>(otherUrls);
                    allGsUrls.addAll(gsUrlsFromDrsObjects.keySet());

                    return Flux.fromStream(damClients
                                                   .entrySet()
                                                   .stream())
                               .flatMap(clientEntry -> {
                                   String damId = clientEntry.getKey();
                                   ReactiveDamClient damClient = clientEntry.getValue();
                                   return damClient.getFlattenedViews(realm)
                                                   .flatMap(flatViews -> {
                                                       return viewsService.getRelevantViewsForUrlsInDam(damId, realm, flatViews, new ArrayList<>(allGsUrls));
                                                   })
                                                   .map(Map::entrySet);
                               })
                               .map(set -> set.stream()
                                              .collect(groupViewCoordinatesByUrl())
                                              .entrySet()
                                              .stream())
                               .flatMap(Flux::fromStream)
                               .map(e -> {
                                   if (gsUrlsFromDrsObjects.containsKey(e.getKey())) {
                                       return Map.entry(gsUrlsFromDrsObjects.get(e.getKey()), e.getValue());
                                   } else {
                                       return e;
                                   }
                               })
                            .collect(groupViewCoordinatesByUrl());
                });
    }

    public Collector<Entry<String, Set<String>>, ?, Map<String, Set<String>>> groupViewCoordinatesByUrl() {
        return groupingBy(Entry::getKey,
                          mapping(Entry::getValue,
                                  reducing(Set.<String>of(), (s1, s2) -> {
                                        Set<String> s3 = new HashSet<>(s1);
                                        s3.addAll(s2);
                                        return s3;
                                    }))
        );
    }

}
