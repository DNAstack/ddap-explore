package com.dnastack.ddap.common.fragments;

import com.dnastack.ddap.common.page.DataDetailPage;
import com.dnastack.ddap.common.util.WebPageScroller;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static java.lang.String.format;

public class DataListItem {

    private WebDriver driver;
    private String resourceName;

    public DataListItem(WebDriver driver, String resourceName) {
        this.driver = driver;
        this.resourceName = resourceName;
        getListItem();
    }

    public DataDetailPage goToDetails() {
        final WebElement listItem = getListItem();
        // need to wait for button to become visible.
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(listItem));
        WebPageScroller.scrollTo(driver, listItem);
        listItem.click();

        return new WebDriverWait(driver, 5).until(DataDetailPage::new);
    }

    private WebElement getListItem() {
        final WebElement we = driver.findElement(By.xpath(format(
                "//mat-card[descendant::*[contains(text(), '%s') and @class='mat-card-title']]",
                resourceName)));
        new WebDriverWait(driver, 50).until(d -> we.isDisplayed());
        return we;
    }

}
