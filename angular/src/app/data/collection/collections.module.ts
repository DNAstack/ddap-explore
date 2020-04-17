import { NgModule } from '@angular/core';

import { DataSharedModule } from '../shared/shared.module';

import { BeaconResultComponent } from './beacon-search/beacon-result/beacon-result.component';
import { BeaconSearchBarComponent } from './beacon-search/beacon-search-bar/beacon-search-bar.component';
import { LimitSearchComponent } from './beacon-search/beacon-search-bar/limit-search/limit-search.component';
import { CollectionBeaconSearchComponent } from './collection-beacon-search/collection-beacon-search.component';
import { CollectionDetailComponent } from './collection-detail/collection-detail.component';
import { CollectionListComponent } from './collection-list/collection-list.component';
import { CollectionsRoutingModule } from './collections-routing.module';

@NgModule({
  declarations: [
    CollectionListComponent,
    CollectionDetailComponent,
    CollectionBeaconSearchComponent,
    BeaconSearchBarComponent,
    BeaconResultComponent,
    LimitSearchComponent,
  ],
  imports: [
    DataSharedModule,
    CollectionsRoutingModule,
  ],
})
export class CollectionsModule {
}
