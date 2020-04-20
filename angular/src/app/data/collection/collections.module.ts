import { NgModule } from '@angular/core';

import { DataSharedModule } from '../shared/shared.module';

import { BeaconLimitSearchComponent } from './collection-beacon-search/beacon-limit-search/beacon-limit-search.component';
import { BeaconResultComponent } from './collection-beacon-search/beacon-result/beacon-result.component';
import { BeaconSearchBarComponent } from './collection-beacon-search/beacon-search-bar/beacon-search-bar.component';
import { CollectionBeaconSearchComponent } from './collection-beacon-search/collection-beacon-search.component';
import { CollectionDetailComponent } from './collection-detail/collection-detail.component';
import { CollectionLogoComponent } from './collection-detail/collection-logo/collection-logo.component';
import {
  CollectionResourceCredentialsComponent
} from './collection-detail/collection-resource-credentials/collection-resource-credentials.component';
import { CollectionResourceComponent } from './collection-detail/collection-resource/collection-resource.component';
import { CollectionListComponent } from './collection-list/collection-list.component';
import { CollectionsRoutingModule } from './collections-routing.module';

@NgModule({
  declarations: [
    CollectionListComponent,
    CollectionDetailComponent,
    CollectionBeaconSearchComponent,
    CollectionLogoComponent,
    CollectionResourceComponent,
    CollectionResourceCredentialsComponent,
    BeaconSearchBarComponent,
    BeaconResultComponent,
    BeaconLimitSearchComponent,
  ],
  imports: [
    DataSharedModule,
    CollectionsRoutingModule,
  ],
})
export class CollectionsModule {
}
