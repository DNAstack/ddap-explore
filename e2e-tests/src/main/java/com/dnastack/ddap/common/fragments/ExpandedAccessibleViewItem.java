package com.dnastack.ddap.common.fragments;

import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.WebPageScroller;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

@Slf4j
public class ExpandedAccessibleViewItem {

    private final WebDriver driver;
    private WebElement view;

    public ExpandedAccessibleViewItem(WebDriver driver, WebElement view) {
        this.driver = driver;
        this.view = view;
    }

    public URI requestAccess() {
        WebElement accessBtn = view.findElement(DdapBy.se("get-access-btn"));
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(accessBtn));
        return URI.create(accessBtn.getAttribute("href"));
    }

    public void assertHasAccessToken() {
        new WebDriverWait(driver, 5)
            .until(ExpectedConditions.visibilityOfElementLocated(DdapBy.se("access-token")));
        WebElement accessTokenInput = driver.findElement(DdapBy.se("access-token"));
        WebPageScroller.scrollTo(driver, accessTokenInput);
        String accessToken = accessTokenInput.getAttribute("value");
        assertThat(accessToken, not(isEmptyString()));
    }

    public String getDownloadLink() {
        return view.findElement(DdapBy.se("download-cli-button")).getAttribute("href");
    }
}
