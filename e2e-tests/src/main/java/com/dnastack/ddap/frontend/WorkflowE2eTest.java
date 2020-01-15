package com.dnastack.ddap.frontend;

import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.page.*;
import com.dnastack.ddap.common.util.DdapBy;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import java.io.IOException;
import java.net.URI;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static com.dnastack.ddap.common.WorkflowRunState.*;
import static java.util.Arrays.asList;

@SuppressWarnings("Duplicates")
public class WorkflowE2eTest extends AbstractFrontendE2eTest {

    private static final String REALM = generateRealmName(WorkflowE2eTest.class.getSimpleName());

    private static String datasetUrl = optionalEnv(
            "E2E_DATASET_URL",
            "https://storage.googleapis.com/ddap-e2etest-objects/dataset/dnastack-internal-subjects-with-objects"
    );
    private static String securedDatasetUrl = optionalEnv(
        "E2E_SECURED_DATASET_URL",
        "https://storage.googleapis.com/ddap-e2etest-objects/dataset/subjects-retricted-access"
    );

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        String testConfig = loadTemplate("/com/dnastack/ddap/adminConfig.json");
        setupRealmConfig(TestingPersona.ADMINISTRATOR, testConfig, "1", REALM);

        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AnyDdapPage::new);
    }

    @Test
    public void testSingleWorkflowExecutionWithTokens() throws IOException {
        WorkflowWesServersPage workflowWesServersPage = ddapPage.getNavBar()
                .goToWorkflows();
        WorkflowManagePage managePage = workflowWesServersPage.clickManage();

        managePage.fetchDatasetResult(datasetUrl);
        managePage.waitForInflightRequests();
        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.selectColumn("bam_file");
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        managePage.fillField(DdapBy.se("inp-workflow-wdl"), loadTemplate("/com/dnastack/ddap/workflow/with-tokens-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        managePage.fillFieldFromDropdown(By.name("md5Sum.inputFile"), "bam_file");
        managePage.clickButton(DdapBy.se("btn-next-to-wes-server"));
        managePage.fillFieldWithFirstValueFromDropdown(DdapBy.se("inp-workflow-wes-view"));
        managePage.waitForInflightRequests();
        managePage.clickButton(DdapBy.se("btn-next-to-auth"));

        URI authorizeUrl = managePage.requestAccess("btn-authorize");
        managePage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        WorkflowListPage workflowListPage = managePage.executeWorkflows(1);
        managePage.waitForInflightRequests();
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE));
    }

    @Test
    public void testMultipleWorkflowExecutionWithTokens() throws IOException {
        WorkflowWesServersPage workflowWesServersPage = ddapPage.getNavBar()
            .goToWorkflows();
        WorkflowManagePage managePage = workflowWesServersPage.clickManage();

        managePage.fetchDatasetResult(datasetUrl);
        managePage.waitForInflightRequests();
        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.clickCheckbox(DdapBy.se("checkbox-2"));
        managePage.clickCheckbox(DdapBy.se("checkbox-3"));
        managePage.selectColumn("bam_file");
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        managePage.fillField(DdapBy.se("inp-workflow-wdl"), loadTemplate("/com/dnastack/ddap/workflow/with-tokens-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        managePage.fillFieldFromDropdown(By.name("md5Sum.inputFile"), "bam_file");
        managePage.clickButton(DdapBy.se("btn-next-to-wes-server"));
        managePage.fillFieldWithFirstValueFromDropdown(DdapBy.se("inp-workflow-wes-view"));
        managePage.waitForInflightRequests();
        managePage.clickButton(DdapBy.se("btn-next-to-auth"));

        URI authorizeUrl = managePage.requestAccess("btn-authorize");
        managePage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        WorkflowListPage workflowListPage = managePage.executeWorkflows(3);
        managePage.waitForInflightRequests();
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE));
    }

    @Test
    public void testSingleWorkflowExecutionWithTokensFromSecuredDataset() throws IOException {
        WorkflowWesServersPage workflowWesServersPage = ddapPage.getNavBar()
            .goToWorkflows();
        WorkflowManagePage managePage = workflowWesServersPage.clickManage();

        // Try to fetch without access token
        managePage.tryFetchDatasetResult(securedDatasetUrl);
        managePage.waitForInflightRequests();

        URI authorizeDatasetUrl = managePage.requestAccess("btn-authorize-dataset");
        managePage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeDatasetUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        // Need to re-fetch with access token
        managePage.fetchDatasetResult(securedDatasetUrl);
        managePage.waitForInflightRequests();

        managePage.clickCheckbox(DdapBy.se("checkbox-0"));
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));
        managePage.fillField(DdapBy.se("inp-workflow-wdl"), loadTemplate("/com/dnastack/ddap/workflow/simple-workflow.wdl"));
        managePage.clickButton(DdapBy.se("btn-next-to-inputs"));
        managePage.waitForInflightRequests();
        managePage.fillFieldFromDropdown(By.name("test.name"), "blood_type");
        managePage.clickButton(DdapBy.se("btn-next-to-wes-server"));
        managePage.fillFieldWithFirstValueFromDropdown(DdapBy.se("inp-workflow-wes-view"));
        managePage.waitForInflightRequests();
        managePage.clickButton(DdapBy.se("btn-next-to-auth"));

        URI authorizeUrl = managePage.requestAccess("btn-authorize");
        managePage = loginStrategy.authorizeForResources(driver, USER_WITH_ACCESS, REALM, authorizeUrl, WorkflowManagePage::new);
        managePage.waitForInflightRequests();

        WorkflowListPage workflowListPage = managePage.executeWorkflows(1);
        managePage.waitForInflightRequests();
        workflowListPage.assertNewRunsInState(asList(QUEUED, RUNNING, COMPLETE));
    }

}
