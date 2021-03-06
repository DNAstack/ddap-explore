package com.dnastack.ddap.common.setup;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.fail;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.util.EnvUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import dam.v1.DamService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

@Slf4j
public class DamConfigStrategy implements ConfigStrategy {

    private boolean setupComplete;
    private DamConfig damConfig;

    public DamConfigStrategy() {
        setupComplete = false;
        damConfig = EnvUtil.requiredEnvConfig("E2E_DAM_CONFIG", DamConfig.class);
    }

    @Override
    public synchronized void doOnetimeSetup() {
        if (!setupComplete) {
            try {
                setupRealmConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void setupRealmConfig() throws IOException {
        DamService.DamConfig.Builder damConfigBuilder = DamService.DamConfig.newBuilder();
        String damRealmJson = getDamRealmJsonAsString();
        validateProtoBuf(damRealmJson, damConfigBuilder);

        /*
         Use the master realm because some tests break the ability to reset realms in future runs.
         In particular, tests that reset the IC config can change the 'ga4gh_dam` client ID which needs
         to be a particular value (configured in master) for passport tokens to have a validatable audience
         */
        final CookieStore cookieStore = StrategyFactory.getLoginStrategy()
            .performPersonaLogin(TestingPersona.ADMINISTRATOR.getId(), AbstractBaseE2eTest.REALM);

        damRealmJson = appendRealmClientsToExistingClientsInConfig(cookieStore, damRealmJson, AbstractBaseE2eTest.REALM);

        final String modificationPayload = format("{ \"item\": %s }", damRealmJson);

        final HttpClient httpclient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        HttpPut request = new HttpPut(format("%s/dam/v1alpha/%s/config", damConfig
            .getDamBaseUrl(), AbstractBaseE2eTest.REALM));
        request.setEntity(new StringEntity(modificationPayload));

        System.out.printf("Sending setup realm request to URI [%s]\n", request.getURI());

        final HttpResponse response = httpclient.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        assertThat(format("Unable to set realm config. Response:\n%s\nConfig:\n%s", responseBody, damRealmJson),
            response.getStatusLine().getStatusCode(),
            allOf(greaterThanOrEqualTo(200), lessThan(300)));
    }


    private JSONObject getExistingClients(CookieStore cookieStore, String realm) throws IOException {
            HttpClient httpclient = HttpClientBuilder.create()
                                                     .setDefaultCookieStore(cookieStore)
                                                     .build();

            HttpGet request = new HttpGet(format("%s/dam/v1alpha/%s/config", damConfig.getDamBaseUrl(), realm));
            HttpResponse response = httpclient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());

        try {
            JSONObject damConfig = new JSONObject(responseBody);
            JSONObject clients = damConfig.getJSONObject("clients");

            log.debug("Parsed clients from {} realm: {}", realm, clients);

            return clients;
        } catch (JSONException je) {
            throw new RuntimeException(String.format("Failed to lookup previous clients. Response payload:\n%s\n", responseBody), je);
        }
    }

    private String appendRealmClientsToExistingClientsInConfig(CookieStore cookieStore, String damConfig, String realm) throws IOException {
        JSONObject damConfigClients = new JSONObject(damConfig).getJSONObject("clients");
        JSONObject masterRealmClients = getExistingClients(cookieStore, realm);
        masterRealmClients.keySet()
            .forEach((masterRealmClient) -> {
                damConfigClients.put(masterRealmClient, masterRealmClients.get(masterRealmClient));
            });

        JSONObject newDamConfig = new JSONObject(damConfig);
        newDamConfig.put("clients", damConfigClients);

        log.debug("DAM Config was altered to include master realm clients: {}", newDamConfig);

        return newDamConfig.toString();
    }

    private static void validateProtoBuf(String resourceJsonString, Message.Builder builder) {
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(resourceJsonString, builder);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse proto", e);
        }
    }

    private String getDamRealmJsonAsString() {
        ObjectMapper mapper = new ObjectMapper();
        String realmJsonString = null;
        try {
            realmJsonString = mapper.writeValueAsString(damConfig.getDamRealmJson());
        } catch (JsonProcessingException e) {
            fail(e.getMessage());
        }
        return realmJsonString;
    }


}
