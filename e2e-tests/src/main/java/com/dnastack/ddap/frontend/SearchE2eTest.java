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
import java.net.URL;
import lombok.Data;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchE2eTest extends AbstractFrontendE2eTest {

    private static SearchTestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        testConfig = EnvUtil.optionalEnvConfig("E2E_TEST_SEARCH_CONFIG",new SearchTestConfig(), SearchTestConfig.class);
        Assume.assumeTrue("The search feature has been disabled for this deployment, and will not be tested",testConfig.isEnabled());
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AdminDdapPage::new);
    }

    @Test
    @Ignore
    public void queryPrestoTable() throws MalformedURLException {
        createSearchServiceTemplate();
        createSearchResource();
        driver.navigate().to(new URL(driver.getCurrentUrl() + "?exp_flag=demo"));
        SearchResourcesPage searchResourcesPage= ddapPage.getNavBar().goToSearchResources();
        searchResourcesPage.exploreResource();
        ddapPage.waitForInflightRequests();

        TablesPage tablesPage = new TablesPage(driver);
        tablesPage.searchTable();
        ddapPage.waitForInflightRequests();
        assertTrue(driver.findElement(DdapBy.se("result-wrapper")).isDisplayed());
    }

    // TODO get dam url and create resource with search view
    private void createSearchResource() {

    }

    // TODO get dam url and create service template
    private void createSearchServiceTemplate() {
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.elementToBeClickable(DdapBy.se("product-app-menu")));
        driver.findElement(DdapBy.se("product-app-menu")).click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.presenceOfElementLocated(DdapBy.se("nav-data-access-manager")));
        driver.findElement(DdapBy.se("nav-data-access-manager")).getAttribute("href");
    }
    @Data
    public static class SearchTestConfig implements ConfigModel {

        private boolean enabled = true;

        @Override
        public void validateConfig() {

        }
    }
}
