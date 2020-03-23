package com.dnastack.ddap.common.page;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class Covid19BeaconPage extends AnyDdapPage {
    public Covid19BeaconPage(WebDriver driver) {
        super(driver);
        waitForInflightRequests();
    }

    public void getMoreDetailsOn(String sourceName) {
        String xpath = String.format("//span[@role='gridcell'][@aria-colindex='1'][contains(text(), '%s')]", sourceName);
        driver.findElement(By.xpath(xpath)).click();
    }

    public void searchFor(String position, String referenceBases, String alternateBases) {
        WebElement positionInput = driver.findElement(By.cssSelector(".beacon-search-parameters .input-control:nth-child(1) input"));
        positionInput.clear();
        positionInput.sendKeys(position);

        WebElement referenceBasesInput = driver.findElement(By.cssSelector(".beacon-search-parameters .input-control:nth-child(2) input"));
        referenceBasesInput.clear();
        referenceBasesInput.sendKeys(referenceBases);

        WebElement alternateBasesInput = driver.findElement(By.cssSelector(".beacon-search-parameters .input-control:nth-child(3) input"));
        alternateBasesInput.clear();
        alternateBasesInput.sendKeys(alternateBases);

        WebElement submitButton = driver.findElement(By.cssSelector(".beacon-search-actions button"));
        new WebDriverWait(driver, 15).until(ExpectedConditions.elementToBeClickable(submitButton));
        submitButton.click();

        waitForInflightRequests();
    }

    public void haveDetailsPanelShown() {
        By selector = By.cssSelector("mat-drawer");
        new WebDriverWait(driver, 5).until(ExpectedConditions.numberOfElementsToBe(selector, 1));
    }

    public void seeInfoInDetailsPanel(String text) {
        String xpath = String.format("//ddap-key-value-pair/div/div[contains(@class, 'ng-star-inserted')][contains(text(), '%s')]", text);
        driver.findElement(By.xpath(xpath));
    }
}
