package com.dnastack.ddap.frontend;

import com.dnastack.ddap.common.PolicyRequirementFailedException;
import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.fragments.ExpandedAccessibleViewItem;
import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.DataDetailPage;
import com.dnastack.ddap.common.page.DataListPage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static com.dnastack.ddap.common.TestingPersona.USER_WITHOUT_ACCESS;
import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static io.restassured.RestAssured.given;

@SuppressWarnings("Duplicates")
public class DataExploreAccessE2eTest extends AbstractFrontendE2eTest {

    private static final String REALM = generateRealmName(DataExploreAccessE2eTest.class.getSimpleName());

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        final String damConfig = loadTemplate("/com/dnastack/ddap/adminConfig.json");
        setupRealmConfig(TestingPersona.ADMINISTRATOR, damConfig, "1", REALM);

        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AnyDdapPage::new);
    }

    private String basicUsername;
    private String basicPassword;

    @Before
    public void setup() {
        basicUsername = requiredEnv("E2E_BASIC_USERNAME");
        basicPassword = requiredEnv("E2E_BASIC_PASSWORD");
    }

    @Test
    public void testRequestAccessForBeaconDiscoveryExpectSuccess() throws IOException {
        DataListPage dataListPage = ddapPage.getNavBar().goToData();
        DataDetailPage thousandGenomesDetailPage = dataListPage
                .findDataByName("1000 Genomes")
                .clickViewButton();

        String viewId = "Beacon Discovery Access";
        ExpandedAccessibleViewItem beaconDiscoveryView = thousandGenomesDetailPage.expandViewItem(viewId);
        URI authorizeUrl = beaconDiscoveryView.requestAccess();
        thousandGenomesDetailPage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, DataDetailPage::new);

        beaconDiscoveryView = thousandGenomesDetailPage.expandViewItem(viewId);
        beaconDiscoveryView.assertHasAccessToken();
    }

    @Test(expected = PolicyRequirementFailedException.class)
    public void testRequestAccessForBeaconDiscoveryAccessExpectFailure() throws IOException {
        DataListPage dataListPage = ddapPage.getNavBar().goToData();
        DataDetailPage thousandGenomesDetailPage = dataListPage
            .findDataByName("1000 Genomes")
            .clickViewButton();

        String viewId = "Beacon Discovery Access";
        ExpandedAccessibleViewItem beaconDiscoveryView = thousandGenomesDetailPage.expandViewItem(viewId);
        URI authorizeUrl = beaconDiscoveryView.requestAccess();
        loginStrategy.authorizeForResources(driver, USER_WITHOUT_ACCESS, REALM, authorizeUrl, DataDetailPage::new);
    }

    @Test
    public void shouldFindWorkingDownloadLink() {
        DataListPage dataListPage = ddapPage.getNavBar().goToData();
        DataDetailPage thousandGenomesDetailPage = dataListPage
                .findDataByName("1000 Genomes")
                .clickViewButton();

        ExpandedAccessibleViewItem fullFileReadView = thousandGenomesDetailPage.expandViewItem("Full File Read Access");
        String downloadHref = fullFileReadView.getDownloadLink();

        /*
         * Clicking a link that opens in a new tab is difficult to do, so instead let's just
         * check that the href works directly.
         */
        given()
                .log().method()
                .log().uri()
                .auth().preemptive().basic(basicUsername, basicPassword)
                .when()
                .get(downloadHref)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/zip");
    }

}
