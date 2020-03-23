import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SearchResourcesComponent } from './search-resources/search-resources.component';
import { SearchTablesComponent } from './search-tables/search-tables.component';

export const routes: Routes = [
  { path: 'resources', component: SearchResourcesComponent},
  { path: 'resource/:resourceName', component: SearchTablesComponent},
  { path: 'tables', component: SearchTablesComponent},
  { path: '', redirectTo: 'resources'},
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SearchRoutingModule {}
