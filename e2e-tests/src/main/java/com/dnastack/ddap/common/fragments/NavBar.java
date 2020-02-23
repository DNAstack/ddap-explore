package com.dnastack.ddap.common.fragments;

import com.dnastack.ddap.common.page.*;
import com.dnastack.ddap.common.util.DdapBy;
import lombok.Value;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Optional;
import java.util.function.Function;

public class NavBar {

    private WebDriver driver;

    @Value
    public static class NavLink {
        private String title;
        private By selector;
        private NavLink parentSelector;

        public Optional<NavLink> getParentSelector() {
            return Optional.ofNullable(parentSelector);
        }
        public Optional<String> getTitle() {
            return Optional.ofNullable(title);
        }
    }

    public static NavLink dataLink() {
        return new NavLink("Collections", DdapBy.se("nav-data-collections"), null);
    }

    public static NavLink workflowLink() {
        return new NavLink("Run", DdapBy.se("nav-analytics-run"), null);
    }

    public NavBar(WebDriver driver) {
        this.driver = driver;
    }

    public boolean existsInNavBar(NavLink item) {
        return driver.findElements(item.getSelector()).size() > 0;
    }

    public NavBar goTo(NavLink navItem) {
        return goTo(navItem, NavBar::new);
    }

    public <T> T goTo(NavLink navItem, Function<WebDriver, T> pageFactory) {
        final WebElement clickableNavLink = navItem.getParentSelector()
                                                   .map(parent -> getChildLink(navItem, parent))
                                                   .orElseGet(() -> driver.findElement(navItem.getSelector()));
        clickableNavLink.click();

        return pageFactory.apply(driver);
    }

    private WebElement getChildLink(NavLink navItem, NavLink parent) {
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.elementToBeClickable(parent.getSelector()));
        final WebElement parentElement = driver.findElement(parent.getSelector());
        parentElement.click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.elementToBeClickable(parentElement.findElement(navItem.getSelector())));

        return parentElement.findElement(navItem.getSelector());
    }

    public DataListPage goToData() {
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.elementToBeClickable(DdapBy.se("product-app-menu")));
        driver.findElement(DdapBy.se("product-app-menu")).click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.elementToBeClickable(DdapBy.se("product-app-menu-data")));
        driver.findElement(DdapBy.se("product-app-menu-data")).click();
        driver.findElement(dataLink().getSelector()).click();

        return new DataListPage(driver);
    }

    public WorkflowWesServersPage goToWorkflows() {
        driver.findElement(DdapBy.se("product-app-menu")).click();
        driver.findElement(DdapBy.se("product-app-menu-analytics")).click();
        driver.findElement(workflowLink().getSelector()).click();

        return new WorkflowWesServersPage(driver);
    }

    public void goToApp(String appSelector) {
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.elementToBeClickable(DdapBy.se("product-app-menu")));
        driver.findElement(DdapBy.se("product-app-menu")).click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.elementToBeClickable(DdapBy.se(appSelector)));
        driver.findElement(DdapBy.se(appSelector)).click();
    }

    public WorkflowManagePage goToRun() {
        driver.findElement(workflowLink().getSelector()).click();
        return new WorkflowManagePage(driver);
    }

    private WebElement getRealmInput() {
        return driver.findElement(DdapBy.se("realm-input"));
    }

    public void setRealm(String targetRealm) {
        new WebDriverWait(driver, 5)
            .until(ExpectedConditions.elementToBeClickable(driver.findElement(DdapBy.se("realm-menu"))));
        driver.findElement(DdapBy.se("realm-menu")).click();
        new WebDriverWait(driver, 5)
            .until(ExpectedConditions.elementToBeClickable(driver.findElement(DdapBy.se("edit-realm"))));
        driver.findElement(DdapBy.se("edit-realm")).click();
        WebElement realmInput = getRealmInput();
        realmInput.clear();
        realmInput.sendKeys(targetRealm);
        driver.findElement(DdapBy.se("update-realm")).click();
    }

    public String getRealm() {
        return driver.findElement(DdapBy.se("realm-name")).getText();
    }

    public ICLoginPage logOut() {
        driver.findElement(DdapBy.se("nav-logout")).click();
        return new ICLoginPage(driver);
    }

}
