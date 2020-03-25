package com.dnastack.ddap.common.page;

import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.WebPageScroller;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.util.List;

public class WorkflowManagePage extends AnyDdapPage {

    public WorkflowManagePage(WebDriver driver) {
        super(driver);
        waitForInflightRequests();
    }

    public void clearField(By fieldSelector) {
        String selectAll = Keys.chord(Keys.CONTROL, "a");
        WebElement formInput = driver.findElement(fieldSelector);
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(formInput));
        formInput.sendKeys(selectAll);
        formInput.sendKeys(Keys.DELETE);
    }

    public void fillField(By fieldSelector, String fieldValue) {
        WebElement formInput = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.elementToBeClickable(fieldSelector));
        formInput.sendKeys(fieldValue);
    }

    public void fillFieldFromDropdown(By fieldSelector, String fieldValue) {
        WebElement field = driver.findElement(fieldSelector);

        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.elementToBeClickable(field));
        // This dismisses any previous auto-complete suggestions in other fields.
        field.sendKeys(Keys.ENTER);

        List<WebElement> options = driver.findElements(By.tagName("mat-option"));

        if (fieldValue != null) {
            WebElement option =
                    new WebDriverWait(driver, 5)
                            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                                    "//mat-option/span[contains(text(), '" + fieldValue + "')]")));

            option.click();
        } else {
            options.get(0).click();
        }
    }

    public void fillFieldWithFirstValueFromDropdown(By fieldSelector) {
        fillFieldFromDropdown(fieldSelector, null);
    }

    public void clickButton(By selector) {
        WebElement button = driver.findElement(selector);
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(button));
        WebPageScroller.scrollTo(driver, button);
        button.click();
    }

    public WorkflowListPage executeWorkflows(Integer expectedNumberOfNewWorkflowRuns) {
        clickSave();
        waitForInflightRequests();
        return new WorkflowListPage(driver, expectedNumberOfNewWorkflowRuns);
    }

    public void clickSave() {
        this.clickButton(DdapBy.se("btn-execute"));
    }

    public void tryFetchDatasetResult(String datasetUrl) {
        WebElement datasetInput = driver.findElement(DdapBy.se("dataset-url"));
        WebElement fetchButton = driver.findElement(DdapBy.se("btn-import-dataset"));
        datasetInput.clear();
        datasetInput.sendKeys(datasetUrl);
        fetchButton.click();
    }

    public List<WebElement> fetchDatasetResult(String datasetUrl) {
        WebElement datasetInput = driver.findElement(DdapBy.se("dataset-url"));
        WebElement fetchButton = driver.findElement(DdapBy.se("btn-import-dataset"));
        datasetInput.clear();
        datasetInput.sendKeys(datasetUrl);
        fetchButton.click();
        return datasetResultRows();
    }

    private List<WebElement> datasetResultRows() {
        new WebDriverWait(getDriver(), 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.tagName("ddap-dataset-results")));
        WebElement datasetResults = driver.findElement(By.tagName("ddap-dataset-results"));
        datasetResults.click();
        return datasetResults.findElements(By.xpath("//mat-row"));
    }

    public void selectColumn(String columnName) {
        fillFieldFromDropdown(DdapBy.se("select-column"), columnName);
        closeDropdown();
    }

    public void clickCheckbox(By checkboxSelector) {
        WebElement checkbox = driver.findElement(checkboxSelector);
        WebPageScroller.scrollTo(driver, checkbox);
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(checkbox));
        checkbox.click();
    }

    public void closeDropdown() {
        driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
    }

    public URI requestAccess(String selector) {
        WebElement accessBtn = driver.findElement(DdapBy.se(selector));
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(accessBtn));
        return URI.create(accessBtn.getAttribute("href"));
    }

    public void typeInEditor(By selector, String content) {
        WebElement element = driver.findElement(selector);
        element.sendKeys(content);
    }

    public int getNumberOfLinesInCodeEditor() {
        return driver.findElements(By.cssSelector("ngx-monaco-editor .view-line")).size();
    }
}
