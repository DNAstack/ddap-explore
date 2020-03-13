package com.dnastack.ddap.explore.cli;

import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.common.security.PlainTextNotDecryptableException;
import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import com.dnastack.ddap.explore.dam.client.DamClientFactory;
import com.dnastack.ddap.explore.security.CartTokenCookieName;
import dam.v1.DamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.dnastack.ddap.explore.cli.InMemoryCliSessionStorage.AuthorizeStatus;


@Slf4j
@RestController
@RequestMapping(value = "/api/v1alpha/realm/{realm}/cli/{cliSessionId}/authorize")
public class CommandLineAccessController {

    private final UserTokenCookiePackager userTokenCookiePackager;
    private final Map<String, DamProperties> dams;
    private final DamClientFactory damClientFactory;

    @Autowired
    public CommandLineAccessController(UserTokenCookiePackager userTokenCookiePackager,
                                       Map<String, DamProperties> dams,
                                       DamClientFactory damClientFactory) {
        this.userTokenCookiePackager = userTokenCookiePackager;
        this.dams = dams;
        this.damClientFactory = damClientFactory;
    }

    @GetMapping(path = "/callback")
    public Mono<Void> authorizeCallback(
        ServerHttpRequest request,
        @PathVariable("realm") String realm,
        @PathVariable("cliSessionId") String cliSessionId,
        @RequestParam("resource") String damIdResourcePair
    ) {
        List<HttpCookie> cartCookies = request.getCookies()
            .values()
            .stream()
            .flatMap(Collection::stream)
            .filter((cookie) -> cookie.getName().startsWith("cart_"))
            .collect(Collectors.toList());

        AuthorizeStatus status = new AuthorizeStatus(damIdResourcePair, cartCookies, Instant.now().plusSeconds(300));
        InMemoryCliSessionStorage.cartCookies.putIfAbsent(cliSessionId, status);

        return Mono.empty();
    }

    @GetMapping(path = "/status", produces = "application/json")
    public Mono<DamService.ResourceResults> authorizeStatus(
        @PathVariable("realm") String realm,
        @PathVariable("cliSessionId") String cliSessionId,
        @RequestParam("resource") String damIdResourcePair
    ) throws PlainTextNotDecryptableException {
        if (!InMemoryCliSessionStorage.cartCookies.containsKey(cliSessionId)) {
            return Mono.empty();
        }

        AuthorizeStatus status = InMemoryCliSessionStorage.cartCookies.get(cliSessionId);
        if (!status.getResource().equals(damIdResourcePair)) {
            return Mono.empty();
        }

        URI resource = getResourceFrom(realm, damIdResourcePair);
        CartTokenCookieName cartCookieName = new CartTokenCookieName(Set.of(resource));
        Optional<HttpCookie> cartCookie = status.getCartCookies()
            .stream()
            .filter((cookie) -> cookie.getName().equals(cartCookieName.cookieName()))
            .findFirst();
        if (cartCookie.isEmpty()) {
            return Mono.error(CartTokenNotFoundInStorageException::new);
        }

        String damId = damIdResourcePair.split(";")[0];
        String plainTextCartCookie = userTokenCookiePackager.decodeToken(cartCookie.get().getValue());
        return damClientFactory.getDamClient(damId).checkoutCart(plainTextCartCookie);
    }

    /**
     * Expecting list of strings in form: ${DAM_ID};${RESOURCE_ID}/views/${VIEW_ID}/roles/${ROLE_ID}
     *
     * Example:
     *  1;test/views/test/roles/discovery
     *
     * @param realm
     * @param damIdResourcePair
     * @return absolute URL pointing to DAM resource
     */
    private URI getResourceFrom(String realm, String damIdResourcePair) {
        String[] pair = damIdResourcePair.split(";");
        URI damBaseUrl = dams.get(pair[0]).getBaseUrl();
        return damBaseUrl.resolve("/dam/" + realm + "/resources/" + pair[1]);
    }

    @PostMapping(path = "/clear", produces = "application/json")
    public Mono<DamService.ResourceResults> authorizeClear(
        @PathVariable("realm") String realm,
        @PathVariable("cliSessionId") String cliSessionId
    ) {
        if (!InMemoryCliSessionStorage.cartCookies.containsKey(cliSessionId)) {
            return Mono.empty();
        }

        InMemoryCliSessionStorage.cartCookies
            .remove(cliSessionId);

        return Mono.empty();
    }

    @Scheduled(fixedRate = 5*60*1000)
    public void cleanupCliSessions() {
        log.info("Starting cleanup of CLI sessions");
        final Instant start = Instant.now();
        final List<String> expiredSessionIds = InMemoryCliSessionStorage.cartCookies.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getExpiresIn().isBefore(start))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        expiredSessionIds.forEach(InMemoryCliSessionStorage.cartCookies::remove);
        final Instant end = Instant.now();
        final Duration duration = Duration.between(start, end);
        log.info("Removed {} CLI sessions in {}ms", expiredSessionIds.size(), TimeUnit.NANOSECONDS.toMillis(duration.get(ChronoUnit.NANOS)));
    }

}
