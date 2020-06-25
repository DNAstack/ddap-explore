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
import java.util.Map;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class DiscoveryE2eTest extends AbstractFrontendE2eTest {

    private static DiscoveryTestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetup() {
        testConfig = EnvUtil.optionalEnvConfig("E2E_TEST_DISCOVERY_CONFIG",
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
        discoveryPage.fillFieldFromDropdown(DdapBy.se("beacon"),
                testConfig.singleDatasetBeacon.getBeaconName());
        discoveryPage.fillField(DdapBy.se("start-inp"),
                testConfig.singleDatasetBeacon.getStart());
        discoveryPage.fillField(DdapBy.se("reference-bases-inp"),
                testConfig.singleDatasetBeacon.getReferenceBases());
        discoveryPage.fillField(DdapBy.se("alternate-bases-inp"),
                testConfig.singleDatasetBeacon.getAlternateBases());
        driver.findElement(DdapBy.se("submit-search-btn")).click();
        ddapPage.waitForInflightRequests();

        assertThat("beacon results exist",
                driver.findElements(By.xpath("//*[@data-se='data-table']//*[@role='row']")).size(),
                greaterThan(2));
    }

    @Test
    public void queryMultipleDatasets() throws IOException {
        DiscoveryPage discoveryPage = ddapPage.getNavBar().goToDiscovery();
        ddapPage.waitForInflightRequests();
        discoveryPage.fillFieldFromDropdown(DdapBy.se("beacon"),
                testConfig.multipleDatasetBeacon.getBeaconName());
        discoveryPage.fillFieldFromDropdown(DdapBy.se("assembly-id-inp"),
                testConfig.multipleDatasetBeacon.getAssemblyId());
        discoveryPage.fillField(DdapBy.se("reference-name-inp"),
                testConfig.multipleDatasetBeacon.getReferenceName());
        discoveryPage.fillField(DdapBy.se("start-inp"),
                testConfig.multipleDatasetBeacon.getStart());
        discoveryPage.fillField(DdapBy.se("reference-bases-inp"),
                testConfig.multipleDatasetBeacon.getReferenceBases());
        discoveryPage.fillField(DdapBy.se("alternate-bases-inp"),
                testConfig.multipleDatasetBeacon.getAlternateBases());

        driver.findElement(DdapBy.se("submit-search-btn")).click();
        ddapPage.waitForInflightRequests();
        // query response would give an authUrl if authorization is required
        requestAccessIfRequired();
        ddapPage.waitForInflightRequests();
        String[] datasetIds = driver.findElement(DdapBy.se("datasets")).getText().split(",");
        assertThat("Number of tabs is equal to number of datasetIds selected",
                driver.findElements(By.className("mat-tab-label")).size(),
                equalTo(datasetIds.length));

        assertThat("Variants are present for the beacon query",
                driver.findElements(DdapBy.se("matching-found")).size(),
                equalTo(datasetIds.length));

    }

    private void requestAccessIfRequired() throws IOException {
        WebElement accessBtn = driver.findElement(DdapBy.se("get-access-btn"));
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.attributeContains(DdapBy.se("get-access-btn"),
                        "href", "?resource"));
        URI authorizeUrl = URI.create(accessBtn.getAttribute("href"));
        loginStrategy
                .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, DiscoveryPage::new);
    }

    @Data
    public static class DiscoveryTestConfig implements ConfigModel {
        private boolean enabled;


        private Beacon singleDatasetBeacon;
        private Beacon multipleDatasetBeacon;


        @Override
        public void validateConfig() {

        }

        @Data
        private class Beacon {
            private String beaconName;
            private String assemblyId;
            private String referenceName;
            private String start;
            private String referenceBases;
            private String alternateBases;
        }
    }
}
