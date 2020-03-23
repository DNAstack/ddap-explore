package com.dnastack.ddap.common.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class WorkflowRegistryPage extends AnyDdapPage {

    public WorkflowRegistryPage(WebDriver driver) {
        super(driver);
        waitForInflightRequests();
    }

    public void filterList(String query) {
        driver.findElement(By.cssSelector(".filter-input input")).sendKeys(query);

    }

    public void selectWorkflowByName(String name) {
        driver.findElement(By.cssSelector(workflowSelector(name))).click();
    }

    public void selectWorkflowTab(String name, int oneBasedTabIndex) {
        By selector = By.cssSelector(String.format("%s .mat-tab-label:nth-child(%d)", workflowSelector(name), oneBasedTabIndex));
        WebElement tab = driver.findElement(selector);
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(tab));
        tab.click();
    }

    public void selectDescriptor(String name, String versionName, String descriptorType) {
        By selector = By.cssSelector(String.format(
                "%s .version-descriptor-selector[data-version-name=\"%s\"][data-descriptor-type=\"%s\"]",
                workflowSelector(name),
                versionName,
                descriptorType
        ));
        WebElement tab = driver.findElement(selector);
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(tab));
        tab.click();
    }

    public void shouldSeeCodeEditorFor(String versionName, String descriptorType) {
        List<By> expectedElementList = new ArrayList<By>();
        expectedElementList.add(By.xpath(String.format("//div[contains(@class, 'code-editor-header')]/h1/span[contains(@class, 'version-name')][contains(text(), '%s')]", versionName)));
        expectedElementList.add(By.xpath(String.format("//div[contains(@class, 'code-editor-header')]/h1/span[contains(@class, 'descriptor-type')][contains(text(), '%s')]", descriptorType)));

        expectedElementList.forEach(selector -> {
            new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(selector));
        });
    }

    public WorkflowManagePage transferToWorkflowManagePage() {
        WebElement button = driver.findElement(By.cssSelector(".transfer-to-manage-trigger"));
        button.click();

        return new WorkflowManagePage(driver);
    }

    public String workflowSelector(String name) {
        return "mat-expansion-panel[data-name=\"" + name + "\"]";
    }
}
