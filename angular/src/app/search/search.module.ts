import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DdapLayoutModule } from 'ddap-common-lib';
import { AceEditorModule } from 'ng2-ace-editor';

import { SearchRoutingModule } from './search-routing.module';
import { SearchTablesComponent } from './search-tables/search-tables.component';

@NgModule({
  declarations: [SearchTablesComponent],
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
  ],
})
export class SearchModule {}
