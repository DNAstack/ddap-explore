package com.dnastack.ddap.common.page;

import com.dnastack.ddap.common.WorkflowRunState;
import com.dnastack.ddap.common.util.DdapBy;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.openqa.selenium.support.ui.ExpectedConditions.numberOfElementsToBeMoreThan;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

@Slf4j
public class WorkflowListPage extends AnyDdapPage {

    private List<String> workflowRunIds = new ArrayList<>();

    public WorkflowListPage(WebDriver driver, Integer expectedNumberOfNewWorkflowRuns) {
        super(driver);
        waitForInflightRequests();
        WebElement pageTitle = driver.findElement(DdapBy.se("page-title"));
        assertThat(pageTitle.getText(), equalTo("Operations"));
        extractNewWorkflowIdsIfExists(expectedNumberOfNewWorkflowRuns);
    }

    private void extractNewWorkflowIdsIfExists(Integer expectedNumberOfNewWorkflowRuns) {
        workflowRunIds = driver.findElements(DdapBy.se("new-run-id"))
                .stream()
                .map(WebElement::getText)
                .collect(toList());
        if (expectedNumberOfNewWorkflowRuns != null) {
            assertThat(workflowRunIds, hasSize(equalTo(expectedNumberOfNewWorkflowRuns)));
        }
    }

    public WorkflowManagePage clickManage() {
        driver.findElement(DdapBy.se("btn-manage"))
                .click();
        return new WorkflowManagePage(driver);
    }

    public WorkflowDetailPage viewRunDetails() {
        new WebDriverWait(driver, 10)
                .until(numberOfElementsToBeMoreThan(DdapBy.se("run"), 0));
        List<WebElement> runs = driver.findElements(DdapBy.se("run"));
        runs.get(0).click();
        new WebDriverWait(driver, 2).until(visibilityOfElementLocated(DdapBy.se("view-btn"))).click();
        // Fixes failure where detail page hadn't loaded yet
        return new WebDriverWait(driver, 10).until(WorkflowDetailPage::new);
    }

    public void assertNewRunsInState(List<WorkflowRunState> acceptedStates, int maxWaitTimeInMinutes) {
        reloadPageUntilNewRunsNotInState(acceptedStates, LocalDateTime.now(), maxWaitTimeInMinutes);
    }

    private void reloadPageUntilNewRunsNotInState(List<WorkflowRunState> acceptedStates, LocalDateTime startedAt, int maxWaitTimeInMinutes) {
        waitForInflightRequests();
        List<WebElement> allRuns = driver.findElements(DdapBy.se("run"));
        List<WebElement> newRuns = allRuns.stream()
            .filter((run) -> {
                String runId = run.findElement(DdapBy.se("run-id")).getText();
                return workflowRunIds.contains(runId);
            })
            .collect(toList());
        final Map<String, String> runStates = newRuns.stream()
            .collect(toMap(run -> run.findElement(DdapBy.se("run-id")).getText(),
                run -> run.findElement(DdapBy.se("run-state")).getText()));
        boolean allInExpectedState = runStates
            .values()
            .stream()
            .allMatch((state) -> acceptedStates.stream()
                .map(Enum::name)
                .anyMatch(s -> s.equals(state)));

        if (newRuns.isEmpty() || !allInExpectedState) {
            LocalDateTime stopTryingAt = LocalDateTime.from(startedAt.plusMinutes(maxWaitTimeInMinutes));
            MatcherAssert.assertThat(String.format("Failed to assert states of workflow runs [%s] within valid reload duration.\nExpected states: %s\nFound states:%s\n", workflowRunIds, acceptedStates, runStates), LocalDateTime.now(), lessThanOrEqualTo(stopTryingAt));
            driver.navigate().refresh();
            reloadPageUntilNewRunsNotInState(acceptedStates, startedAt, maxWaitTimeInMinutes);
        }
    }

}
