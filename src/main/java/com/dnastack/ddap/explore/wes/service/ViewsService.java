package com.dnastack.ddap.explore.wes.service;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieValue;
import dam.v1.DamService.GetFlatViewsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieKind;

@Component
public class ViewsService {

    private Map<String, ReactiveDamClient> damClients;
    @Autowired
    public ViewsService(Map<String, ReactiveDamClient> damClients) {
        this.damClients = damClients;
    }

    public Mono<Map<String, Set<String>>> getRelevantViewsForUrlsInDam(String damId,
                                                              String realm,
                                                              Map<String, GetFlatViewsResponse.FlatView> flatViews,
                                                              List<String> uniqueUrls) {

        Map<String, Set<String>> views = new HashMap<>();
        uniqueUrls.forEach(url -> {
            Set<String> viewsForUrl = new HashSet<>();
            for (Map.Entry<String, GetFlatViewsResponse.FlatView> entry : flatViews.entrySet()) {
                GetFlatViewsResponse.FlatView flatView = entry.getValue();
                String interfaceUri = flatView.getInterfaceUri();

                if (!interfaceUri.endsWith("/") && url.length() > interfaceUri.length()) {
                    interfaceUri = interfaceUri + "/";
                }

                if (url.startsWith(interfaceUri)) {
                    UriTemplate template = new UriTemplate("{damId};{resourceId}/views/{viewId}/roles/{roleId}");

                    final Map<String, Object> variables = new HashMap<>();
                    variables.put("damId", damId);
                    variables.put("resourceId", flatView.getResourceName());
                    variables.put("viewId", flatView.getViewName());
                    variables.put("roleId", flatView.getRoleName());
                    viewsForUrl.add(template.expand(variables).toString());
                }
            }
            views.put(url, viewsForUrl);
        });
        return Mono.just(views);
    }

    public Mono<Set<String>> getRelevantViewsForUrlInAllDams(String realm,
                                                             String resourceUrl,
                                                             Map<CookieKind, CookieValue> tokens) {
        return Flux.fromStream(damClients.entrySet().stream()).flatMap(clientEntry -> {
            String damId = clientEntry.getKey();
            ReactiveDamClient damClient = clientEntry.getValue();
            return damClient.getFlattenedViews(realm,
                    tokens.get(CookieKind.DAM).getClearText(),
                    tokens.get(CookieKind.REFRESH).getClearText())
                    .flatMap(flatViews ->
                            getRelevantViewsForUrlsInDam(damId, realm, flatViews, List.of(resourceUrl))
                    );
        }).collectList().flatMap(viewsForAllDams -> {
            final Set<String> finalViews = new HashSet<>();
            for (Map<String, Set<String>> viewsForDam : viewsForAllDams) {
                for (Map.Entry<String, Set<String>> entry : viewsForDam.entrySet()){
                    finalViews.addAll(entry.getValue());
                }
            }
            return Mono.just(finalViews);
        });
    }
}
