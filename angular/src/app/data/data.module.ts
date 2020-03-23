import { NgModule } from '@angular/core';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { DdapLayoutModule } from 'ddap-common-lib';
import { AceEditorModule } from 'ng2-ace-editor';

import { SharedModule } from '../shared/shared.module';

import { DataDetailComponent } from './data-detail/data-detail.component';
import { DataExplorerDetailComponent } from './data-explorer/data-explorer-detail/data-explorer-detail.component';
import { LinkifyPipe } from './data-explorer/data-explorer-detail/linkify.pipe';
import { ObjectToArrayPipe } from './data-explorer/data-explorer-detail/objecttoarray.pipe';
import { DataExplorerComponent } from './data-explorer/data-explorer.component';
import { DataListComponent } from './data-list/data-list.component';
import { DataRoutingModule } from './data-routing.module';
import { DataSearchComponent } from './data-search/data-search.component';

@NgModule({
  declarations: [
    DataExplorerComponent,
    DataExplorerDetailComponent,
    DataListComponent,
    DataDetailComponent,
    DataSearchComponent,
    ObjectToArrayPipe,
    LinkifyPipe,
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
