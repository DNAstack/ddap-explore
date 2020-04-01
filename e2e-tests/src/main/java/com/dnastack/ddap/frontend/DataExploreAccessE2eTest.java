package com.dnastack.ddap.frontend;

import static com.dnastack.ddap.common.TestingPersona.USER_WITHOUT_ACCESS;
import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dnastack.ddap.common.PolicyRequirementFailedException;
import com.dnastack.ddap.common.fragments.ExpandedAccessibleViewItem;
import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.DataDetailPage;
import com.dnastack.ddap.common.page.DataListPage;
import com.dnastack.ddap.common.setup.ConfigModel;
import com.dnastack.ddap.common.util.EnvUtil;
import java.io.IOException;
import java.net.URI;
import lombok.Data;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("Duplicates")
public class DataExploreAccessE2eTest extends AbstractFrontendE2eTest {

    private static DataExploreTestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        testConfig = EnvUtil.optionalEnvConfig("E2E_TEST_DATA_EXPLORE_CONFIG",new DataExploreTestConfig(),  DataExploreTestConfig.class);
        Assume.assumeTrue("DataExploreAccessE2eTest has been disabled, and will not run.",testConfig.isEnabled());
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AnyDdapPage::new);
    }

    @Test
    public void testRequestAccessForBeaconDiscoveryExpectSuccess() throws IOException, InterruptedException {
        DataListPage dataListPage = ddapPage.getNavBar().goToData();
        DataDetailPage detailPage = dataListPage
            .findDataByName(testConfig.getResourceName())
            .clickViewButton();

        ExpandedAccessibleViewItem discoveryView = detailPage.expandViewItem(testConfig.getDiscoveryView());
        URI authorizeUrl = discoveryView.requestAccess();
        detailPage = loginStrategy
            .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, DataDetailPage::new);

        /*
         FIXME DISCO-2743 something that loads on this page causes the expansion panel to collapse
               after it is clicked!
         */
        Thread.sleep(2000);
        discoveryView = detailPage.expandViewItem(testConfig.getDiscoveryView());
        discoveryView.assertHasAccessToken();
    }

    @Test(expected = PolicyRequirementFailedException.class)
    public void testRequestAccessForBeaconDiscoveryAccessExpectFailure() throws IOException {
        // NOTE: Tried @Ignore but it doesn't work.
        Assume.assumeTrue("loginStrategy.authorizeForResources fails to send 'code' to ResourceFlowController", false);

        DataListPage dataListPage = ddapPage.getNavBar().goToData();
        DataDetailPage detailPage = dataListPage
            .findDataByName(testConfig.getResourceName())
            .clickViewButton();

        ExpandedAccessibleViewItem discoveryView = detailPage.expandViewItem(testConfig.getViewWithDownloadLink());
        URI authorizeUrl = discoveryView.requestAccess();
        loginStrategy.authorizeForResources(driver, USER_WITHOUT_ACCESS, REALM, authorizeUrl, DataDetailPage::new);
    }

    @Test
    public void shouldFindWorkingDownloadLink() throws IOException {
        DataListPage dataListPage = ddapPage.getNavBar().goToData();
        DataDetailPage detailPage = dataListPage
            .findDataByName(testConfig.getResourceName())
            .clickViewButton();

        ExpandedAccessibleViewItem fullFileReadView = detailPage.expandViewItem(testConfig.getViewWithDownloadLink());
        String downloadHref = fullFileReadView.getDownloadLink();

        /*
         * Clicking a link that opens in a new tab is difficult to do, so instead let's just
         * check that the href works directly.
         */
        getRequestSpecWithBasicAuthIfNeeded()
            .when()
            .get(downloadHref)
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType("application/zip");
    }

    @Data
    public static class DataExploreTestConfig implements ConfigModel {
        private boolean enabled = true;
        private String resourceName;
        private String discoveryView;
        private String viewWithDownloadLink;

        @Override
        public void validateConfig() {
            if (enabled) {
                assertThat(resourceName, Matchers.notNullValue());
                assertThat(discoveryView, Matchers.notNullValue());
                assertThat(viewWithDownloadLink, Matchers.notNullValue());
            }
        }
    }

}
