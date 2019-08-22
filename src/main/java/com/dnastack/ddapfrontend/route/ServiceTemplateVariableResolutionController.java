package com.dnastack.ddapfrontend.route;

import static java.lang.String.format;

import com.dnastack.ddapfrontend.client.dam.DamClientFactory;
import com.dnastack.ddapfrontend.client.dam.ReactiveDamClient;
import com.dnastack.ddapfrontend.config.Dam;
import com.dnastack.ddapfrontend.security.UserTokenCookiePackager;
import dam.v1.DamService;
import dam.v1.DamService.ItemFormat;
import dam.v1.DamService.ServiceTemplate;
import dam.v1.DamService.TargetAdapter;
import dam.v1.DamService.TargetAdaptersResponse;
import dam.v1.DamService.VariableFormat;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/v1alpha/{realm}/serviceTemplates")
public class ServiceTemplateVariableResolutionController {

    private UserTokenCookiePackager cookiePackager;
    private DamClientFactory damClientFactory;
    private Map<String, Dam> dams;

    @Autowired
    public ServiceTemplateVariableResolutionController(UserTokenCookiePackager cookiePackager,
        DamClientFactory damClientFactory,
        @Qualifier("dams") Map<String, Dam> dams) {
        this.cookiePackager = cookiePackager;
        this.damClientFactory = damClientFactory;
        this.dams = dams;
    }

    /*
     * Temporary hack while we work on DISCO-2276
     */
    @GetMapping(value = "variables")
    public Mono<Map<String, VariableFormat>> resolveVariables(@PathVariable String realm,
        @RequestParam(name = "serviceTemplate") String serviceTemplateId,
        ServerHttpRequest request) {
        if (dams.size() != 1) {
            throw new IllegalArgumentException("Must specify DAM ID when more than one DAM is configured.");
        }

        return resolveVariables(realm, dams.keySet().iterator().next(), serviceTemplateId, request);
    }

    @GetMapping(value = "{damId}/variables")
    public Mono<Map<String, VariableFormat>> resolveVariables(@PathVariable String realm,
        @PathVariable String damId,
        @RequestParam(name = "serviceTemplate") String serviceTemplateId,
        ServerHttpRequest request) {
        Optional<String> foundDamToken = cookiePackager.extractToken(request, UserTokenCookiePackager.CookieKind.DAM);
        Optional<String> foundRefreshToken = cookiePackager
            .extractToken(request, UserTokenCookiePackager.CookieKind.DAM);
        String damToken = foundDamToken
            .orElseThrow(() -> new IllegalArgumentException("Authorization dam token is required."));
        String refreshToken = foundRefreshToken
            .orElseThrow(() -> new IllegalArgumentException("Authorization refresh token is required."));

        final ReactiveDamClient damClient = damClientFactory.getDamClient(damId);

        return getServiceTemplate(damClient, realm, damToken, refreshToken, serviceTemplateId)
            .flatMap(serviceTemplate -> getItemFormatForServiceTemplate(damClient, realm, damToken, refreshToken, serviceTemplate)
                .map(ItemFormat::getVariablesMap));
    }

    private Mono<ServiceTemplate> getServiceTemplate(ReactiveDamClient damClient, String realm, String damToken,
        String refreshToken, String serviceTemplateId) {
        return damClient.getConfig(realm, damToken, refreshToken)
            .map(DamService.DamConfig::getServiceTemplatesMap)
            .map(serviceTemplates -> {
                if (!serviceTemplates.containsKey(serviceTemplateId)) {
                    throw new IllegalArgumentException(format("Unrecognized serviceTemplate id [%s]", serviceTemplateId));
                }
                return serviceTemplates.get(serviceTemplateId);
            });
    }

    private Mono<ItemFormat> getItemFormatForServiceTemplate(ReactiveDamClient damClient, String realm,
        String damToken, String refreshToken, ServiceTemplate serviceTemplate) {
        String targetAdapterId = serviceTemplate.getTargetAdapter();
        String itemFormatId = serviceTemplate.getItemFormat();

        return damClient.getTargetAdapters(realm, damToken, refreshToken)
            .map(targetAdaptersResponse -> {
                TargetAdapter targetAdapter = getDamTargetAdapter(targetAdapterId, targetAdaptersResponse);
                return getDamItemFormat(targetAdapterId, itemFormatId, targetAdapter);
            });
    }

    private ItemFormat getDamItemFormat(String targetAdapterId, String itemFormatId, TargetAdapter targetAdapter) {
        ItemFormat itemFormat = targetAdapter.getItemFormats().get(itemFormatId);
        if (itemFormat == null) {
            throw new IllegalStateException(format(
                "Could not find itemFormat [%s] in targetAdapter [%s] referenced from service template",
                itemFormatId,
                targetAdapterId));
        }
        return itemFormat;
    }

    private TargetAdapter getDamTargetAdapter(String targetAdapterId, TargetAdaptersResponse targetAdaptersResponse) {
        TargetAdapter targetAdapter = targetAdaptersResponse.getTargetAdaptersMap().get(targetAdapterId);
        if (targetAdapter == null) {
            throw new IllegalStateException(format(
                "Could not find targetAdapter [%s] referenced from service template",
                targetAdapterId));
        }
        return targetAdapter;
    }

}
