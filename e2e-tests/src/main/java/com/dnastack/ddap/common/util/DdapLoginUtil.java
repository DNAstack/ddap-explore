package com.dnastack.ddap.common.util;

import static com.dnastack.ddap.common.util.WebDriverCookieHelper.SESSION_COOKIE_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;

@Slf4j
public class DdapLoginUtil {

    public static Cookie loginToDdap(String url, String username, String password) throws IOException {
        if (username != null || password != null){
            final CookieStore cookieStore = new BasicCookieStore();
            final HttpClient httpclient = setupHttpClient(cookieStore);

            HttpPost request = new HttpPost(String.format("%s/login", url));

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            request.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = httpclient.execute(request);
            assertThat(response.getStatusLine().getStatusCode(), equalTo(302));
            assertThat(response.getLastHeader("Location").getValue(), not(equalTo("/login?error")));

            Optional<Cookie> sessionCookie = cookieStore.getCookies().stream()
                .filter((cookie) -> cookie.getName().equals(SESSION_COOKIE_NAME))
                .findFirst();
            return sessionCookie
                .orElseThrow(SessionCookieNotPresentException::new);
        } else {
            return new BasicClientCookie(SESSION_COOKIE_NAME, null);
        }
    }

    private static CloseableHttpClient setupHttpClient(CookieStore cookieStore) {
        return HttpClientBuilder.create()
            .setDefaultCookieStore(cookieStore)
            .build();
    }

}
