import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DataDetailComponent } from './data-detail/data-detail.component';
import { DataExplorerDetailComponent } from './data-explorer/data-explorer-detail/data-explorer-detail.component';
import { DataExplorerComponent } from './data-explorer/data-explorer.component';
import { DataListComponent } from './data-list/data-list.component';
import { DataSearchComponent } from './data-search/data-search.component';

export const routes: Routes = [
  { path: '', redirectTo: 'explorer' },
  { path: 'explorer', component: DataExplorerComponent },
  { path: 'explorer/:id', component: DataExplorerDetailComponent },
  { path: 'collections', component: DataListComponent },
  { path: 'collections/search', component: DataSearchComponent },
  { path: 'collections/:damId/:resourceName', component: DataDetailComponent },
  { path: '', redirectTo: 'collections' },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
  ],
  exports: [RouterModule],
})
export class DataRoutingModule { }
