package com.dnastack.ddap.common.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchResourcesPage extends AnyDdapPage {
    public SearchResourcesPage(WebDriver driver) {
        super(driver);
    }

    public void exploreResource() {
        WebDriver driver = getDriver();
        new WebDriverWait(driver, 30)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//mat-card[1]")));
        driver.findElement(By.xpath("//mat-card[1]")).click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-se='explore-resource']")));
        driver.findElement(By.xpath("//button[@data-se='explore-resource']")).click();
    }
    
}
