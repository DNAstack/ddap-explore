package com.dnastack.ddap.common.page;

import com.dnastack.ddap.common.util.DdapBy;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.numberOfElementsToBeMoreThan;

public class SearchPage extends AnyDdapPage {

    private static final String SEARCH_REVEAL_BUTTON_SELECTOR = "search-reveal";
    private static final String SEARCH_INPUT_SELECTOR = "search-input";

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    public void openSearchInput() {
        getDriver().findElement(By.xpath("//*[@data-se=\"" + SEARCH_REVEAL_BUTTON_SELECTOR + "\"]"))
            .click();
    }

    public void submitSearchQuery(String query) {
        WebElement searchQueryInput = getDriver().findElement(By.xpath("//*[@data-se=\"" + SEARCH_INPUT_SELECTOR + "\"]"));
        searchQueryInput.clear();
        searchQueryInput.sendKeys(query + Keys.ENTER);
    }

    public List<WebElement> getSearchResults(int expectedNumberOfResults, Consumer<List<WebElement>> assertions) {
        return new WebDriverWait(getDriver(), 15)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(AssertionError.class)
                .until(d -> {
                    final List<WebElement> foundResults = d.findElements(By.tagName("ddap-beacon-result"));
                    assertThat(foundResults, hasSize(greaterThanOrEqualTo(expectedNumberOfResults)));

                    assertions.accept(foundResults);

                    return foundResults;
                });
    }

    public void clickBack() {
        getDriver().findElement(DdapBy.se("search-back-link"))
                .click();
    }


    public void clickBeaconLink(String linkName){
        getDriver().findElement(DdapBy.se(linkName + "-link"))
                .click();
    }

    public void clickLimitSearch() {
        getDriver().findElement(DdapBy.se("limit-search"));
    }

    public URI requestAccess() {
        WebElement accessBtn = driver.findElement(DdapBy.se("btn-authorize"));
        new WebDriverWait(driver, 5).until(ExpectedConditions.elementToBeClickable(accessBtn));
        return URI.create(accessBtn.getAttribute("href"));
    }
}
