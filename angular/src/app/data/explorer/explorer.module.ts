import { NgModule } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AceEditorModule } from 'ng2-ace-editor';

import { DataSharedModule } from '../shared/shared.module';

import { DataExplorerDetailComponent } from './data-explorer/data-explorer-detail/data-explorer-detail.component';
import { LinkifyPipe } from './data-explorer/data-explorer-detail/linkify.pipe';
import { ObjectToArrayPipe } from './data-explorer/data-explorer-detail/objecttoarray.pipe';
import { DataExplorerComponent } from './data-explorer/data-explorer.component';
import { ExplorerRoutingModule } from './explorer-routing.module';


@NgModule({
  declarations: [
    DataExplorerComponent,
    DataExplorerDetailComponent,
    ObjectToArrayPipe,
    LinkifyPipe,
  ],
  imports: [
    DataSharedModule,
    ExplorerRoutingModule,
    MatToolbarModule,
    AceEditorModule,
  ],
})
export class ExplorerModule {
}
