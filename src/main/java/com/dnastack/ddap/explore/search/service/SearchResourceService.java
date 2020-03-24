package com.dnastack.ddap.explore.search.service;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.search.model.SearchResourceResponseModel;
import dam.v1.DamService.GetFlatViewsResponse.FlatView;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class SearchResourceService {

    private Map<String, ReactiveDamClient> damClients;

    @Autowired
    public SearchResourceService(Map<String, ReactiveDamClient> damClients) {
        this.damClients = damClients;
    }

    public Mono<List<SearchResourceResponseModel>> getSearchResources(String realm) {
        return getAllFlattenedViews(realm)
                .filter(this::isSearchView)
                .map((view) -> SearchResourceResponseModel.builder()
                                                          .damId(view.getDamId())
                                                          .resourcePath(view.getResourcePath())
                                                          .viewName(view.getFlatView().getViewName())
                                                          .resourceName(view.getFlatView().getResourceName())
                                                          .roleName(Optional.of(view.getFlatView().getRoleName()))
                                                          .interfaceName(Optional.of(view.getFlatView().getInterfaceName()))
                                                          .ui(Map.of(
                                                                  "label", view.getFlatView().getViewUiMap().get("label"),
                                                                  "description", view.getFlatView().getViewUiMap().get("description")
                                                          ))
                                                          .build())
                .collect(toList());
    }

    private Flux<FlatViewMetadata> getAllFlattenedViews(String realm) {
        return Flux.fromStream(damClients.entrySet().stream())
                   .flatMap(clientEntry -> clientEntry.getValue().getFlattenedViews(realm)
                                                      .map(flatViewMap -> flatViewMap.entrySet()
                                                                                     .stream()
                                                                                     .map(e -> new FlatViewMetadata(clientEntry.getKey(), e.getKey(), e.getValue()))))
                   .flatMap(Flux::fromStream);
    }

    private boolean isSearchView(FlatViewMetadata viewMetadata) {
        return isSearchView(viewMetadata.getFlatView());
    }

    private boolean isSearchView(FlatView view) {
        return view.getInterfaceName().equalsIgnoreCase("http:search")
            || view.getInterfaceName().equalsIgnoreCase("https:search");
    }

    public Mono<URI> lookupFirstInterfaceUrlByResourcePath(String realm, String resourcePath) {
        return Mono.from(getAllFlattenedViews(realm)
                                 .filter((view) -> view.getFlatView().getResourcePath().equals(resourcePath))
                                 .map((viewMeta) -> viewMeta.getFlatView().getInterfaceUri())
                                 .map(URI::create)
                                 .take(1));
    }

    public Mono<List<SearchResourceResponseModel>> getSearchResourceViews(String resourceName, String realm) {
        return getAllFlattenedViews(realm)
                .filter((view) -> view.getFlatView().getResourceName().equals(resourceName))
                .map(view -> {
                    return SearchResourceResponseModel.builder()
                                                      .damId(view.getDamId())
                                                      .resourceName(view.getFlatView().getResourceName())
                                                      .viewName(view.getFlatView().getViewName())
                                                      .roleName(Optional.of(view.getFlatView().getRoleName()))
                                                      .interfaceName(Optional.of(view.getFlatView().getInterfaceName()))
                                                      .resourcePath(view.getFlatView().getResourcePath())
                                                      .isSearchView(Optional.of(isSearchView(view)))
                                                      .ui(Map.of(
                                                              "label", view.getFlatView().getViewUiMap().get("label"),
                                                              "description", view.getFlatView().getViewUiMap().get("description")
                                                      ))
                                                      .build();
                })
                .collect(toList());
    }

    @Value
    private static class FlatViewMetadata {
        private String damId;
        private String resourcePath;
        private FlatView FlatView;
    }
}
