package com.dnastack.ddap.explore.wes.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import com.dnastack.ddap.explore.wes.service.ViewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieKind;

@RestController
@RequestMapping("/api/v1alpha/{realm}/views")
public class ViewsController {

    private final UserTokenCookiePackager cookiePackager;
    private final ViewsService viewsService;
    private Map<String, ReactiveDamClient> damClients;

    @Autowired
    public ViewsController(Map<String, ReactiveDamClient> damClients,
                           UserTokenCookiePackager cookiePackager,
                           ViewsService viewsService) {
        this.cookiePackager = cookiePackager;
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

        Map<CookieKind, UserTokenCookiePackager.CookieValue> tokens = cookiePackager.extractRequiredTokens(request, Set.of(CookieKind.DAM, CookieKind.REFRESH));
        return Flux.fromStream(damClients.entrySet().stream()).flatMap(clientEntry -> {
            String damId = clientEntry.getKey();
            ReactiveDamClient damClient = clientEntry.getValue();
            // TODO: Handle error when token is empty
            return damClient.getFlattenedViews(realm, tokens.get(CookieKind.DAM).getClearText(), tokens.get(CookieKind.REFRESH).getClearText())
                    .flatMap(flatViews -> viewsService.getRelevantViewsForUrlsInDam(damId, realm, flatViews, uniqueUrls));
        }).collectList().flatMap(viewsForAllDams -> {
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
