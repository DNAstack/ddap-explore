package com.dnastack.ddap.common.util;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

@Slf4j
public class WebDriverCookieHelper {

    public static final String SESSION_COOKIE_NAME = "SESSION";
    public static final String SESSION_DRECRYPTION_COOKIE_NAME = "SESSION_DECRYPTION_KEY";

    public static Optional<Cookie> getCookie(WebDriver driver, String cookieName) {
        return Optional.ofNullable(driver.manage().getCookieNamed(cookieName));
    }

    public static void addBrowserCookie(WebDriver driver, org.apache.http.cookie.Cookie httpCookie) {
        if (httpCookie != null) {
            getCookie(driver, httpCookie.getName()).ifPresent((cookie) -> driver.manage().deleteCookie(cookie));
            driver.manage().addCookie(mapToBrowserCookie(httpCookie));
        }
    }



    public static Cookie mapToBrowserCookie(org.apache.http.cookie.Cookie cookie) {
        // Making cookie null for localhost, more info: https://stackoverflow.com/a/29312227/4445511
        final String domain = "localhost".equals(cookie.getDomain()) ? null : cookie.getDomain();
        return new Cookie(cookie.getName(), cookie.getValue(), domain, cookie.getPath(), cookie.getExpiryDate(), cookie.isSecure());
    }

    public static void cleanUpAllCartCookies(WebDriver driver) {
        driver.manage().getCookies()
            .stream()
            .filter((cookie) -> cookie.getName().contains("cart"))
            .forEach((cookie) -> driver.manage().deleteCookie(cookie));
    }


    public static org.apache.http.client.CookieStore setupCookieStore(org.apache.http.cookie.Cookie sessionCookie) {
        final CookieStore cookieStore = new BasicCookieStore();
        if (sessionCookie != null) {
            cookieStore.addCookie(sessionCookie);
        }
        return cookieStore;
    }

    public static void addCookiesFromStoreToSelenium(CookieStore cookieStore, WebDriver driver) {
        cookieStore.getCookies()
            .forEach(cookie -> {
                System.out.printf(
                    "Adding cookie to selenium: Cookie(name=%s, domain=%s, path=%s, expiry=%s, secure=%b" + System
                        .lineSeparator(), cookie.getName(), cookie.getDomain(), cookie.getPath(), cookie
                        .getExpiryDate(), cookie.isSecure());
                addBrowserCookie(driver, cookie);
            });
    }
}
