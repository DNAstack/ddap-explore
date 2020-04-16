import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DataDetailComponent } from './data-detail/data-detail.component';
import { DataListComponent } from './data-list/data-list.component';
import { DataSearchComponent } from './data-search/data-search.component';

export const routes: Routes = [
  { path: 'collections', component: DataListComponent },
  { path: 'collections/search', component: DataSearchComponent },
  { path: 'collections/:collectionId', component: DataDetailComponent },
  { path: '', redirectTo: 'collections' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DataRoutingModule { }
