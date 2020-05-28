package com.dnastack.ddap.explore.apps.wes.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.apps.wes.service.DrsService;
import com.dnastack.ddap.explore.apps.wes.client.ReactiveWdlValidatorClient;
import com.dnastack.ddap.explore.apps.wes.model.WesResourceViews;
import com.dnastack.ddap.explore.apps.wes.model.WorkflowExecutionRunModel;
import com.dnastack.ddap.explore.apps.wes.model.WorkflowExecutionRunRequestModel;
import com.dnastack.ddap.explore.apps.wes.model.WorkflowExecutionRunsResponseModel;
import com.dnastack.ddap.explore.apps.wes.service.WesResourceService;
import com.dnastack.ddap.explore.apps.wes.service.WesService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/wes")
public class WorkflowController {

    private WesResourceService wesResourceService;
    private WesService wesService;

    private ReactiveWdlValidatorClient wdlValidatorClient;
    private Map<String, ReactiveDamClient> damClients;
    private final DrsService drsService;

    @Autowired
    public WorkflowController(ReactiveWdlValidatorClient wdlValidatorClient,
                              Map<String, ReactiveDamClient> damClients,
                              WesResourceService wesResourceService,
                              WesService wesService,
                              DrsService drsService) {
        this.wdlValidatorClient = wdlValidatorClient;
        this.wesResourceService = wesResourceService;
        this.wesService = wesService;
        this.damClients = damClients;
        this.drsService = drsService;
    }

    @PostMapping(value = "/describe")
    public Mono<Object> getJsonSchemaFromWdl(@RequestBody String wdl) {
        return wdlValidatorClient.getJsonSchema(wdl);
    }

    @GetMapping(value = "/views")
    public Flux<WesResourceViews> getWesResources(@PathVariable String realm) {
        return Flux.merge(damClients.entrySet().stream()
                .map(damClient -> wesResourceService.getResources(damClient, realm))
                .collect(toList())
        );
    }

    @GetMapping(value = "/{damId}/views/{viewId}/runs")
    public Mono<WorkflowExecutionRunsResponseModel> getWorkflowRuns(@PathVariable String realm,
                                                                    @PathVariable String damId,
                                                                    @PathVariable String viewId,
                                                                    @RequestParam String accessToken,
                                                                    @RequestParam(required = false) String nextPage) {
        // TODO: use damClientFactory
        Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                                                                   .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                                                                   .findFirst()
                                                                   .get();
        return wesService.getAllWorkflowRunsFromWesServer(damClient, realm, accessToken, viewId, nextPage);
    }

    @PostMapping(value = "/{damId}/views/{viewId}/runs")
    public Mono<WorkflowExecutionRunModel> addWorkflowToRun(@PathVariable String realm,
                                                            @PathVariable String damId,
                                                            @PathVariable String viewId,
                                                            @RequestParam String accessToken,
                                                            @RequestBody WorkflowExecutionRunRequestModel runRequest) {
        final Mono<Map<String, String>> resolvedByDrsUri =
                Flux.fromStream(runRequest.getCredentials()
                                          .keySet()
                                          .stream()
                                          .filter(drsService::isDrsUri))
                    .flatMap(drsUri -> {
                        return drsService.resolveAccessMethods(URI.create(drsUri), "gs")
                                         .flatMap(resolvedUris -> {
                                             if (resolvedUris.size() > 1) {
                                                 return Mono.error(new UnsupportedOperationException("Cannot resolve DRS URI with multiple gs access methods"));
                                             }

                                             return Mono.just(resolvedUris.stream()
                                                                          .sorted()
                                                                          .findFirst()
                                                                          .stream()
                                                                          .map(resolvedUri -> Map.entry(drsUri, resolvedUri)));
                                         });
                    })
                    .flatMap(Flux::fromStream)
                    .collectMap(Entry::getKey, Entry::getValue);

        final Mono<WorkflowExecutionRunRequestModel> transformedModel = resolvedByDrsUri.map(drsUriMap -> resolveDrsUris(runRequest, drsUriMap));

        // TODO: use damClientFactory
        Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                                                               .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                                                               .findFirst()
                                                               .get();

        return transformedModel.flatMap(model -> wesService.executeWorkflow(damClient, realm, accessToken, viewId, model));
    }

    private WorkflowExecutionRunRequestModel resolveDrsUris(WorkflowExecutionRunRequestModel original, Map<String, String> drsUriMap) {
        final WorkflowExecutionRunRequestModel transformed = new WorkflowExecutionRunRequestModel();

        final Map<String, WorkflowExecutionRunRequestModel.CredentialsModel> transformedTokensJson = original.getCredentials()
                                                                  .entrySet()
                                                                  .stream()
                                                                  .map(tokenMapping -> Map.entry(drsUriMap.getOrDefault(tokenMapping.getKey(), tokenMapping.getKey()), tokenMapping.getValue()))
                                                                  .collect(toMap(Entry::getKey, Entry::getValue));

        final Map<String, JsonNode> transformedInputJson = original.getInputsJson()
                                                                   .entrySet()
                                                                   .stream()
                                                                   .map(entry -> Map.entry(entry.getKey(), transformInput(entry.getValue(), drsUriMap)))
                                                                   .collect(toMap(Entry::getKey, Entry::getValue));

        transformed.setCredentials(transformedTokensJson);
        transformed.setInputsJson(transformedInputJson);
        transformed.setWdl(original.getWdl());

        return transformed;
    }

    private JsonNode transformInput(JsonNode node, Map<String, String> drsUriMap) {
        if (node.isTextual()) {
            final String value = node.asText();
            if (drsUriMap.containsKey(value)) {
                return new TextNode(drsUriMap.get(value));
            } else {
                return node;
            }
        } else if (node.isArray()) {
            final Spliterator<JsonNode> spliterator = Spliterators.spliteratorUnknownSize(node.iterator(), 0);
            // Doing a copy so that we don't have to create our own JsonFactory
            final ArrayNode copy = node.deepCopy();
            final JsonNode[] transformedItemNodes = StreamSupport.stream(spliterator, false)
                                                                 .map(itemNode -> transformInput(itemNode, drsUriMap))
                                                                 .toArray(JsonNode[]::new);
            for (int i = 0; i < transformedItemNodes.length; i++) {
                copy.set(i, transformedItemNodes[i]);
            }

            return copy;
        } else if (node.isObject()) {
            final Spliterator<Entry<String, JsonNode>> spliterator = Spliterators.spliteratorUnknownSize(node.fields(), 0);
            // Doing a copy so that we don't have to create our own JsonFactory
            final ObjectNode copy = node.deepCopy();
            StreamSupport.stream(spliterator, false)
                         .map(e -> Map.entry(drsUriMap.getOrDefault(e.getKey(), e.getKey()), transformInput(e.getValue(), drsUriMap)))
                         .forEach(e -> {
                             copy.set(e.getKey(), e.getValue());
                         });
            return copy;
        } else {
            return node;
        }
    }

    @GetMapping(value = "/{damId}/views/{viewId}/runs/{runId}")
    public Mono<WorkflowExecutionRunModel> getWorkflowRunDetails(@PathVariable String realm,
                                                                          @PathVariable String damId,
                                                                          @PathVariable String viewId,
                                                                          @PathVariable String runId,
                                                                 @RequestParam String accessToken) {
        // TODO: use damClientFactory
        Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                                                                   .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                                                                   .findFirst()
                                                                   .get();
        return wesService.getWorkflowRunDetails(damClient, realm, accessToken, viewId, runId);
    }

}
