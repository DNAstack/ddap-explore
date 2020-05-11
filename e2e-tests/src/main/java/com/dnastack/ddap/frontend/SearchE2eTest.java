package com.dnastack.ddap.frontend;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static org.junit.Assert.assertTrue;

import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.page.*;
import com.dnastack.ddap.common.setup.ConfigModel;
import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.EnvUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class SearchE2eTest extends AbstractFrontendE2eTest {

    private static SearchTestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        testConfig = EnvUtil.optionalEnvConfig("E2E_TEST_SEARCH_CONFIG",new SearchTestConfig(), SearchTestConfig.class);
        Assume.assumeTrue("The search feature has been disabled for this deployment, and will not be tested",testConfig.isEnabled());
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AdminDdapPage::new);
    }

    @Ignore
    @Test
    public void queryPrestoTable() throws IOException {
        driver.navigate().to(new URL(driver.getCurrentUrl() + "?exp_flag=demo"));
        ddapPage.getNavBar().goToSearchResources();
        log.info("Authorizing for search resources");

        WebElement accessBtn = driver.findElement(DdapBy.se("search-resource"));
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.attributeContains(DdapBy.se("search-resource"), "data-href", "?resource"));
        URI authorizeUrl = URI.create(accessBtn.getAttribute("data-href"));

        TablesPage tablesPage = loginStrategy
                .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, TablesPage::new);
        tablesPage.fillEditorWithQueryAndRun();
        ddapPage.waitForInflightRequests();
        assertTrue(driver.findElement(DdapBy.se("result-wrapper")).isDisplayed());
    }

    @Data
    public static class SearchTestConfig implements ConfigModel {

        private boolean enabled = true;

        @Override
        public void validateConfig() {

        }
    }
}
