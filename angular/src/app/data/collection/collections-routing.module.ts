import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CollectionDetailComponent } from './collection-detail/collection-detail.component';
import { CollectionListComponent } from './collection-list/collection-list.component';

export const routes: Routes = [
  { path: '', component: CollectionListComponent },
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
