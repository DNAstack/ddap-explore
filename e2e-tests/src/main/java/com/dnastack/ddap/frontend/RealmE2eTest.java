package com.dnastack.ddap.frontend;

import static com.dnastack.ddap.common.TestingPersona.ADMINISTRATOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.dnastack.ddap.common.page.AdminDdapPage;
import com.dnastack.ddap.common.setup.ConfigModel;
import com.dnastack.ddap.common.util.EnvUtil;
import java.io.IOException;
import lombok.Data;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

@SuppressWarnings("Duplicates")
public class RealmE2eTest extends AbstractFrontendE2eTest {

    private static RealmTestConfig realmTestConfig;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        realmTestConfig = EnvUtil.requiredEnvConfig("E2E_TEST_REALM_CONFIG",  RealmTestConfig.class);
        Assume.assumeTrue("RealmE2eTest has been disabled, and will not run.",realmTestConfig.isEnabled());
        Assume.assumeTrue(realmTestConfig.isSandbox());
        ddapPage = doBrowserLogin(REALM, ADMINISTRATOR, AdminDdapPage::new);
    }

    @Test
    public void testRealmChange() {
        // Make sure that it is current realm
        assertThat(ddapPage.getNavBar().getRealm(), is(REALM));

        assertThat("this test is pointless unless we start on a different realm than we're going to!",
            ddapPage.getNavBar().getRealm(), is(not(realmTestConfig.getOtherRealm())));

        // Change realm
        ddapPage.getNavBar().setRealm(realmTestConfig.getOtherRealm());
        ddapPage.waitForInflightRequests();
        assertThat(ddapPage.getNavBar().getRealm(), is(realmTestConfig.getOtherRealm()));

        // Wrap this with large timeout because redirect to IC and back happens here
        new WebDriverWait(driver, 10)
            .ignoring(AssertionError.class)
            .until(d -> {
                // Make sure that it is changed realm
                assertThat(driver.getCurrentUrl(), containsString(realmTestConfig.getOtherRealm()));
                assertThat(ddapPage.getNavBar().getRealm(), is(realmTestConfig.getOtherRealm()));
                return true;
            });
    }

    @Data
    public static class RealmTestConfig implements ConfigModel {

        private boolean enabled = true;
        private boolean sandbox = true;
        private String otherRealm;

        @Override
        public void validateConfig() {
            if (enabled) {
                assertThat(otherRealm, Matchers.notNullValue());
            }
        }


    }

}
