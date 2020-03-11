package com.dnastack.ddap.common.page;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.function.Function;

public class StandaloneLoginPage {
    private final WebDriver driver;

    public StandaloneLoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public <T extends AnyDdapPage> T logInAs(String username, String password, Function<WebDriver, T> pageConstructor) {
        testAadIdpLogin(driver, username, password);
        return pageConstructor.apply(driver);
    }

    private void testAadIdpLogin(WebDriver driver, String aadUsername, String aadPasword) {
        sendKeysToInput(driver, "//input[contains(@type,'email')]", aadUsername, true);
        WebElement next = driver.findElement(By.xpath("//input[contains(@type, 'submit')]"));
        next.click();
        becomeHumanAndSlowSpeed(3000L);
        sendKeysToInput(driver, "//input[contains(@type,'password')]", aadPasword, true);
        WebElement submit = driver.findElement(By.xpath("//input[contains(@type, 'submit')]"));
        becomeHumanAndSlowSpeed(3000L);
        submit.click();
        new WebDriverWait(driver,10L)
                .until(webDriver -> webDriver.findElement(By.xpath("//a[contains(@data-se, 'btn-authorize')]")));
    }

    private void sendKeysToInput(WebDriver driver, String elementPath, String text, boolean retry) {
        WebElement element = new WebDriverWait(driver, 10)
                .until(w -> w.findElement(By.xpath(elementPath)));
        try {
            for (char c : text.toCharArray()) {
                element.sendKeys("" + c);
                becomeHumanAndSlowSpeed(100L);
            }
        } catch (StaleElementReferenceException e) {
            if (retry) {
                sendKeysToInput(driver, elementPath, text, false);
            }
        }
    }

    private void becomeHumanAndSlowSpeed(Long millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}
