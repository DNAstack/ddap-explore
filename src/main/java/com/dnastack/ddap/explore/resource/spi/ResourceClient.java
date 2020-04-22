package com.dnastack.ddap.explore.resource.spi;

import com.dnastack.ddap.explore.resource.model.Collection;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.OAuthState;
import com.dnastack.ddap.explore.resource.model.Resource;
import com.dnastack.ddap.explore.resource.model.UserCredential;
import java.net.URI;
import java.util.List;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;


/**
 * Interface defining a client which can be used for interacting with resources served by an underlying resource server.
 * This interface provides an abstraction layer ontop of existing clients
 * to present a unified mechanism for  interacting with {@code Resources}, {@code Collections}, as well as providing an
 * approach to generate and store interface specific credentials.
 *
 * ResourceClients should be configured using a concrete instance of {@link ResourceClientFactory} in order to properly
 * handle autowiring of required beans, as well as enforement of required configuration properties
 * <p/>
 */
public interface ResourceClient {


    /**
     * Retrieve the SPI Key uniquely identifying this Client. The key is used to attribute resources to this client, as
     * well as for performing configuration at startup
     */
    String getSpiKey();


    /**
     * List All of the resources from the underlying resource server, optionally filtering the resources by the supplied
     * filters. When one or more filters are passed, the resulting list of resources must consist of all entries with at
     * least one match in each filter. For exmaple, if I pass a list of <code>collectionsToFilter</code>, and
     * <code>interfaceTypesToFilter</code>, the resulting list will be resources that have entries in <b>Both</b>
     * lists.
     * <p></p>
     * A special case exists for <code>interfaceUrisToFilter</code>. All implementation should filter by partial prefix
     * mapping of the interface URI in order to allow for "reverse lookups" of a url to a resource. A URI should be
     * considered a partial match IFF the following conditions apply:
     * <ol>
     *     <li>The URI is shorter or exactly equal in length to the InterfaceUri </li>
     *     <li>If the URI is shorter then the InterfaceUri, it must be tested against the interface URI with it ending in a slash ({@code /})</li>
     * </ol>
     *
     * If there are no resources found, then an empty list should be returned;
     *
     * @param realm The Realm to retrieve resources from
     * @param collectionsToFilter a list of collection ids to filter the response by
     * @param interfaceTypesToFilter a list of interfaceTypes. These values are URN's defined by the praticular resource
     * server.
     * @param interfaceUrisToFilter a list of complete or partial uris to filter by
     * @return List of filtered resources
     */
    Mono<List<Resource>> listResources(String realm, List<Id> collectionsToFilter, List<String> interfaceTypesToFilter, List<String> interfaceUrisToFilter);

    /**
     * Get a Single resource from the Configuered Resource server or raise an exception if it is not present
     *
     * @param realm The Realm to retrieve resources from
     * @param resourceId The id of the Resource to look up
     * @return the identified resource
     */
    Mono<Resource> getResource(String realm, Id resourceId);

    /**
     * List all of the collections from the underlying resource server, or an empty list if there are non configured
     *
     * @param realm The realm to retrieve resources from
     * @return List of collections
     */
    Mono<List<Collection>> listCollections(String realm);

    /**
     * Get a Single collection from the Configuered Resource server or raise an exception if it is not present
     *
     * @param realm The Realm to retrieve resources from
     * @param collection The id of the Resource to look up
     * @return the identified resource
     */
    Mono<Collection> getCollection(String realm, Id collection);

    /**
     * Determine whether a resource requires authorization or not. Some implementations may choose to publicly expose
     * their underlying resources and therefore will not require an authorizaiton flow. In these cases, this method
     * should return false
     */
    default boolean resourceRequiresAutorization(Id resourceId) {
        return true;
    }

    /**
     * Prepare the OAuthState which will be used to perform the authorization of one or more resources. This method
     * should not actually perform any of the authorization.
     *
     * @param realm realm The realm to retrieve resources from
     * @param resources The list of resources to authorize
     * @param postLoginRedirect The callback URI for processing the authorization response
     * @param scopes Scopes to request during the authorization code flow
     * @param loginHint A login hint to pass to the underlying IDP
     * @param ttl The TTL to request credentials are valid for. Unless the backing IDP shows otherwise, all credentials
     * should be assumed to live for this TTL
     * @return the OAuthState ready for authorization flows
     */
    OAuthState prepareOauthState(String realm, List<Id> resources, URI postLoginRedirect, String scopes, String loginHint, String ttl);

    /**
     * Handle the result of an authorization code flow and retreive a list of {@link UserCredential} which can be used
     * to directly access a specific resource interafce. Its important to note, that the credentials Must be
     * <b>Directly</b> applicable to the <code>interfaceUri</code> and not require any additional steps for access
     */
    Mono<List<UserCredential>> handleResponseAndGetCredentials(ServerHttpRequest exchange, URI redirectUri, OAuthState currentState, String code);

    default boolean shouldKeepInterfaceUri(String interfaceUri, List<String> testUris) {
        return testUris.stream().anyMatch(testUri -> {
            String testInerfaceUri = interfaceUri;
            if (!testInerfaceUri.endsWith("/") && testUri.length() > testInerfaceUri.length()) {
                testInerfaceUri = testInerfaceUri + "/";
            }
            return testUri.startsWith(testInerfaceUri);
        });
    }

}
