package com.dnastack.ddap.frontend;

import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.Covid19BeaconPage;
import com.dnastack.ddap.common.setup.ConfigModel;
import com.dnastack.ddap.common.util.EnvUtil;
import lombok.Data;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;

public class Covid19BeaconE2eTest extends AbstractFrontendE2eTest {
    private static Covid19BeaconTestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetup() {
        testConfig = EnvUtil.optionalEnvConfig("E2E_TEST_COVID19_CONFIG", new Covid19BeaconTestConfig(), Covid19BeaconTestConfig.class);
        Assume.assumeTrue("Covid19BeaconE2eTest has been disabled, and will not run.", testConfig.isEnabled());
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AnyDdapPage::new);
    }

    @Test
    public void walkthrough() {
        // In COVID-19 release, the UI may not have app switcher.
        String startingUri = URI.create(DDAP_BASE_URL).resolve("/_/discovery").toString();
        driver.navigate().to(startingUri);

        Covid19BeaconPage page = new Covid19BeaconPage(driver);
        page.searchFor("3847", "G", "A");

        page.getMoreDetailsOn("BetaCoV/pangolin/Guangxi/P4L/2017");

        page.haveDetailsPanelShown();
        page.seeInfoInDetailsPanel("BetaCoV/pangolin/Guangxi/P4L/2017");
        page.seeInfoInDetailsPanel("EPI_ISL_410538");
        page.seeInfoInDetailsPanel("GISAID");
        page.seeInfoInDetailsPanel("Manis javanica");
    }

    @Data
    public static class Covid19BeaconTestConfig implements ConfigModel {
        private boolean enabled = true;

        @Override
        public void validateConfig() {
            // No validation required
        }
    }
}
