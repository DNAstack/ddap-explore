package com.dnastack.ddap.frontend;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static com.dnastack.ddap.common.WorkflowRunState.COMPLETE;
import static com.dnastack.ddap.common.WorkflowRunState.QUEUED;
import static com.dnastack.ddap.common.WorkflowRunState.RUNNING;
import static java.util.Arrays.asList;

import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.WorkflowListPage;
import com.dnastack.ddap.common.page.WorkflowManagePage;
import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.EnvUtil;
import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

@SuppressWarnings("Duplicates")
@Slf4j
public class WorkflowE2eTest extends AbstractFrontendE2eTest {


    private static String datasetUrl = EnvUtil.optionalEnv(
        "E2E_DATASET_URL",
        "https://storage.googleapis.com/ddap-e2etest-objects/table/dnastack-internal-subjects-with-objects/data"
    );
    private static String securedDatasetUrl = EnvUtil.optionalEnv(
        "E2E_SECURED_DATASET_URL",
        "https://storage.googleapis.com/ddap-e2etest-objects/table/subjects-restricted-access/data"
    );
    private static Integer maxWaitTimeInMinutes = Integer
        .valueOf(EnvUtil.optionalEnv("E2E_WORKFLOW_MAX_WAIT_TIME_IN_MINUTES", "2"));

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AnyDdapPage::new);
    }

    @Test
    public void testSingleWorkflowExecutionWithTokens() throws IOException {
        //        WorkflowWesServersPage workflowWesServersPage = ddapPage.getNavBar()
        //                .goToWorkflows();
        //        WorkflowManagePage managePage = workflowWesServersPage.clickManage();
        ddapPage.getNavBar().goToApp("product-app-menu-analytics");
        // FIXME Assert that the right app is present
        WorkflowManagePage managePage = ddapPage.getNavBar().goToRun();

        log.info("Workflow Execution Step: Dataset import");
        managePage.fetchDatasetResult(datasetUrl);
        managePage.waitForInflightRequests();
        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.selectColumn("bam_file");
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        log.info("Workflow Execution Step: WDL");
        managePage.typeInEditor(By.cssSelector("ngx-monaco-editor .monaco-editor textarea"), loadTemplate("/com/dnastack/ddap/workflow/with-tokens-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Inputs");
        managePage.fillFieldFromDropdown(By.name("md5Sum.inputFile"), "bam_file");
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
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE), maxWaitTimeInMinutes);
    }

    @Test
    public void testMultipleWorkflowExecutionWithTokens() throws IOException {
        //        WorkflowWesServersPage workflowWesServersPage = ddapPage.getNavBar()
        //            .goToWorkflows();
        //        WorkflowManagePage managePage = workflowWesServersPage.clickManage();
        ddapPage.getNavBar().goToApp("product-app-menu-analytics");
        // FIXME Assert that the right app is present
        WorkflowManagePage managePage = ddapPage.getNavBar().goToRun();

        log.info("Workflow Execution Step: Dataset import");
        managePage.fetchDatasetResult(datasetUrl);
        managePage.waitForInflightRequests();
        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.clickCheckbox(DdapBy.se("checkbox-2"));
        managePage.clickCheckbox(DdapBy.se("checkbox-3"));
        managePage.selectColumn("bam_file");
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        log.info("Workflow Execution Step: WDL");
        managePage.fillField(DdapBy
            .se("inp-workflow-wdl"), loadTemplate("/com/dnastack/ddap/workflow/with-tokens-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Inputs");
        managePage.fillFieldFromDropdown(By.name("md5Sum.inputFile"), "bam_file");
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
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE), maxWaitTimeInMinutes);
    }

    @Test
    public void testSingleWorkflowExecutionWithTokensFromSecuredDataset() throws IOException {
        //        WorkflowWesServersPage workflowWesServersPage = ddapPage.getNavBar()
        //            .goToWorkflows();
        //        WorkflowManagePage managePage = workflowWesServersPage.clickManage();
        ddapPage.getNavBar().goToApp("product-app-menu-analytics");
        // FIXME Assert that the right app is present
        WorkflowManagePage managePage = ddapPage.getNavBar().goToRun();

        log.info("Workflow Execution Step: Dataset import");
        // Try to fetch without access token
        managePage.tryFetchDatasetResult(securedDatasetUrl);
        managePage.waitForInflightRequests();

        log.info("Workflow Execution Step: Authorizing for dataset resource");
        URI authorizeDatasetUrl = managePage.requestAccess("btn-authorize-dataset");
        managePage = loginStrategy
            .authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeDatasetUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        log.info("Workflow Execution Step: Secured dataset import");
        // Need to re-fetch with access token
        managePage.fetchDatasetResult(securedDatasetUrl);
        managePage.waitForInflightRequests();

        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        log.info("Workflow Execution Step: WDL");
        managePage
            .fillField(DdapBy.se("inp-workflow-wdl"), loadTemplate("/com/dnastack/ddap/workflow/simple-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        log.info("Workflow Execution Step: Inputs");
        managePage.fillFieldFromDropdown(By.name("test.name"), "blood_type");
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
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE), maxWaitTimeInMinutes);
    }

}
