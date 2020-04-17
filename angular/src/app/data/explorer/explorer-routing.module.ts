import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DataExplorerDetailComponent } from './data-explorer/data-explorer-detail/data-explorer-detail.component';
import { DataExplorerComponent } from './data-explorer/data-explorer.component';

export const routes: Routes = [
  { path: '', component: DataExplorerComponent },
  { path: ':id', component: DataExplorerDetailComponent },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
  ],
  exports: [RouterModule],
})
export class ExplorerRoutingModule {
}
