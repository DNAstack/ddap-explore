import { NgModule } from '@angular/core';

import { DataSharedModule } from '../shared/shared.module';

import { CollectionDetailComponent } from './collection-detail/collection-detail.component';
import { CollectionLogoComponent } from './collection-detail/collection-logo/collection-logo.component';
import {
  ResourceAccessCredentialsComponent
} from './collection-detail/resource-access-credentials/resource-access-credentials.component';
import { ResourceAccessFormComponent } from './collection-detail/resource-access-form/resource-access-form.component';
import { CollectionListComponent } from './collection-list/collection-list.component';
import { CollectionsRoutingModule } from './collections-routing.module';

@NgModule({
  declarations: [
    CollectionListComponent,
    CollectionDetailComponent,
    CollectionLogoComponent,
    ResourceAccessFormComponent,
    ResourceAccessCredentialsComponent,
  ],
  imports: [
    DataSharedModule,
    CollectionsRoutingModule,
  ],
})
export class CollectionsModule {
}
