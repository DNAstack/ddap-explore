package com.dnastack.ddap.frontend;

import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.DiscoveryPage;
import com.dnastack.ddap.common.setup.ConfigModel;
import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.EnvUtil;
import lombok.Data;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class DiscoveryE2eTest extends AbstractFrontendE2eTest {

    private static DiscoveryTestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetup() {
        testConfig = EnvUtil.optionalEnvConfig("E2E_DISCOVERY_CONFIG",
                new DiscoveryTestConfig(),
                DiscoveryTestConfig.class);
        Assume.assumeTrue("Discovery feature is disabled for this deployment, and will not be tested",
                testConfig.isEnabled());
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AnyDdapPage::new);
    }

    @Test
    public void queryBeacon() {
        DiscoveryPage discoveryPage = ddapPage.getNavBar().goToDiscovery();
        ddapPage.waitForInflightRequests();
        discoveryPage.fillFieldFromDropdown(DdapBy.se("beacon"), "Covid-19 Viral Genome");
        discoveryPage.fillField(DdapBy.se("start-inp"), "25417");
        discoveryPage.fillField(DdapBy.se("reference-bases-inp"), "C");
        discoveryPage.fillField(DdapBy.se("alternate-bases-inp"), "A");
        driver.findElement(DdapBy.se("submit-search-btn")).click();
        ddapPage.waitForInflightRequests();

        assertThat("beacon results exist",
                driver.findElements(By.xpath("//*[@data-se='data-table']//*[@role='row']")).size(),
                greaterThan(2));
    }

    @Test
    public void queryMultipleDatasets() throws IOException {
        DiscoveryPage discoveryPage = ddapPage.getNavBar().goToDiscovery();
        discoveryPage.fillFieldFromDropdown(DdapBy.se("beacon"), "Cafe Variome Beacon");
        discoveryPage.fillFieldFromDropdown(DdapBy.se("assembly-id-inp"), "GRCh37");
        discoveryPage.fillField(DdapBy.se("reference-name-inp"), "1");
        discoveryPage.fillField(DdapBy.se("start-inp"), "25417");
        discoveryPage.fillField(DdapBy.se("reference-bases-inp"), "C");
        discoveryPage.fillField(DdapBy.se("alternate-bases-inp"), "A");

        driver.findElement(DdapBy.se("submit-search-btn")).click();
        // query response would give an authUrl if authorization is required
        requestAccessIfRequired();

        // FIXME verify tabs with icons

        // FIXME deselect datasets and check results

    }

    private void requestAccessIfRequired() throws IOException {
        WebElement accessBtn = driver.findElement(DdapBy.se("get-access-btn"));
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.attributeContains(DdapBy.se("get-access-btn"),
                        "href", "?resource"));
        URI authorizeUrl = URI.create(accessBtn.getAttribute("href"));
        DiscoveryPage discoveryPage = loginStrategy
                .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, DiscoveryPage::new);
    }

    @Data
    public static class DiscoveryTestConfig implements ConfigModel {
        private boolean enabled;

        @Override
        public void validateConfig() {

        }
    }
}
