import { Component, OnDestroy, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormGroup } from '@angular/forms';
import ResourceTokens = dam.v1.ResourceTokens;
import { MatStepper } from '@angular/material/stepper';
import { ActivatedRoute, Router } from '@angular/router';
import { FormValidationService } from 'ddap-common-lib';
import _isequal from 'lodash.isequal';
import { Subscription, zip } from 'rxjs';

import { dam } from '../../shared/proto/dam-service';
import { ResourceAuthStateService } from '../../shared/resource-auth-state.service';
import { ResourceService } from '../../shared/resource/resource.service';
import {
  ResourceAuthorizationStepComponent
} from '../workflow-execution-form/resource-authorization-step/resource-authorization-step.component';
import { WdlSelectionStepComponent } from '../workflow-execution-form/wdl-selection-step/wdl-selection-step.component';
import IResourceTokens = dam.v1.IResourceTokens;
import { WesServerSelectionStepComponent } from '../workflow-execution-form/wes-server-selection-step/wes-server-selection-step.component';
import {
  WorkflowExecutionStepComponent
} from '../workflow-execution-form/workflow-execution-step/workflow-execution-step.component';
import { WorkflowExecution } from '../workflow-execution-form/workflow-execution-step/workflow-execution.model';
import { WorkflowFormBuilder } from '../workflow-execution-form/workflow-form-builder.service';
import { WorkflowsStateService } from '../workflow-execution-form/workflows-state.service';
import { WorkflowService } from '../workflows.service';

@Component({
  selector: 'ddap-workflow-manage',
  templateUrl: './workflow-manage.component.html',
  styleUrls: ['./workflow-manage.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class WorkflowManageComponent implements OnInit, OnDestroy {

  get wdlForm() {
    return this.workflowForm.get('wdl') as FormGroup;
  }

  get inputsForm() {
    return this.workflowForm.get('inputs') as FormGroup;
  }

  get wesForm() {
    return this.workflowForm.get('wesView') as FormGroup;
  }

  get selectedColumns(): string[] {
    return this.datasetForm.get('selectedColumns').value;
  }

  get selectedRows(): object[] {
    return this.datasetForm.get('selectedRows').value;
  }

  datasetForm: FormGroup;
  workflowForm: FormGroup;
  datasetColumns: string[];
  paramsSubscription: Subscription;
  resourceTokens: IResourceTokens;
  workflowId = Math.random().toString(36).substring(7);

  @ViewChild('stepper', { static: false })
  stepper: MatStepper;
  @ViewChild(WdlSelectionStepComponent, { static: false })
  wdlStep: WdlSelectionStepComponent;
  @ViewChild(WesServerSelectionStepComponent, { static: false })
  wesStep: WesServerSelectionStepComponent;
  @ViewChild(ResourceAuthorizationStepComponent, { static: false })
  resourceAuthorizationStep: ResourceAuthorizationStepComponent;
  @ViewChild(WorkflowExecutionStepComponent, { static: false })
  executionStep: WorkflowExecutionStepComponent;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private validationService: FormValidationService,
              private workflowService: WorkflowService,
              private resourceService: ResourceService,
              private workflowFormBuilder: WorkflowFormBuilder,
              private workflowsStateService: WorkflowsStateService,
              private resourceAuthStateService: ResourceAuthStateService) {
  }

  ngOnInit() {
    this.datasetForm = this.workflowFormBuilder.buildDatasetForm();
    this.datasetForm.valueChanges
      .subscribe(() => this.saveState());
    this.workflowForm = this.workflowFormBuilder.buildWorkflowForm();
    this.workflowForm.valueChanges
      .subscribe(() => this.saveState());

    this.paramsSubscription = this.route.queryParams
      .subscribe(params => {
        if (!params.state) {
          return;
        }
        this.workflowId = params.state;
        this.loadFromStateAndCheckoutAuthorizedResources();
      });
  }

  ngOnDestroy(): void {
    this.paramsSubscription.unsubscribe();
    this.workflowsStateService.removeWorkflowData(this.workflowId);
  }

  resetStepper() {
    this.stepper.reset();
    this.workflowsStateService.removeWorkflowData(this.workflowId);
  }

  datasetColumnsChange(columns) {
    if (!_isequal(this.datasetColumns, columns)) {
      this.datasetColumns = columns;
      if (this.workflowForm.get('wdl').value) {
        this.wdlStep.generateForm();
      }
    }
  }

  getAccessTokensForAuthorizedResources(workflowId: string) {
    const { columnDataMappedToViews } = this.workflowsStateService.getMetaInfoForWorkflow(workflowId);
    const damIdResourcePathPairs: string[] = Object.values(columnDataMappedToViews);
    const damIdWesResourcePathPair = this.workflowForm.get('wesViewResourcePath').value;
    damIdResourcePathPairs.push(damIdWesResourcePathPair);
    return this.resourceService.getAccessTokensForAuthorizedResources(damIdResourcePathPairs);
  }

  executeWorkflows(): void {
    const damId = this.wesStep.getDamId();
    const wesViewId = this.workflowForm.get('wesView').value;
    const wesResourcePath = this.workflowForm.get('wesViewResourcePath').value.split(';')[1];
    const wesAccessToken = this.resourceService.lookupResourceToken(this.resourceTokens, wesResourcePath)['access_token'];
    const executions: WorkflowExecution[] = this.executionStep.getWorkflowExecutionModels();

    zip(...executions.map((execution) =>
        this.workflowService.runWorkflow(damId, wesViewId, execution, wesAccessToken)
      )).subscribe((runs: object[]) => this.navigateUp('../..', runs, damId, wesViewId));
  }

  protected navigateUp = (path: string, runs: object[], damId, wesView) => {
    const { viewId } = this.route.snapshot.params;
    const navigatePath = viewId ? [path] : [path, damId, 'views', wesView, 'runs'];
    this.router.navigate(navigatePath, { relativeTo: this.route, state: { runs } });
  }

  private loadFromStateAndCheckoutAuthorizedResources() {
    const { datasetForm, workflowForm } = this.workflowsStateService.getWorkflowForm(this.workflowId);
    this.datasetForm = this.workflowFormBuilder.buildDatasetForm(datasetForm);
    this.workflowForm = this.workflowFormBuilder.buildWorkflowForm(workflowForm);

    this.getAccessTokensForAuthorizedResources(this.workflowId)
      .subscribe((access) => {
        this.resourceTokens = access;
        // this.resourceAuthStateService.storeAccess(this.resourceTokens);
        this.moveToExecutionStep();
      });
  }

  private saveState() {
    this.workflowsStateService.storeWorkflowForm(this.workflowId, {
      datasetForm: this.datasetForm.value,
      workflowForm: this.workflowForm.value,
    });
  }

  private moveToExecutionStep() {
    // https://stackoverflow.com/a/56201015
    this.stepper.linear = false;
    this.stepper.selectedIndex = 5;
    this.stepper.linear = true;
  }

}
