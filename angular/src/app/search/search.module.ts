import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { _MatMenuDirectivesModule, MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DdapLayoutModule } from 'ddap-common-lib';
import { AceEditorModule } from 'ng2-ace-editor';

import { SearchRoutingModule } from './search-routing.module';
import { LinkifyPipe } from './search-tables/linkify.pipe';
import { ObjectToArrayPipe } from './search-tables/objecttoarray.pipe';
import { SearchTablesComponent } from './search-tables/search-tables.component';

@NgModule({
  declarations: [
    SearchTablesComponent,
    ObjectToArrayPipe,
    LinkifyPipe,
  ],
  imports: [
    SearchRoutingModule,
    DdapLayoutModule,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatExpansionModule,
    CommonModule,
    AceEditorModule,
    MatTooltipModule,
    MatButtonModule,
    MatChipsModule,
    _MatMenuDirectivesModule,
    MatMenuModule,
    MatProgressBarModule,
  ],
})
export class SearchModule {}
