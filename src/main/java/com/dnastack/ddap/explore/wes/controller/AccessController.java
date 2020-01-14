package com.dnastack.ddap.explore.wes.controller;

import com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieValue;
import com.dnastack.ddap.explore.wes.service.ViewsService;
import dam.v1.DamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieKind;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/{realm}/access/gcs")
public class AccessController {
    private final ViewsService viewsService;

    public AccessController(ViewsService viewsService) {
        this.viewsService = viewsService;
    }

    @GetMapping("/{bucketName}/{*filePath}")
    public Mono<Void> getAccessToResource(@PathVariable String bucketName,
                                     @PathVariable String realm,
                                     @PathVariable String filePath,
                                     ServerHttpRequest request,
                                     ServerHttpResponse response) throws URISyntaxException {
        URI bucketUri = new URI("https://storage.cloud.google.com/" + bucketName + filePath);

        return getViews(realm, bucketName).flatMap(views -> {
            if(views.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "There are no views associated with the resource");
            }
            try {
                return getAccessAndRedirect(views, tokens, realm, bucketUri, response);
            } catch (URISyntaxException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bucket URI");
            }
        });
    }

    private Mono<Set<String>> getViews(String realm, String bucketName) {
        String bucketUrl = "gs://" + bucketName;
        return viewsService.getRelevantViewsForUrlInAllDams(realm, bucketUrl);
    }
}
