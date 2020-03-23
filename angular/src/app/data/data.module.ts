import { NgModule } from '@angular/core';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { DdapLayoutModule } from 'ddap-common-lib';
import { AceEditorModule } from 'ng2-ace-editor';

import { SharedModule } from '../shared/shared.module';

import { DataDetailComponent } from './data-detail/data-detail.component';
import { DataListComponent } from './data-list/data-list.component';
import { DataRoutingModule } from './data-routing.module';
import { DataSearchComponent } from './data-search/data-search.component';

@NgModule({
  declarations: [
    DataListComponent,
    DataDetailComponent,
    DataSearchComponent,
  ],
  imports: [
    SharedModule,
    DataRoutingModule,
    MatGridListModule,
    MatToolbarModule,
    AceEditorModule,
    DdapLayoutModule,
  ],
})
export class DataModule {
}
