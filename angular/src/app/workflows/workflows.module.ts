import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatStepperModule } from '@angular/material/stepper';
import { MonacoEditorModule } from '@materia-ui/ngx-monaco-editor';
import { NgJsonEditorModule } from 'ang-jsoneditor';
import { MaterialDesignFrameworkModule } from 'angular7-json-schema-form';

import { SharedModule } from '../shared/shared.module';

import { RegisteredWorkflowListComponent } from './registered-workflow-list/registered-workflow-list.component';
import { WorkflowDetailComponent } from './workflow-detail/workflow-detail.component';
import { DatasetResultsComponent } from './workflow-execution-form/dataset-selection-step/dataset-results/dataset-results.component';
import { DatasetSelectionStepComponent } from './workflow-execution-form/dataset-selection-step/dataset-selection-step.component';
import { InputsSelectionStepComponent } from './workflow-execution-form/inputs-selection-step/inputs-selection-step.component';
import { AutocompleteInputComponent } from './workflow-execution-form/inputs-selection-step/widget/autocomplete-input.component';
import {
  ResourceAuthorizationStepComponent
} from './workflow-execution-form/resource-authorization-step/resource-authorization-step.component';
import { WdlSelectionStepComponent } from './workflow-execution-form/wdl-selection-step/wdl-selection-step.component';
import { WesServerSelectionStepComponent } from './workflow-execution-form/wes-server-selection-step/wes-server-selection-step.component';
import { WorkflowExecutionStepComponent } from './workflow-execution-form/workflow-execution-step/workflow-execution-step.component';
import { WorkflowListMultiComponent } from './workflow-list-multi/workflow-list-multi.component';
import { WorkflowListSingleComponent } from './workflow-list-single/workflow-list-single.component';
import { WorkflowManageComponent } from './workflow-manage/workflow-manage.component';
import { WorkflowsRoutingModule } from './workflows-routing.module';

@NgModule({
  declarations: [
    WorkflowListMultiComponent,
    WorkflowListSingleComponent,
    WorkflowDetailComponent,
    WorkflowManageComponent,
    DatasetResultsComponent,
    WorkflowDetailComponent,
    AutocompleteInputComponent,
    DatasetSelectionStepComponent,
    WdlSelectionStepComponent,
    InputsSelectionStepComponent,
    WesServerSelectionStepComponent,
    ResourceAuthorizationStepComponent,
    WorkflowExecutionStepComponent,
    RegisteredWorkflowListComponent,
  ],
    imports: [
      SharedModule,
      NgJsonEditorModule,
      FlexLayoutModule,
      MaterialDesignFrameworkModule,
      WorkflowsRoutingModule,
      MatStepperModule,
      MonacoEditorModule,
    ],
})
export class WorkflowsModule { }
