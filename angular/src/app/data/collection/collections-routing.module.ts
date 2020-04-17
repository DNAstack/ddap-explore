import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CollectionBeaconSearchComponent } from './collection-beacon-search/collection-beacon-search.component';
import { CollectionDetailComponent } from './collection-detail/collection-detail.component';
import { CollectionListComponent } from './collection-list/collection-list.component';

export const routes: Routes = [
  { path: '', component: CollectionListComponent },
  { path: 'search', component: CollectionBeaconSearchComponent, pathMatch: 'full' },
  { path: ':collectionId', component: CollectionDetailComponent },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
  ],
  exports: [RouterModule],
})
export class CollectionsRoutingModule {
}
