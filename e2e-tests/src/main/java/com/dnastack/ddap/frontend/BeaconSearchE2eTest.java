package com.dnastack.ddap.frontend;

import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.fragments.DataListItem;
import com.dnastack.ddap.common.page.AdminDdapPage;
import com.dnastack.ddap.common.page.DataDetailPage;
import com.dnastack.ddap.common.page.DataListPage;
import com.dnastack.ddap.common.page.SearchPage;
import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.WebDriverCookieHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static com.dnastack.ddap.common.fragments.NavBar.dataLink;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
@SuppressWarnings("Duplicates")
public class BeaconSearchE2eTest extends AbstractFrontendE2eTest {

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AdminDdapPage::new);
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        WebDriverCookieHelper.cleanUpAllCartCookies(driver); // need to remove cart cookies to provide isolation
    }

    @Test
    public void searchBeaconWithValidQuery() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        String query = "1 : 156105028 T > C";
        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(query);

        searchPage.getSearchResults(2, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
        });

        URI authorizeUrl = searchPage.requestAccess();
        searchPage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);

        searchPage.getSearchResults(2, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-success").isPresent());
        });
    }

    @Test
    public void searchBeaconWithInvalidQuery() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
            .goTo(dataLink());

        String query = "1 : 1 T > C";
        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(query);

        searchPage.getSearchResults(2, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
        });

        URI authorizeUrl = searchPage.requestAccess();
        searchPage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);

        searchPage.getSearchResults(2, results -> {
            assertFalse(findFirstElementByCssClass(results, "match-success").isPresent());
            assertTrue(findFirstElementByCssClass(results, "match-failure").isPresent());
        });
    }

    @Test
    public void backLinkFromDataList() {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
                .goTo(dataLink());

        String query = "1 : 156105028 T > C";
        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(query);

        searchPage.clickBack();

        driver.findElement(DdapBy.text("Collections","h2"));
    }

    @Test
    public void backLinkFromDataDetails() {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
                .goTo(dataLink());

        DataListPage dataListPage = new DataListPage(driver);
        final DataListItem data = dataListPage.findDataByName("1000 Genomes");
        DataDetailPage dataDetailPage = data.clickViewButton();
        dataDetailPage.assertResourcePage("1000 Genomes");

        String query = "1 : 156105028 T > C";
        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(query);

        searchPage.clickBack();

        driver.findElement(DdapBy.text("1000 Genomes","h2"));
    }

    @Test
    public void limitSearchFromDataDetails() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
                .goTo(dataLink());

        DataListPage dataListPage = new DataListPage(driver);
        final DataListItem data = dataListPage.findDataByName("1000 Genomes");
        DataDetailPage dataDetailPage = data.clickViewButton();
        dataDetailPage.assertResourcePage("1000 Genomes");

        String query = "1 : 156105028 T > C";
        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.clickLimitSearch();
        searchPage.submitSearchQuery(query);

        searchPage.getSearchResults(1, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
        });

        URI authorizeUrl = searchPage.requestAccess();
        searchPage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);

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
        final DataListItem data = dataListPage.findDataByName("1000 Genomes");
        DataDetailPage dataDetailPage = data.clickViewButton();
        dataDetailPage.assertResourcePage("1000 Genomes");

        String query = "1 : 156105028 T > C";
        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.clickLimitSearch();
        searchPage.submitSearchQuery(query);

        searchPage.getSearchResults(1, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
        });

        URI authorizeUrl = searchPage.requestAccess();
        searchPage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);

        searchPage.getSearchResults(1, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-success").isPresent());
        });
    }

    @Test
    public void changeQueryOnSearchPageAndGoBack() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
                .goTo(dataLink());

        String query = "1 : 156105028 T > C";
        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.submitSearchQuery(query);

        // Start with a valid search

        searchPage.getSearchResults(2, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
        });

        URI authorizeUrl = searchPage.requestAccess();
        searchPage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);

        searchPage.getSearchResults(2, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-success").isPresent());
        });

        // Continue with invalid search

        query = "1 : 1 T > C";
        searchPage.submitSearchQuery(query);

        searchPage.getSearchResults(2, results -> {
            assertFalse(findFirstElementByCssClass(results, "match-success").isPresent());
            assertTrue(findFirstElementByCssClass(results, "match-failure").isPresent());
        });

        // Go back to data list page

        searchPage.clickBack();
        searchPage.waitForInflightRequests();
        searchPage.clickBack();

        driver.findElement(DdapBy.text("Collections","h2"));
    }

    @Test
    public void testBRCA2SearchLink() throws IOException {
        ddapPage.getNavBar().goToApp("product-app-menu-data");
        ddapPage.getNavBar()
                .goTo(dataLink());

        DataListPage dataListPage = new DataListPage(driver);
        final DataListItem data = dataListPage.findDataByName("GA4GH APIs");
        DataDetailPage dataDetailPage = data.clickViewButton();
        dataDetailPage.assertResourcePage("GA4GH APIs");

        String query = "C";
        SearchPage searchPage = new SearchPage(driver);
        searchPage.openSearchInput();
        searchPage.clickLimitSearch();
        searchPage.submitSearchQuery(query);

        searchPage.getSearchResults(0, results -> {
            assertThat(results.size(), is(0));
        });

        searchPage.clickBRCA2();

        searchPage.getSearchResults(2, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-error").isPresent());
        });

        URI authorizeUrl = searchPage.requestAccess();
        searchPage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, SearchPage::new);

        searchPage.getSearchResults(2, results -> {
            assertTrue(findFirstElementByCssClass(results, "match-success").isPresent());
            assertFalse(findFirstElementByCssClass(results, "match-failure").isPresent());
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

}
