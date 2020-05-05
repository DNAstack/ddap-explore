package com.dnastack.ddap.explore.apps.search.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.common.util.http.XForwardUtil;
import com.dnastack.ddap.explore.apps.search.exception.SearchAuthorizationException;
import com.dnastack.ddap.explore.apps.search.exception.SearchResourceException;
import com.dnastack.ddap.explore.apps.search.model.AggregateListTable;
import com.dnastack.ddap.explore.apps.search.model.ListTables;
import com.dnastack.ddap.explore.apps.search.model.Pagination;
import com.dnastack.ddap.explore.apps.search.model.SearchAuthRequest;
import com.dnastack.ddap.explore.apps.search.model.SearchRequest;
import com.dnastack.ddap.explore.apps.search.model.TableData;
import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.Id.InterfaceId;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import com.dnastack.ddap.explore.resource.service.ResourceClientService;
import com.dnastack.ddap.explore.resource.service.UserCredentialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1beta/{realm}/apps/search")
public class SearchV1BetaController {


    private final UserCredentialService userCredentialService;
    private final ResourceClientService resourceClientService;
    private final ObjectMapper mapper;


    @Autowired
    public SearchV1BetaController(UserCredentialService userCredentialService, ResourceClientService resourceClientService, ObjectMapper mapper) {
        this.userCredentialService = userCredentialService;
        this.resourceClientService = resourceClientService;
        this.mapper = mapper;
    }


    @GetMapping("/tables")
    public Mono<AggregateListTable> getTables(ServerHttpRequest httpRequest, WebSession session, @PathVariable String realm) {
        return Mono.defer(() -> {
            return resourceClientService.listResources(realm, null, List.of("http:search"), null)
                .flatMapMany(Flux::fromIterable)
                .flatMap(resource -> {
                    Optional<AccessInterface> accessInterfaceOptional = resource.getInterfaces().stream()
                        .filter(i -> i.getType().equals("http:search"))
                        .findFirst();
                    if (accessInterfaceOptional.isEmpty()) {
                        return Mono.empty();
                    }

                    final AccessInterface accessInterface = accessInterfaceOptional.get();
                    final String accessToken = getAccessTokenForInterface(httpRequest, session, realm, accessInterface);

                    if (accessInterface.isAuthRequired() && accessToken == null) {
                        ListTables result = constructListResultRequiringAuthorization(httpRequest, realm, List
                            .of(accessInterface.getId()));
                        result.setResource(resource);
                        return Mono.just(result);
                    }

                    URI tablesUri = UriComponentsBuilder.fromUri(accessInterface.getUri()).pathSegment("tables")
                        .build().toUri();
                    ParameterizedTypeReference<ListTables> typeReference = new ParameterizedTypeReference<>() {
                    };
                    return retrieveSearchResource(httpRequest, session, accessInterface, tablesUri, typeReference, accessToken, null)
                        .onErrorResume(throwable -> Mono
                            .just(handleException(httpRequest, throwable, realm, this::constructListResultRequiringAuthorization)))
                        .map(listTables -> {
                            listTables.setResource(resource);
                            return listTables;
                        });
                }).reduce(new AggregateListTable(), (identity, current) -> {
                    List<ListTables> tableResources = identity.getTableResources();
                    if (tableResources == null) {
                        tableResources = new ArrayList<>();
                        identity.setTableResources(tableResources);
                    }

                    tableResources.add(current);
                    return identity;
                }).map(aggregateListTable -> {
                    List<String> additionalIdsToAuthorize = aggregateListTable.getTableResources().stream()
                        .filter(ListTables::isRequiresAdditionalAuth)
                        .map(ListTables::getInterfaceIdsForAdditionalAuth)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
                    if (!additionalIdsToAuthorize.isEmpty()) {
                        UriComponentsBuilder componentsBuilder = getAuthorizationUriBuilder(httpRequest, realm);
                        for (String interfaceId : additionalIdsToAuthorize) {
                            componentsBuilder.queryParam("resource", interfaceId);
                        }
                        aggregateListTable.setAuthorizationUrlBase(componentsBuilder.build().toUri());
                    }
                    return aggregateListTable;
                });
        });
    }

    @GetMapping("/tables/data-models")
    public Mono<Map<String, Object>> resolveDataModel(ServerHttpRequest httpRequest, WebSession session, @PathVariable String realm, @RequestParam("resource") String interfaceIdString, @RequestParam("data_model_uri") URI dataModelUri) {
        return Mono.defer(() -> {
            InterfaceId interfaceId = Id.decodeInterfaceId(interfaceIdString);
            return getAccessInterfaceFor(realm, interfaceId)
                .flatMap(accessInterface -> {
                    final URI absoluteDataModelUri = accessInterface.getUri().resolve(dataModelUri);
                    if (absoluteDataModelUri.getHost().equals(accessInterface.getUri().getHost())) {
                        return Mono.just(accessInterface);
                    } else {
                        return getFirstAccessInterfaceForURI(realm, absoluteDataModelUri)
                            .switchIfEmpty(Mono.just(new AccessInterface(null, absoluteDataModelUri, null, false)));
                    }
                }).flatMap(accessInterface -> {
                    final URI absoluteDataModelUri = accessInterface.getUri().resolve(dataModelUri);
                    String accessToken = null;
                    if (accessInterface.isAuthRequired()) {
                        accessToken = getAccessTokenForInterface(httpRequest, session, realm, accessInterface);
                        if (accessToken == null) {
                            Map<String, Object> response = new HashMap<>();
                            UriComponentsBuilder componentsBuilder = getAuthorizationUriBuilder(httpRequest, realm);
                            componentsBuilder.queryParam("resource", accessInterface.getId());
                            response.put("requiresAdditionalAuth", true);
                            response.put("authorizationUrlBase", componentsBuilder.build().toUri());
                            return Mono.just(response);
                        }
                    }

                    ParameterizedTypeReference<Map<String, Object>> typeReference = new ParameterizedTypeReference<>() {
                    };
                    return retrieveSearchResource(httpRequest, session, accessInterface, absoluteDataModelUri, typeReference, accessToken, null)
                        .onErrorResume(throwable -> Mono
                            .just(handleException(httpRequest, throwable, realm, this::contstructDataModelResultRequiredAuth)));

                });
        });
    }


    @GetMapping(value = "/query/results")
    public Mono<TableData> getSearchPage(ServerHttpRequest httpRequest, WebSession session, @PathVariable String realm, @RequestParam("resource") String searchInterfaceId, @RequestParam(value = "next_page_token", required = false) String nextPageToken, @RequestParam(value = "previous_page_token", required = false) String previousPageToken) {
        return Mono.defer(() -> {
            InterfaceId interfaceId = Id.decodeInterfaceId(searchInterfaceId);
            if (!interfaceId.getType().startsWith("http:search")) {
                throw new IllegalArgumentException("Resource: " + searchInterfaceId
                    + ", has invalid interface type for search. Must be \"http:search\"");
            }

            if (nextPageToken == null && previousPageToken == null) {
                throw new IllegalArgumentException("No page token provided for retrieving the next page");
            }

            URI pageToRetrieve;
            if (nextPageToken != null) {
                pageToRetrieve = URI
                    .create(new String(Base64.getDecoder().decode(nextPageToken.getBytes()), StandardCharsets.UTF_8));
            } else {
                pageToRetrieve = URI.create(new String(Base64.getDecoder()
                    .decode(previousPageToken.getBytes()), StandardCharsets.UTF_8));
            }

            return getAccessInterfaceFor(realm, interfaceId).flatMap(accessInterface -> {
                final String accessToken = getAccessTokenForInterface(httpRequest, session, realm, accessInterface);
                if (accessInterface.isAuthRequired() && accessToken == null) {
                    return Mono.just(constructSearchResultRequiringAuthorization(httpRequest, realm, List
                        .of(searchInterfaceId)));
                }
                URI absolutePageToRetrieve = UriComponentsBuilder.fromUri(accessInterface.getUri())
                    .pathSegment("search").build().toUri().resolve(pageToRetrieve);
                ParameterizedTypeReference<TableData> typeReference = new ParameterizedTypeReference<>() {
                };

                return retrieveSearchResource(httpRequest, session, accessInterface, absolutePageToRetrieve, typeReference, accessToken, null)
                    .onErrorResume(throwable -> Mono
                        .just(handleException(httpRequest, throwable, realm, this::constructSearchResultRequiringAuthorization)));
            });
        });
    }

    @PostMapping(value = "/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TableData> search(ServerHttpRequest httpRequest, WebSession session, @PathVariable String realm, @RequestParam("resource") String searchInterfaceId, @RequestBody SearchRequest searchRequest) {
        return Mono.defer(() -> {
            InterfaceId interfaceId = Id.decodeInterfaceId(searchInterfaceId);
            if (!interfaceId.getType().startsWith("http:search")) {
                throw new IllegalArgumentException("Resource: " + searchInterfaceId
                    + ", has invalid interface type for search. Must be \"http:search\"");
            }
            return getAccessInterfaceFor(realm, interfaceId).flatMap(accessInterface -> {
                final String accessToken = getAccessTokenForInterface(httpRequest, session, realm, accessInterface);
                if (accessInterface.isAuthRequired() && accessToken == null) {
                    return Mono.just(constructSearchResultRequiringAuthorization(httpRequest, realm, List
                        .of(searchInterfaceId)));
                }

                URI searchUri = UriComponentsBuilder.fromUri(accessInterface.getUri()).pathSegment("search")
                    .build().toUri();
                ParameterizedTypeReference<TableData> typeReference = new ParameterizedTypeReference<TableData>() {
                };
                return postSearchResource(httpRequest, session, accessInterface, searchUri, typeReference, searchRequest, accessToken, null)
                    .map(data -> setupPagination(httpRequest, realm, data, interfaceId))
                    .onErrorResume(throwable -> Mono
                        .just(handleException(httpRequest, throwable, realm, this::constructSearchResultRequiringAuthorization)));
            });
        });

    }

    private TableData setupPagination(ServerHttpRequest httpRequest, String realm, TableData tableData, InterfaceId searchInterface) {
        if (tableData.getPagination() != null) {
            Pagination pagination = tableData.getPagination();
            if (pagination.getNextPageUrl() != null) {

                pagination
                    .setNextPageUrl(getUriBuilder(httpRequest, realm, "/apps/search/query/results")
                        .queryParam("resource", searchInterface.encodeId())
                        .queryParam("next_page_token", Base64.getEncoder()
                            .encodeToString(pagination.getNextPageUrl().toString().getBytes(StandardCharsets.UTF_8)))
                        .build().toUri());
            }
            if (pagination.getPreviousPageUrl() != null) {
                pagination
                    .setPreviousPageUrl(getUriBuilder(httpRequest, realm, "/apps/search/query/results")
                        .queryParam("resource", searchInterface.encodeId())
                        .queryParam("previous_page_token", Base64.getEncoder()
                            .encodeToString(pagination.getPreviousPageUrl().toString()
                                .getBytes(StandardCharsets.UTF_8)))
                        .build().toUri());
            }
        }
        return tableData;
    }

    private String getAccessTokenForInterface(ServerHttpRequest httpRequest, WebSession session, String realm, AccessInterface accessInterface) {
        String accessToken = null;
        if (accessInterface.isAuthRequired()) {
            Optional<UserCredential> userCredentialOptional = userCredentialService
                .getAndDecryptCredentialsForResourceInterface(httpRequest, session, Id
                    .decodeInterfaceId(accessInterface.getId()));
            if (userCredentialOptional.isEmpty()) {
                return null;
            } else {
                UserCredential userCredential = userCredentialOptional.get();
                accessToken = userCredential.getAccessToken();
            }
        }
        return accessToken;
    }


    private <T> T handleException(ServerHttpRequest httpRequest, Throwable throwable, String realm, AuthExceptionRecovery<T> authRecovery) {
        if (throwable instanceof SearchAuthorizationException) {
            SearchAuthorizationException searchAuthorizationException = (SearchAuthorizationException) throwable;
            AccessInterface accessInterface = searchAuthorizationException.getAccessInterfaceToAuthorize();
            if (accessInterface == null) {
                throw new SearchResourceException(throwable.getMessage(), HttpStatus.FORBIDDEN
                    .value(), throwable);
            }
            return authRecovery.recoverOnAuthException(httpRequest, realm, List.of(accessInterface.getId()));
        } else if (throwable instanceof SearchResourceException) {
            throw (SearchResourceException) throwable;
        } else {
            throw new SearchResourceException(throwable.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR
                .value(), throwable);
        }
    }


    private Mono<AccessInterface> getAccessInterfaceFor(String realm, InterfaceId interfaceId) {
        return resourceClientService.getClient(interfaceId.getSpiKey()).getResource(realm, interfaceId.toResourceId())
            .map(beaconResource -> {
                AccessInterface accessInterface = beaconResource.getInterfaces().stream()
                    .filter(beaconInterface -> beaconInterface.getId().equals(interfaceId.encodeId())).findFirst()
                    .orElseThrow(() -> new NotFoundException(
                        "Could not find Interface for resource with id: " + interfaceId.encodeId()));
                return accessInterface;
            });
    }

    private Mono<AccessInterface> getFirstAccessInterfaceForURI(String realm, URI uri) {
        return resourceClientService.listResources(realm, null, null, List.of(uri.toString()))
            .flatMap(resources -> Mono.justOrEmpty(resources.stream().findFirst()))
            .flatMap(resource -> Mono.justOrEmpty(resource.getInterfaces().stream().findFirst()));
    }

    private Map<String, Object> contstructDataModelResultRequiredAuth(ServerHttpRequest httpRequest, String realm, List<String> interfaceIds) {
        Map<String, Object> result = new HashMap<>();
        result.put("requiresAdditionalAuth", true);
        UriComponentsBuilder componentsBuilder = getAuthorizationUriBuilder(httpRequest, realm);
        for (var id : interfaceIds) {
            componentsBuilder.queryParam("resource", id);
        }
        result.put("authorizationUrlBase", componentsBuilder.build().toUri());
        return result;
    }


    private TableData constructSearchResultRequiringAuthorization(ServerHttpRequest httpRequest, String realm, List<String> interfaceIds) {
        TableData tableData = new TableData();
        tableData.setRequiresAdditionalAuth(true);
        UriComponentsBuilder componentsBuilder = getAuthorizationUriBuilder(httpRequest, realm);
        for (var id : interfaceIds) {
            componentsBuilder.queryParam("resource", id);
        }
        tableData.setAuthorizationUrlBase(componentsBuilder.build().toUri());
        return tableData;
    }

    private ListTables constructListResultRequiringAuthorization(ServerHttpRequest httpRequest, String realm, List<String> interfaceIds) {
        ListTables tableData = new ListTables();
        tableData.setInterfaceIdsForAdditionalAuth(interfaceIds);
        tableData.setRequiresAdditionalAuth(true);
        UriComponentsBuilder componentsBuilder = getAuthorizationUriBuilder(httpRequest, realm);
        for (var id : interfaceIds) {
            componentsBuilder.queryParam("resource", id);
        }
        tableData.setAuthorizationUrlBase(componentsBuilder.build().toUri());
        return tableData;
    }

    private UriComponentsBuilder getAuthorizationUriBuilder(ServerHttpRequest httpRequest, String realm) {
        return getUriBuilder(httpRequest, realm, "/resources/authorize");
    }


    private UriComponentsBuilder getUriBuilder(ServerHttpRequest httpRequest, String realm, String path) {
        URI authorizeUriBase = URI.create(XForwardUtil
            .getExternalPath(httpRequest, String.format("/api/v1beta/%s" + path, realm)));
        return UriComponentsBuilder.fromUri(authorizeUriBase);
    }


    private <T> Mono<T> retrieveSearchResource(ServerHttpRequest serverHttpRequest, WebSession webSession, AccessInterface accessInterface, URI uriToGet, ParameterizedTypeReference<T> typeReference, String accessToken, Map<String, String> ga4ghCredentials) {
        return WebClientFactory.getWebClientBuilder()
            .filter(buildAuthExchangeFilter(accessToken, ga4ghCredentials))
            .build()
            .get()
            .uri(uriToGet)
            .retrieve()
            .bodyToMono(typeReference)
            .onErrorMap(this::handleSearchAuthorizationException)
            .onErrorResume(throwable -> retryRequestWithAdditionalHeadersOr(throwable, serverHttpRequest, webSession, accessInterface, uriToGet, typeReference, null, accessToken, ga4ghCredentials));

    }

    private <T> Mono<T> postSearchResource(ServerHttpRequest serverHttpRequest, WebSession webSession, AccessInterface accessInterface, URI uriToGet, ParameterizedTypeReference<T> typeReference, Object body, String accessToken, Map<String, String> ga4ghCredentials) {
        return WebClientFactory.getWebClientBuilder()
            .filter(buildAuthExchangeFilter(accessToken, ga4ghCredentials))
            .build()
            .post()
            .uri(uriToGet)
            .body(BodyInserters.fromObject(body))
            .retrieve()
            .bodyToMono(typeReference)
            .onErrorMap(this::handleSearchAuthorizationException)
            .onErrorResume(throwable -> retryRequestWithAdditionalHeadersOr(throwable, serverHttpRequest, webSession, accessInterface, uriToGet, typeReference, body, accessToken, ga4ghCredentials));

    }

    private <T> Mono<T> retryRequestWithAdditionalHeadersOr(Throwable throwable, ServerHttpRequest serverHttpRequest, WebSession webSession, AccessInterface accessInterface, URI uriToGet, ParameterizedTypeReference<T> typeReference, Object body, String accessToken, Map<String, String> ga4ghCredentials) {

        final Map<String, String> additionalCredentials = ga4ghCredentials == null ? new HashMap<>() : ga4ghCredentials;
        if (throwable instanceof SearchAuthorizationException) {
            SearchAuthorizationException searchAuthorizationException = (SearchAuthorizationException) throwable;
            SearchAuthRequest searchAuthRequest = searchAuthorizationException.getSearchAuthRequest();
            return getAccessInterfaceForConnector(accessInterface, searchAuthRequest.getKey())
                .switchIfEmpty(Mono.error(() -> new SearchResourceException(
                    "Search Resource: " + accessInterface.getId()
                        + " requries additional authorization for its connectors, however there are no resources configured for connector: "
                        + searchAuthRequest.getKey(), HttpStatus.FORBIDDEN.value(), throwable)))
                .flatMap((connectorAccessInterface) -> {
                    if (additionalCredentials.containsKey(searchAuthRequest.getKey())) {
                        return Mono.error(new SearchAuthorizationException(connectorAccessInterface));
                    }

                    Optional<UserCredential> optionalCredential = userCredentialService
                        .getAndDecryptCredentialsForResourceInterface(serverHttpRequest, webSession, Id
                            .decodeInterfaceId(connectorAccessInterface.getId()));
                    return optionalCredential.map(credential -> {
                        additionalCredentials.put(searchAuthRequest.getKey(), credential.getAccessToken());
                        if (body != null) {
                            return postSearchResource(serverHttpRequest, webSession, accessInterface, uriToGet, typeReference, body, accessToken, additionalCredentials);
                        } else {
                            return retrieveSearchResource(serverHttpRequest, webSession, accessInterface, uriToGet, typeReference, accessToken, additionalCredentials);
                        }
                    }).orElse(Mono.error(() -> new SearchAuthorizationException(connectorAccessInterface)));
                });

        } else {
            return Mono.error(throwable);

        }

    }

    private Mono<AccessInterface> getAccessInterfaceForConnector(AccessInterface searchInterface, String connectorKey) {
        InterfaceId interfaceId = Id.decodeInterfaceId(searchInterface.getId());
        String connectorUri = UriComponentsBuilder.fromUri(searchInterface.getUri())
            .pathSegment("auth-realm", connectorKey).pathSegment().build()
            .toUriString();
        return resourceClientService
            .listResources(interfaceId.getRealm(), null, List.of("search:connector"), List.of(connectorUri))
            .flatMap(resourceList -> {
                Optional<AccessInterface> accessInterfaceOptional = resourceList.stream()
                    .flatMap(resource -> resource.getInterfaces().stream())
                    .filter(accessInterface -> accessInterface.isAuthRequired() && accessInterface.getUri().toString()
                        .equals(connectorUri)).findFirst();
                return Mono.justOrEmpty(accessInterfaceOptional);
            });
    }

    private Throwable handleSearchAuthorizationException(Throwable ex) {
        if (ex instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) ex;
            if (ga4ghSearchAuthRequired(webEx)) {
                try {
                    SearchAuthRequest authRequest = mapper
                        .readValue(webEx.getResponseBodyAsString(), SearchAuthRequest.class);
                    return new SearchAuthorizationException(authRequest);
                } catch (IOException ioException) {
                    return ioException;
                }
            } else {
                return new SearchResourceException(webEx.getResponseBodyAsString(), webEx
                    .getRawStatusCode(), webEx);
            }
        } else if (ex instanceof Exception) {
            return new SearchResourceException(ex.getMessage(), INTERNAL_SERVER_ERROR.value(), ex);
        } else {
            return ex;
        }
    }

    private boolean ga4ghSearchAuthRequired(WebClientResponseException webEx) {
        if (webEx.getRawStatusCode() == 401) {
            String authenticateHeader = webEx.getHeaders().getFirst("WWW-Authenticate");
            return authenticateHeader != null && authenticateHeader.contains("GA4GH-Search");
        }
        return false;
    }


    private ExchangeFilterFunction buildAuthExchangeFilter(String accessToken, Map<String, String> ga4ghCredentials) {
        return ExchangeFilterFunction.ofRequestProcessor((request) -> {
            if (accessToken != null) {
                request.headers().setBearerAuth(accessToken);
            }
            if (ga4ghCredentials != null) {
                ga4ghCredentials.forEach((connectorKey, credential) -> {
                    request.headers().add("GA4GH-Search-Authorization", connectorKey + "=" + credential);
                });
            }
            return Mono.just(request);
        });
    }

    @FunctionalInterface
    private interface AuthExceptionRecovery<T> {

        T recoverOnAuthException(ServerHttpRequest httpRequest, String realm, List<String> authorizationIds);
    }

}
