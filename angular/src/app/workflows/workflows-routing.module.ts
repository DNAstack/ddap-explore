import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RegisteredWorkflowListComponent } from './registered-workflow-list/registered-workflow-list.component';
import { WorkflowDetailComponent } from './workflow-detail/workflow-detail.component';
import { WorkflowListMultiComponent } from './workflow-list-multi/workflow-list-multi.component';
import { WorkflowListSingleComponent } from './workflow-list-single/workflow-list-single.component';
import { WorkflowManageComponent } from './workflow-manage/workflow-manage.component';

export const routes: Routes = [
  { path: '', redirectTo: 'operations' },
  { path: 'operations/:damId/views/:viewId/runs', component: WorkflowListSingleComponent},
  { path: 'operations/:damId/views/:viewId/runs/:runId', component: WorkflowDetailComponent},
  { path: 'operations/:damId/views/:viewId/runs/manage/add', component: WorkflowManageComponent},
  { path: 'run', pathMatch: 'full', component: WorkflowManageComponent },
  { path: 'run/:sourceUrl', component: WorkflowManageComponent },
  { path: 'workflows', component: RegisteredWorkflowListComponent },
  { path: 'operations', component: WorkflowListMultiComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WorkflowsRoutingModule { }
