import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { DdapLayoutModule } from 'ddap-common-lib';

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
  ],
})
export class SearchModule {}
