package com.dnastack.ddap.frontend;

import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.WorkflowManagePage;
import com.dnastack.ddap.common.page.WorkflowRegistryPage;
import com.dnastack.ddap.common.util.DdapBy;
import com.dnastack.ddap.common.util.EnvUtil;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.dnastack.ddap.common.TestingPersona.USER_WITH_ACCESS;
import static org.junit.Assert.assertTrue;

public class WorkflowRegistryE2eTest extends AbstractFrontendE2eTest {
    private static WorkflowE2eTest.WorkflowTestConfig workflowTestConfig;

    @BeforeClass
    public static void oneTimeSetup() {
        workflowTestConfig = EnvUtil.optionalEnvConfig("E2E_TEST_WORKFLOW_CONFIG", new WorkflowE2eTest.WorkflowTestConfig(), WorkflowE2eTest.WorkflowTestConfig.class);
        Assume.assumeTrue("WorkflowRegistryE2eTest has been disabled, and will not run.", workflowTestConfig.isEnabled());
        ddapPage = doBrowserLogin(REALM, USER_WITH_ACCESS, AnyDdapPage::new);
    }

    @Test
    public void testSelectPublicWDL() {
        String targetWorkflowName = "mssng-db6-joint-genotype-by-region";
        ddapPage.getNavBar().goToApp("product-app-menu-analytics");

        WorkflowRegistryPage registryPage = ddapPage.getNavBar().goToWorkflowRegistry();

        registryPage.waitForInflightRequests();

        registryPage.filterList("mssng");
        registryPage.selectWorkflowByName(targetWorkflowName);
        registryPage.selectWorkflowTab(targetWorkflowName, 2);
        registryPage.selectDescriptor(targetWorkflowName, "1.0.0", "WDL");
        registryPage.shouldSeeCodeEditorFor("1.0.0", "WDL");

        WorkflowManagePage managePage = registryPage.transferToWorkflowManagePage();
        managePage.clickButton(DdapBy.se("btn-next-to-wdl"));

        assertTrue(managePage.getNumberOfLinesInCodeEditor() > 10);
    }
}
