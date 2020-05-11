import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { WorkspaceListComponent } from './workspace-list/workspace-list.component';

export const routes: Routes = [
  { path: '', redirectTo: 'all' },
  // { path: ':collectionId/:submoduleId', component: WorkspaceComponent},
  // { path: ':collectionId', component: WorkspaceComponent},
  { path: 'all', component: WorkspaceListComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WorkspaceRoutingModule {
}
