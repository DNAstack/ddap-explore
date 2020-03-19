package com.dnastack.ddap.frontend;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static com.dnastack.ddap.common.fragments.NavBar.dataLink;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import com.dnastack.ddap.common.fragments.DataListItem;
import com.dnastack.ddap.common.page.AdminDdapPage;
import com.dnastack.ddap.common.page.DataDetailPage;
import com.dnastack.ddap.common.page.DataListPage;
import com.dnastack.ddap.common.page.SearchPage;
import com.dnastack.ddap.common.setup.ConfigModel;
import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.EnvUtil;
import com.dnastack.ddap.common.util.WebDriverCookieHelper;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Slf4j
@SuppressWarnings("Duplicates")
public class BeaconSearchE2eTest extends AbstractFrontendE2eTest {

    private static BeaconSearchTestConfig testConfig;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        testConfig = EnvUtil.optionalEnvConfig("E2E_TEST_BEACON_SEARCH_CONFIG",new BeaconSearchTestConfig(), BeaconSearchTestConfig.class);
        Assume.assumeTrue("BeaconSearchE2eTest tests have been disabled, and will not run.",testConfig.isEnabled());
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AdminDdapPage::new);
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        WebDriverCookieHelper.cleanUpAllCartCookies(driver); // need to remove cart cookies to provide isolation
    }


    @Test
    public void searchBeaconWithValidQueryAnAuthorize() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(testConfig.getQuery());

        if (testConfig.getRequiresAuth()) {
            searchPage.getSearchResults(testConfig.getExpectedNumberOfResults(), results -> {
                assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
            });

            URI authorizeUrl = searchPage.requestAccess();
            searchPage = loginStrategy
                .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);
        }

        searchPage.getSearchResults(testConfig.getExpectedNumberOfResults(), results -> {
            assertTrue(findFirstElementByCssClass(results, "match-success").isPresent());
        });
    }

    @Test
    public void searchBeaconWithInvalidQuery() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(testConfig.getInvalidQuery());

        searchPage.getSearchResults(2, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
        });
    }

    @Test
    public void backLinkFromDataList() {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(testConfig.getQuery());

        searchPage.clickBack();

        driver.findElement(DdapBy.text("Collections", "h2"));
    }

    @Test
    public void backLinkFromDataDetails() {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        DataListPage dataListPage = new DataListPage(driver);
        final DataListItem data = dataListPage.findDataByName(testConfig.getBeaconResourceName());
        DataDetailPage dataDetailPage = data.clickViewButton();
        dataDetailPage.assertResourcePage(testConfig.getBeaconResourceName());

        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(testConfig.getQuery());

        searchPage.clickBack();

        driver.findElement(DdapBy.text(testConfig.getBeaconResourceName(), "h2"));
    }

    @Test
    public void limitSearchFromDataDetails() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        DataListPage dataListPage = new DataListPage(driver);
        final DataListItem data = dataListPage.findDataByName(testConfig.getBeaconResourceName());
        DataDetailPage dataDetailPage = data.clickViewButton();
        dataDetailPage.assertResourcePage(testConfig.getBeaconResourceName());

        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.clickLimitSearch();
        searchPage.submitSearchQuery(testConfig.getQuery());

        if (testConfig.getRequiresAuth()) {
            searchPage.getSearchResults(1, results -> {
                assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
            });

            URI authorizeUrl = searchPage.requestAccess();
            searchPage = loginStrategy
                .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);

        }
        searchPage.getSearchResults(1, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-success").isPresent());
        });
    }

    @Test
    public void limitSearchOnSearchPage() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        DataListPage dataListPage = new DataListPage(driver);
        final DataListItem data = dataListPage.findDataByName(testConfig.getBeaconResourceName());
        DataDetailPage dataDetailPage = data.clickViewButton();
        dataDetailPage.assertResourcePage(testConfig.getBeaconResourceName());

        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.clickLimitSearch();
        searchPage.submitSearchQuery(testConfig.getQuery());

        if (testConfig.getRequiresAuth()) {

            searchPage.getSearchResults(1, results -> {
                assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
            });

            URI authorizeUrl = searchPage.requestAccess();
            searchPage = loginStrategy
                .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);
        }

        searchPage.getSearchResults(1, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-success").isPresent());
        });
    }

    @Test
    public void changeQueryOnSearchPageAndGoBack() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(testConfig.getQuery());

        // Start with a valid search
        if (testConfig.getRequiresAuth()) {
            searchPage.getSearchResults(testConfig.getExpectedNumberOfResults(), results -> {
                assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
            });

            URI authorizeUrl = searchPage.requestAccess();
            searchPage = loginStrategy
                .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);
        }

        searchPage.getSearchResults(testConfig.getExpectedNumberOfResults(), results -> {
            assertTrue(findFirstElementByCssClass(results, "match-success").isPresent());
        });

        // Continue with invalid search
        searchPage.submitSearchQuery(testConfig.getInvalidQuery());
        searchPage.getSearchResults(testConfig.getExpectedNumberOfResults(), results -> {
            assertTrue(findFirstElementByCssClass(results, "match-failure").isPresent());
        });

        // Go back to data list page
        searchPage.clickBack();
        searchPage.waitForInflightRequests();
        searchPage.clickBack();

        driver.findElement(DdapBy.text("Collections", "h2"));
    }

    @Test
    public void testSecureBeacon() throws IOException {
        Assume.assumeNotNull(testConfig.getSecureBeaconResourceName());
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        DataListPage dataListPage = new DataListPage(driver);
        final DataListItem data = dataListPage.findDataByName(testConfig.getSecureBeaconResourceName());
        DataDetailPage dataDetailPage = data.clickViewButton();
        dataDetailPage.assertResourcePage(testConfig.getSecureBeaconResourceName());

        String query = testConfig.getSecureBeaconQuery();
        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.clickLimitSearch();
        searchPage.submitSearchQuery(query);

        searchPage.getSearchResults(0, results -> {
            assertThat(results.size(), is(0));
        });

        searchPage.clickBeaconLink(testConfig.getSecureBeaconName());
        searchPage.getSearchResults(testConfig.getExpectedNumberOfResults(), results -> {
            assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
        });

        URI authorizeUrl = searchPage.requestAccess();
        searchPage = loginStrategy
            .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);

        searchPage.getSearchResults(testConfig.getExpectedNumberOfResults(), results -> {
            assertTrue(findFirstElementByCssClass(results, "match-success").isPresent());
        });
    }

    private Optional<WebElement> findFirstElementByCssClass(List<WebElement> results, String cssClass) {
        for (WebElement result : results) {
            try {
                return Optional.of(result.findElement(By.className(cssClass)));
            } catch (NoSuchElementException nsee) {
                // Deliberately empty
            }
        }

        return Optional.empty();
    }

    @Data
    public static class BeaconSearchTestConfig implements ConfigModel {

        private boolean enabled = true;

        private String query;
        private String invalidQuery;
        private String beaconResourceName;
        private Boolean requiresAuth;
        private Integer expectedNumberOfResults;

        private String secureBeaconResourceName;
        private String secureBeaconQuery;
        private String secureBeaconName;

        @Override
        public void validateConfig() {
            if (enabled) {
                assertThat(query, Matchers.notNullValue());
                assertThat(invalidQuery, Matchers.notNullValue());
                assertThat(beaconResourceName, Matchers.notNullValue());
                assertThat(expectedNumberOfResults, Matchers.notNullValue());
                assertThat(secureBeaconResourceName, Matchers.notNullValue());
                assertThat(secureBeaconQuery, Matchers.notNullValue());
                assertThat(secureBeaconName, Matchers.notNullValue());
            }
        }
    }

}
