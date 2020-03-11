import {Component, OnDestroy, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MatStepper} from '@angular/material/stepper';
import {ActivatedRoute, Router} from '@angular/router';
import {ErrorHandlerService, FormValidationService, ViewControllerService} from 'ddap-common-lib';
import _isequal from 'lodash.isequal';
import {Observable, Subscription, zip} from 'rxjs';
import {map} from 'rxjs/operators';

import {AccessControlService} from '../../shared/access-control.service';
import {AppConfigModel} from '../../shared/app-config/app-config.model';
import {AppConfigService} from '../../shared/app-config/app-config.service';
import {dam} from '../../shared/proto/dam-service';
import {ResourceAuthStateService} from '../../shared/resource-auth-state.service';
import {ResourceService} from '../../shared/resource/resource.service';
import {TrsService} from '../trs-v2/trs.service';
import {ResourceAuthorizationStepComponent} from '../workflow-execution-form/resource-authorization-step/resource-authorization-step.component';
import {WdlSelectionStepComponent} from '../workflow-execution-form/wdl-selection-step/wdl-selection-step.component';
import {WesServerSelectionStepComponent} from '../workflow-execution-form/wes-server-selection-step/wes-server-selection-step.component';
import {WorkflowExecutionStepComponent} from '../workflow-execution-form/workflow-execution-step/workflow-execution-step.component';
import {WorkflowExecution} from '../workflow-execution-form/workflow-execution-step/workflow-execution.model';
import {WorkflowFormBuilder} from '../workflow-execution-form/workflow-form-builder.service';
import {WorkflowsStateService} from '../workflow-execution-form/workflows-state.service';
import {WorkflowService} from '../workflows.service';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;

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
  subscriptions: Subscription[] = [];
  resourceAccesses: {[key: string]: IResourceAccess};
  workflowId = Math.random().toString(36).substring(7);
  readyToExecute = false;

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
              private appConfigService: AppConfigService,
              private accessControl: AccessControlService,
              private router: Router,
              private validationService: FormValidationService,
              private workflowService: WorkflowService,
              private resourceService: ResourceService,
              private trsService: TrsService,
              private workflowFormBuilder: WorkflowFormBuilder,
              private workflowsStateService: WorkflowsStateService,
              private resourceAuthStateService: ResourceAuthStateService,
              private errorHandler: ErrorHandlerService,
              private viewController: ViewControllerService
              ) {
  }

  ngOnInit() {
    this.initialize();

    // Ensure that the user can only access this component when it is enabled.
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      if (data.featureWorkflowsEnabled) {
        // NOOP
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  ngOnDestroy(): void {
    this.subscriptions
      .forEach((subscription) => subscription.unsubscribe());
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

  getAccessTokensForAuthorizedResources(workflowId: string): Observable<{[key: string]: IResourceAccess}> {
    const { columnDataMappedToViews, datasetDamIdResourcePathPairs } = this.workflowsStateService.getMetaInfoForWorkflow(workflowId);
    const damIdWesResourcePathPair = this.workflowForm.get('wesViewResourcePath').value;
    const inputsStepCompleted = this.workflowForm.get('inputs').value;
    const damIdResourcePathPairs: string[] = [];
    if (columnDataMappedToViews) {
      const pairs: string[] = Object.values(columnDataMappedToViews).reduce((l, r) => l.concat(r));
      damIdResourcePathPairs.push(...pairs);
    }
    if (damIdWesResourcePathPair && inputsStepCompleted) {
      damIdResourcePathPairs.push(damIdWesResourcePathPair);
    }
    const formResourceTokens$ = this.resourceService.getAccessTokensForAuthorizedResources(damIdResourcePathPairs);

    if (datasetDamIdResourcePathPairs) {
      // Needs to be done separately, because secured dataset view was checkout separately, hence the separate cart
      const datasetResourceTokens$ = this.resourceService.getAccessTokensForAuthorizedResources(datasetDamIdResourcePathPairs);
      if (damIdResourcePathPairs.length > 0) {
        return zip(formResourceTokens$, datasetResourceTokens$, (resourceTokens1, resourceTokens2) => {
          return {
            ...this.resourceService.toResourceAccessMap(resourceTokens1),
            ...this.resourceService.toResourceAccessMap(resourceTokens2),
          };
        });
      } else {
        return datasetResourceTokens$.pipe(
          map(this.resourceService.toResourceAccessMap)
        );
      }
    } else {
      return formResourceTokens$.pipe(
        map(this.resourceService.toResourceAccessMap)
      );
    }
  }

  executeWorkflows(): void {
    const damId = this.wesStep.getDamId();
    const wesViewId = this.workflowForm.get('wesView').value;
    const wesResourcePath = this.workflowForm.get('wesViewResourcePath').value.split(';')[1];
    const wesAccessToken = this.resourceService
      .lookupResourceTokenFromAccessMap(this.resourceAccesses, wesResourcePath)
      .credentials['access_token'];
    const executions: WorkflowExecution[] = this.executionStep.getWorkflowExecutionModels();

    // When no rows have been selected, the execution list from the execution step will be empty. In this case, we will
    // generate the execution based on the workflow form data instead.
    if (executions.length === 0) {
      executions.push(this.executionStep.createWorkflowExecutionModel(this.workflowForm.getRawValue().inputs));
    }

    zip(...executions.map((execution) =>
        this.workflowService.runWorkflow(damId, wesViewId, execution, wesAccessToken)
      ))
      .subscribe((runs: object[]) => this.navigateUp('operations', runs, damId, wesViewId))
    ;
  }

  toggleLeftSideNav() {
    this.viewController.toggleLeftSidenav();
  }

  protected getSourceUrl(): string {
    const { sourceUrl } = this.route.snapshot.params;

    if (!sourceUrl) {
      return null;
    }

    return atob(sourceUrl);
  }

  protected navigateUp = (path: string, runs: object[], damId, wesView) => {
    const actualPath = (this.getSourceUrl() ? '../..' : '..') + '/' + path;
    const { viewId } = this.route.snapshot.params;
    const navigatePath = viewId ? [actualPath] : [actualPath, damId, 'views', wesView, 'runs'];
    this.router.navigate(navigatePath, { relativeTo: this.route, state: { runs } });
  }

  private initialize() {
    this.datasetForm = this.workflowFormBuilder.buildDatasetForm();
    this.workflowForm = this.workflowFormBuilder.buildWorkflowForm();
    this.subscribeToFormChanges();

    this.subscriptions.push(this.route.queryParams
      .subscribe(params => {
        if (!params.state) {
          this.loadPredefinedWorkflowDescription();
          return;
        }
        this.workflowId = params.state;
        this.loadFromStateAndCheckoutAuthorizedResources();
      }));
  }

  private loadFromStateAndCheckoutAuthorizedResources() {
    const { datasetForm, workflowForm } = this.workflowsStateService.getWorkflowForm(this.workflowId);
    this.datasetForm = this.workflowFormBuilder.buildDatasetForm(datasetForm);
    this.workflowForm = this.workflowFormBuilder.buildWorkflowForm(workflowForm);

    this.subscribeToFormChanges();

    this.getAccessTokensForAuthorizedResources(this.workflowId)
      .subscribe((access) => {
        this.resourceAccesses = access;
        this.resourceAuthStateService.storeAccess(access);
        const { wesView, wesViewResourcePath, wdl, inputs } = this.workflowForm.value;
        if (wesView && wesViewResourcePath && wdl && inputs) {
          this.readyToExecute = true;
          this.moveToExecutionStep();
        }
      });
  }

  private loadPredefinedWorkflowDescription() {
    const sourceUrl = this.getSourceUrl();

    if (sourceUrl) {
      this.workflowForm.get('wdl').patchValue('# Loading...');
      this.trsService.getDescriptorFrom(sourceUrl)
        .subscribe(script => this.workflowForm.get('wdl').patchValue(script));
    }
  }

  private subscribeToFormChanges() {
    this.subscriptions.push(this.datasetForm.valueChanges
      .subscribe(() => {
        this.saveState();
      }));
    this.subscriptions.push(this.workflowForm.valueChanges
      .subscribe(() => {
        this.saveState();
      }));
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
