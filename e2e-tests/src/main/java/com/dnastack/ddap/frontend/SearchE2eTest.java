package com.dnastack.ddap.frontend;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static org.junit.Assert.assertTrue;

import com.dnastack.ddap.common.fragments.NavBar;
import com.dnastack.ddap.common.page.AdminDdapPage;
import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.SearchResourcesPage;
import com.dnastack.ddap.common.page.TablesPage;
import com.dnastack.ddap.common.setup.ConfigModel;
import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.EnvUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.Data;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class SearchE2eTest extends AbstractFrontendE2eTest {

    private static SearchTestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        testConfig = EnvUtil.optionalEnvConfig("E2E_TEST_SEARCH_CONFIG",new SearchTestConfig(), SearchTestConfig.class);
        Assume.assumeTrue(testConfig.isEnabled());
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AdminDdapPage::new);
    }

    @Test
    public void queryPrestoTable() throws MalformedURLException {
        driver.navigate().to(new URL(driver.getCurrentUrl() + "?exp_flag=demo"));
        SearchResourcesPage searchResourcesPage= ddapPage.getNavBar().goToSearchResources();
        searchResourcesPage.exploreResource();
        ddapPage.waitForInflightRequests();

        TablesPage tablesPage = new TablesPage(driver);
        tablesPage.searchTable();
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
