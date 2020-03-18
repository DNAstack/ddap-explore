package com.dnastack.ddap.frontend;

import com.dnastack.ddap.common.fragments.NavBar;
import com.dnastack.ddap.common.page.AdminDdapPage;
import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.SearchResourcesPage;
import com.dnastack.ddap.common.page.TablesPage;
import com.dnastack.ddap.common.util.DdapBy;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.Augmenter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static org.junit.Assert.assertTrue;

public class SearchE2eTest extends AbstractFrontendE2eTest{

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
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
}
