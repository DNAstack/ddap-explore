package com.dnastack.ddap.explore.apps.wes.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.apps.wes.service.DrsService;
import com.dnastack.ddap.explore.apps.wes.service.ViewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collector;

import static java.util.stream.Collectors.*;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/views")
public class ViewsController {

    private final ViewsService viewsService;
    private Map<String, ReactiveDamClient> damClients;
    private final DrsService drsService;

    @Autowired
    public ViewsController(Map<String, ReactiveDamClient> damClients,
                           ViewsService viewsService,
                           DrsService drsService) {
        this.damClients = damClients;
        this.viewsService = viewsService;
        this.drsService = drsService;
    }

    @PostMapping(path = "/lookup")
    public Mono<Map<String, Set<String>>> lookupViews(@PathVariable String realm,
                                                      @RequestBody List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            throw new IllegalArgumentException("Urls cannot be empty or null");
        }

        final List<String> drsUrls = urls.stream()
            .distinct()
            .filter(drsService::isDrsUri)
            .collect(toList());
        final List<String> otherUrls = urls.stream()
            .distinct()
            .filter((url) -> !drsUrls.contains(url))
            .collect(toList());

        Mono<Map<String, String>> drsUrisByGcsAccessMethodUri =
                Flux.fromStream(drsUrls.stream())
                    .flatMap(drsUri ->
                                     drsService.resolveAccessMethods(URI.create(drsUri), "gs")
                                               // TODO: what if there are more GS accesses?
                                               .flatMap(list -> {
                                                            if (list.size() > 1) {
                                                                return Mono.error(new UnsupportedOperationException("Cannot resolve DRS URI with multiple GS access methods"));
                                                            }
                                                            return Mono.just(list.stream()
                                                                                 .sorted()
                                                                                 .findFirst()
                                                                                 .stream()
                                                                                 .map(resolvedUri -> Map.entry(resolvedUri, drsUri)));
                                                        }
                                               )
                    )
                    .flatMap(Flux::fromStream)
                    .collectMap(Entry::getKey, Entry::getValue);

        return drsUrisByGcsAccessMethodUri
                .flatMap((gsUrlsFromDrsObjects) -> {
                    final Set<String> allGsUrls = new HashSet<>(otherUrls);
                    allGsUrls.addAll(gsUrlsFromDrsObjects.keySet());

                    return Flux.fromStream(damClients.entrySet()
                                                     .stream())
                               .flatMap(clientEntry -> {
                                   String damId = clientEntry.getKey();
                                   ReactiveDamClient damClient = clientEntry.getValue();
                                   return damClient.getFlattenedViews(realm)
                                                   .flatMap(flatViews -> viewsService.getRelevantViewsForUrlsInDam(damId, realm, flatViews, new ArrayList<>(allGsUrls)))
                                                   .map(Map::entrySet);
                               })
                               .flatMap(set -> Flux.fromStream(set.stream()))
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
