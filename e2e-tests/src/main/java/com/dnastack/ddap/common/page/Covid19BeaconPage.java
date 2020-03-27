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
        By selector = By.xpath(xpath);
        new WebDriverWait(driver, 60).until(ExpectedConditions.visibilityOfElementLocated(selector));
        driver.findElement(selector).click();
    }

    public void searchFor(String position, String referenceBases, String alternateBases) {
        By submitButtonSelector = By.cssSelector(".beacon-search-actions button");
        new WebDriverWait(driver, 60).until(ExpectedConditions.elementToBeClickable(submitButtonSelector));
        WebElement submitButton = driver.findElement(submitButtonSelector);

        WebElement positionInput = driver.findElement(By.cssSelector(".beacon-search-parameters input[name='position']"));
        positionInput.clear();
        positionInput.sendKeys(position);

        WebElement referenceBasesInput = driver.findElement(By.cssSelector(".beacon-search-parameters input[name='reference']"));
        referenceBasesInput.clear();
        referenceBasesInput.sendKeys(referenceBases);

        WebElement alternateBasesInput = driver.findElement(By.cssSelector(".beacon-search-parameters input[name='alternate']"));
        alternateBasesInput.clear();
        alternateBasesInput.sendKeys(alternateBases);

        submitButton.click();

        waitForInflightRequests();
    }

    public void haveDetailsPanelShown() {
        By selector = By.cssSelector("mat-drawer");
        new WebDriverWait(driver, 15).until(ExpectedConditions.numberOfElementsToBe(selector, 1));
    }

    public void seeInfoInDetailsPanel(String text) {
        String xpath = String.format("//ddap-key-value-pair/div/div[contains(@class, 'ng-star-inserted')][contains(text(), '%s')]", text);
        driver.findElement(By.xpath(xpath));
    }
}
