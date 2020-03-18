package com.dnastack.ddap.common.page;

import com.dnastack.ddap.common.util.DdapBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TablesPage extends AnyDdapPage{
    public TablesPage(WebDriver driver) {
        super(driver);
    }

    public void searchTable() {
        WebDriver driver = getDriver();
        new WebDriverWait(driver, 30)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//mat-expansion-panel[3]")));
        driver.findElement(By.xpath("//mat-expansion-panel[3]")).click();
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-se='preview-table-3']")));
        driver.findElement(By.xpath("//button[@data-se='preview-table-3']")).click();
        driver.findElement(DdapBy.se("run-query")).click();
    }
}
