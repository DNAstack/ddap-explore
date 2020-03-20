package com.dnastack.ddap.frontend;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static com.dnastack.ddap.common.WorkflowRunState.COMPLETE;
import static com.dnastack.ddap.common.WorkflowRunState.QUEUED;
import static com.dnastack.ddap.common.WorkflowRunState.RUNNING;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.WorkflowListPage;
import com.dnastack.ddap.common.page.WorkflowManagePage;
import com.dnastack.ddap.common.setup.ConfigModel;
import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.EnvUtil;
import java.io.IOException;
import java.net.URI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

@SuppressWarnings("Duplicates")
@Slf4j
public class WorkflowE2eTest extends AbstractFrontendE2eTest {

    private static WorkflowTestConfig workflowTestConfig;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        try {
            workflowTestConfig = EnvUtil
                .optionalEnvConfig("E2E_TEST_WORKFLOW_CONFIG", new WorkflowTestConfig(), WorkflowTestConfig.class);
            Assume.assumeTrue("RealmE2eTest has been disabled, and will not run.", workflowTestConfig.isEnabled());
            ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AnyDdapPage::new);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


    @Test
    public void testSingleWorkflowExecution() throws Exception {
        ddapPage.getNavBar().goToApp("product-app-menu-analytics");
        // FIXME Assert that the right app is present
        WorkflowManagePage managePage = ddapPage.getNavBar().goToRun();

        log.info("Workflow Execution Step: Dataset import");
        managePage.fetchDatasetResult(workflowTestConfig.getDatasetUrl());
        managePage.waitForInflightRequests();
        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        log.info("Workflow Execution Step: WDL");
        managePage.typeInEditor(By
            .cssSelector("ngx-monaco-editor .monaco-editor textarea"), loadTemplate("/com/dnastack/ddap/workflow/simple-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Inputs");
        managePage.fillFieldFromDropdown(By.name("test.name"), workflowTestConfig.getDataColumn());
        managePage.clickButton(DdapBy.se("btn-next-to-wes-server"));
        managePage.fillFieldWithFirstValueFromDropdown(DdapBy.se("inp-workflow-wes-view"));
        managePage.waitForInflightRequests();
        managePage.clickButton(DdapBy.se("btn-next-to-auth"));

        log.info("Workflow Execution Step: Authorizing for resources");
        URI authorizeUrl = managePage.requestAccess("btn-authorize");
        managePage = loginStrategy
            .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        log.info("Workflow Execution Step: Execution");
        WorkflowListPage workflowListPage = managePage.executeWorkflows(1);
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Asserting new workflows are in expected state");
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE), workflowTestConfig.getMaxWaitTime());
    }

    @Test
    public void testSingleWorkflowExecutionWithTokens() throws IOException {
        Assume
            .assumeTrue("Performing additional authorization for files in workflows has been disabled, and will not be tested", workflowTestConfig
                .isAdditionalAuthorizationEnabled());
        ddapPage.getNavBar().goToApp("product-app-menu-analytics");
        // FIXME Assert that the right app is present
        WorkflowManagePage managePage = ddapPage.getNavBar().goToRun();

        log.info("Workflow Execution Step: Dataset import");
        managePage.fetchDatasetResult(workflowTestConfig.getDatasetUrl());
        managePage.waitForInflightRequests();
        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.selectColumn(workflowTestConfig.getColumnToAuthorize());
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        log.info("Workflow Execution Step: WDL");
        managePage.typeInEditor(By
            .cssSelector("ngx-monaco-editor .monaco-editor textarea"), loadTemplate("/com/dnastack/ddap/workflow/with-tokens-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Inputs");
        managePage.fillFieldFromDropdown(By.name("md5Sum.inputFile"), workflowTestConfig.getColumnToAuthorize());
        managePage.clickButton(DdapBy.se("btn-next-to-wes-server"));
        managePage.fillFieldWithFirstValueFromDropdown(DdapBy.se("inp-workflow-wes-view"));
        managePage.waitForInflightRequests();
        managePage.clickButton(DdapBy.se("btn-next-to-auth"));

        log.info("Workflow Execution Step: Authorizing for resources");
        URI authorizeUrl = managePage.requestAccess("btn-authorize");
        managePage = loginStrategy
            .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        log.info("Workflow Execution Step: Execution");
        WorkflowListPage workflowListPage = managePage.executeWorkflows(1);
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Asserting new workflows are in expected state");
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE), workflowTestConfig.getMaxWaitTime());
    }

    @Test
    public void testMultipleWorkflowExecutionWithTokens() throws IOException {
        Assume
            .assumeTrue("Performing additional authorization for files in workflows has been disabled, and will not be tested", workflowTestConfig
                .isAdditionalAuthorizationEnabled());
        ddapPage.getNavBar().goToApp("product-app-menu-analytics");
        // FIXME Assert that the right app is present
        WorkflowManagePage managePage = ddapPage.getNavBar().goToRun();

        log.info("Workflow Execution Step: Dataset import");
        managePage.fetchDatasetResult(workflowTestConfig.getDatasetUrl());
        managePage.waitForInflightRequests();
        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.clickCheckbox(DdapBy.se("checkbox-2"));
        managePage.clickCheckbox(DdapBy.se("checkbox-3"));
        managePage.selectColumn(workflowTestConfig.getColumnToAuthorize());
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        log.info("Workflow Execution Step: WDL");
        managePage.typeInEditor(By
            .cssSelector("ngx-monaco-editor .monaco-editor textarea"), loadTemplate("/com/dnastack/ddap/workflow/with-tokens-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Inputs");
        managePage.fillFieldFromDropdown(By.name("md5Sum.inputFile"), workflowTestConfig.getColumnToAuthorize());
        managePage.clickButton(DdapBy.se("btn-next-to-wes-server"));
        managePage.fillFieldWithFirstValueFromDropdown(DdapBy.se("inp-workflow-wes-view"));
        managePage.waitForInflightRequests();
        managePage.clickButton(DdapBy.se("btn-next-to-auth"));

        log.info("Workflow Execution Step: Authorizing for resources");
        URI authorizeUrl = managePage.requestAccess("btn-authorize");
        managePage = loginStrategy
            .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        log.info("Workflow Execution Step: Execution");
        WorkflowListPage workflowListPage = managePage.executeWorkflows(3);
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Asserting new workflows are in expected state");
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE), workflowTestConfig.getMaxWaitTime());
    }

    @Test
    public void testSingleWorkflowExecutionWithTokensFromSecuredDataset() throws IOException {
        Assume
            .assumeTrue("Loading secured datasets for the current deployment has been disabled and will not be tested", workflowTestConfig
                .isSecuredDatasetEnabled());
        ddapPage.getNavBar().goToApp("product-app-menu-analytics");
        // FIXME Assert that the right app is present
        WorkflowManagePage managePage = ddapPage.getNavBar().goToRun();

        log.info("Workflow Execution Step: Dataset import");
        // Try to fetch without access token
        managePage.tryFetchDatasetResult(workflowTestConfig.getSecuredDatasetUrl());
        managePage.waitForInflightRequests();

        log.info("Workflow Execution Step: Authorizing for dataset resource");
        URI authorizeDatasetUrl = managePage.requestAccess("btn-authorize-dataset");
        managePage = loginStrategy
            .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeDatasetUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        log.info("Workflow Execution Step: Secured dataset import");
        // Need to re-fetch with access token
        managePage.fetchDatasetResult(workflowTestConfig.getSecuredDatasetUrl());
        managePage.waitForInflightRequests();

        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        log.info("Workflow Execution Step: WDL");
        managePage.typeInEditor(By
            .cssSelector("ngx-monaco-editor .monaco-editor textarea"), loadTemplate("/com/dnastack/ddap/workflow/simple-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Inputs");
        managePage.fillFieldFromDropdown(By.name("test.name"), workflowTestConfig.getSecuredDataColumn());
        managePage.clickButton(DdapBy.se("btn-next-to-wes-server"));
        managePage.fillFieldWithFirstValueFromDropdown(DdapBy.se("inp-workflow-wes-view"));
        managePage.waitForInflightRequests();
        managePage.clickButton(DdapBy.se("btn-next-to-auth"));

        log.info("Workflow Execution Step: Authorizing for resources");
        URI authorizeUrl = managePage.requestAccess("btn-authorize");
        managePage = loginStrategy
            .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        log.info("Workflow Execution Step: Execution");
        WorkflowListPage workflowListPage = managePage.executeWorkflows(1);
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Asserting new workflows are in expected state");
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE), workflowTestConfig.getMaxWaitTime());
    }


    @Data
    public static class WorkflowTestConfig implements ConfigModel {

        private boolean enabled = true;
        private String datasetUrl;
        private String dataColumn;

        private boolean additionalAuthorizationEnabled = true;
        private String columnToAuthorize;


        private boolean securedDatasetEnabled = true;
        private String securedDatasetUrl;
        private String securedDataColumn;

        private int maxWaitTime = 2;

        @Override
        public void validateConfig() {
            if (enabled) {
                assertThat(datasetUrl, Matchers.notNullValue());
                assertThat(dataColumn, Matchers.notNullValue());

                if (additionalAuthorizationEnabled) {
                    assertThat(columnToAuthorize, Matchers.notNullValue());
                }

                if (securedDatasetEnabled) {
                    assertThat(securedDatasetUrl, Matchers.notNullValue());
                    assertThat(securedDataColumn, Matchers.notNullValue());
                }
            }
        }
    }

}
