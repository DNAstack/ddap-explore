import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SearchTablesComponent } from './search-tables/search-tables.component';

export const routes: Routes = [
  { path: 'tables', component: SearchTablesComponent},
  { path: '', redirectTo: 'tables'},
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SearchRoutingModule {}
