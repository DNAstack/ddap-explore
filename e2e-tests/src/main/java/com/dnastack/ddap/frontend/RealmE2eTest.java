package com.dnastack.ddap.frontend;

import static com.dnastack.ddap.common.TestingPersona.ADMINISTRATOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.dnastack.ddap.common.page.AdminDdapPage;
import com.dnastack.ddap.common.util.EnvUtil;
import java.io.IOException;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

@SuppressWarnings("Duplicates")
public class RealmE2eTest extends AbstractFrontendE2eTest {
    public static final boolean DDAP_CURRENT_TEST_ENABLED = Boolean.parseBoolean(EnvUtil.optionalEnv("E2E_TEST_REALM_ENABLED", "true"));

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        Assume.assumeTrue(DDAP_CURRENT_TEST_ENABLED);

        boolean isSandbox = Boolean.parseBoolean(EnvUtil.optionalEnv("E2E_SANDBOX", "false"));
        Assume.assumeTrue(isSandbox);
        ddapPage = doBrowserLogin(REALM, ADMINISTRATOR, AdminDdapPage::new);
    }

    @Test
    public void testRealmChange() {
        // Make sure that it is current realm
        assertThat(ddapPage.getNavBar().getRealm(), is(REALM));

        String otherRealm = "test_other_realm_1";
        assertThat("this test is pointless unless we start on a different realm than we're going to!",
            ddapPage.getNavBar().getRealm(), is(not(otherRealm)));

        // Change realm
        ddapPage.getNavBar().setRealm(otherRealm);
        ddapPage.waitForInflightRequests();
        assertThat(ddapPage.getNavBar().getRealm(), is(otherRealm));

        // Wrap this with large timeout because redirect to IC and back happens here
        new WebDriverWait(driver, 10)
            .ignoring(AssertionError.class)
            .until(d -> {
                // Make sure that it is changed realm
                assertThat(driver.getCurrentUrl(), containsString(otherRealm));
                assertThat(ddapPage.getNavBar().getRealm(), is(otherRealm));
                return true;
            });
    }
}
