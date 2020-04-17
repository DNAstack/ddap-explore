import { NgModule } from '@angular/core';

import { DataSharedModule } from '../shared/shared.module';

import { CollectionBeaconSearchComponent } from './collection-beacon-search/collection-beacon-search.component';
import { CollectionDetailComponent } from './collection-detail/collection-detail.component';
import { CollectionListComponent } from './collection-list/collection-list.component';
import { CollectionsRoutingModule } from './collections-routing.module';

@NgModule({
  declarations: [
    CollectionListComponent,
    CollectionDetailComponent,
    CollectionBeaconSearchComponent,
  ],
  imports: [
    DataSharedModule,
    CollectionsRoutingModule,
  ],
})
export class CollectionsModule {
}
