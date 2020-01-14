package com.dnastack.ddap.explore.wes.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.wes.service.ViewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/{realm}/views")
public class ViewsController {

    private final ViewsService viewsService;
    private Map<String, ReactiveDamClient> damClients;

    @Autowired
    public ViewsController(Map<String, ReactiveDamClient> damClients, ViewsService viewsService) {
        this.damClients = damClients;
        this.viewsService = viewsService;
    }

    @PostMapping(path = "/lookup")
    public Mono<Map<String, Set<String>>> lookupViews(@PathVariable String realm,
                                                      @RequestBody List<String> urls,
                                                      ServerHttpRequest request) {
        if (urls == null || urls.isEmpty()) {
            throw new IllegalArgumentException("Urls cannot be empty or null");
        }
        final List<String> uniqueUrls = new ArrayList<>(new HashSet<>(urls));

        return Flux.fromStream(damClients
            .entrySet()
            .stream())
            .flatMap(clientEntry -> {
                String damId = clientEntry.getKey();
                ReactiveDamClient damClient = clientEntry.getValue();
                return damClient.getFlattenedViews(realm)
                    .flatMap(flatViews -> {
                        log.warn("FLATTEN VIEWS: {}", flatViews);
                        return viewsService.getRelevantViewsForUrlsInDam(damId, realm, flatViews, uniqueUrls);
                    });
            })
            .collectList()
            .flatMap(viewsForAllDams -> {
                log.warn("VIEWS: {}", viewsForAllDams);
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
    }

}
