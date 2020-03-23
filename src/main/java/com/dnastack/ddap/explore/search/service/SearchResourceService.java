package com.dnastack.ddap.explore.search.service;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.search.model.SearchResourceResponseModel;
import dam.v1.DamService.GetFlatViewsResponse.FlatView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            .map(this::toSearchResponseModels)
            .reduce((list1, list2) -> {
                return Stream.concat(list1.stream(), list2.stream())
                    .collect(Collectors.toList());
            });
    }

    private Flux<Map<String, FlatView>> getAllFlattenedViews(String realm) {
        return Flux.fromIterable(damClients.values()
            .stream()
            .map((damClient) -> damClient.getFlattenedViews(realm))
            .collect(Collectors.toList()))
            .flatMap(mono -> mono);
    }

    private List<SearchResourceResponseModel> toSearchResponseModels(Map<String, FlatView> views) {
        return views.values()
            .stream()
            .filter(this::isSearchView)
            .map((view) -> SearchResourceResponseModel.builder()
                    .resourcePath(view.getResourcePath())
                    .viewName(view.getViewName())
                    .resourceName(view.getResourceName())
                    .roleName(Optional.of(view.getRoleName()))
                    .interfaceName(Optional.of(view.getInterfaceName()))
                    .ui(Map.of(
                    "label", view.getViewUiMap().get("label"),
                    "description", view.getViewUiMap().get("description")
                    ))
                    .build())
            .collect(Collectors.toList());
    }

    private boolean isSearchView(FlatView view) {
        return view.getInterfaceName().equalsIgnoreCase("http:search")
            || view.getInterfaceName().equalsIgnoreCase("https:search");
    }

    public Mono<URI> lookupFirstInterfaceUrlByResourcePath(String realm, String resourcePath) {
        return getAllFlattenedViews(realm)
            .map((flatViews) -> {
                return flatViews.values()
                    .stream()
                    .filter((view) -> view.getResourcePath().equals(resourcePath))
                    .map(FlatView::getInterfaceUri)
                    .map(URI::create)
                    .findFirst();
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce((uri1, uri2) -> {
                return uri1;
            });
    }

    public Mono<SearchResourceResponseModel> getSearchResourceViews(String resourceName, String realm) {
        return getAllFlattenedViews(realm)
                .map((flatViews) -> {
                    return flatViews.values()
                            .stream()
                            .filter(this::isSearchView)
                            .filter((view) -> view.getResourceName().equals(resourceName))
                            .findFirst();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(view -> {
                    return SearchResourceResponseModel.builder()
                            .resourceName(view.getResourceName())
                            .viewName(view.getViewName())
                            .roleName(Optional.of(view.getRoleName()))
                            .interfaceName(Optional.of(view.getInterfaceName()))
                            .resourcePath(view.getResourcePath())
                            .ui(Map.of(
                                    "label", view.getViewUiMap().get("label"),
                                    "description", view.getViewUiMap().get("description")
                            ))
                            .build();
                })
                .next();
//        return null;
    }
}
