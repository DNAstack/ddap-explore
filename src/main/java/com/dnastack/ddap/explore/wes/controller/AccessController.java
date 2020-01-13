package com.dnastack.ddap.explore.wes.controller;

import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieValue;
import com.dnastack.ddap.explore.wes.service.ViewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import static com.dnastack.ddap.common.security.UserTokenCookiePackager.CookieKind;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/{realm}/access/gcs")
public class AccessController {
    private final UserTokenCookiePackager cookiePackager;
    private final ViewsService viewsService;

    public AccessController(UserTokenCookiePackager cookiePackager,
                            ViewsService viewsService) {
        this.cookiePackager = cookiePackager;
        this.viewsService = viewsService;
    }

    @GetMapping("/{bucketName}/{*filePath}")
    public Mono<Void> getAccessToResource(@PathVariable String bucketName,
                                     @PathVariable String realm,
                                     @PathVariable String filePath,
                                     ServerHttpRequest request,
                                     ServerHttpResponse response) throws URISyntaxException {
        Map<CookieKind, CookieValue> tokens = cookiePackager.extractRequiredTokens(request,
                                                                                                           Set.of(CookieKind.DAM, CookieKind.REFRESH));
        URI bucketUri = new URI("https://storage.cloud.google.com/" + bucketName + filePath);

        return getViews(realm, bucketName, tokens).flatMap(views -> {
            if(views.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "There are no views associated with the resource");
            }
            return Mono.empty();
//            try {
//                return getAccessAndRedirect(views, tokens, realm, bucketUri, response);
//            } catch (URISyntaxException e) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bucket URI");
//            }
        });
    }

//    private Mono<Void> getAccessAndRedirect(Set<String> views,
//                                            Map<CookieKind, CookieValue> tokens,
//                                            String realm,
//                                            URI bucketUri,
//                                            ServerHttpResponse response) throws URISyntaxException {
//        List<String> uniqueViews = new ArrayList<>(new HashSet<>(views));
//        return viewsService.authorizeViews(uniqueViews, tokens, realm)
//                .collectList()
//                .flatMap(viewAuthorizationResponses -> {
//                    if(viewAuthorizationResponses.isEmpty()){
//                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
//                                "You are not allowed to access this resource");
//                    }
//                    ResourceToken tokenResponse = viewAuthorizationResponses.get(0).getLocationAndToken();
//                    if(tokenResponse != null) {
//                        String accessToken = tokenResponse.getToken();
//                        log.info("Redirecting to {} with access token", bucketUri);
//                        URI updatedBucketURI = UriComponentsBuilder.fromUri(bucketUri)
//                                .queryParam("access_token", accessToken)
//                                .build()
//                                .toUri();
//                        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
//                        response.getHeaders().setLocation(updatedBucketURI);
//                        return response.setComplete();
//                    } else {
//                        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
//                    }
//                });
//    }

    private Mono<Set<String>> getViews(String realm,
                                       String bucketName,
                                       Map<CookieKind, CookieValue> tokens) {
        String bucketUrl = "gs://" + bucketName;
        return viewsService.getRelevantViewsForUrlInAllDams(realm, bucketUrl, tokens);
    }
}
