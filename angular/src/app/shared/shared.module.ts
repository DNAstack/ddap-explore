import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatOptionModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { AgGridModule } from 'ag-grid-angular';
import { DdapFormModule, DdapLayoutModule, MenuModule, ViewControllerService } from 'ddap-common-lib';
import { TagInputModule } from 'ngx-chips';
import { ClipboardModule } from 'ngx-clipboard';

import { AccessDeniedScreenComponent } from './access-denied-screen/access-denied-screen.component';
import { BeaconResultComponent } from './beacon-search/beacon-result/beacon-result.component';
import { BeaconSearchBarComponent } from './beacon-search/beacon-search-bar/beacon-search-bar.component';
import { LimitSearchComponent } from './beacon-search/beacon-search-bar/limit-search/limit-search.component';
import { DataTableComponent } from './data-table/data-table.component';
import { LayoutComponent } from './layout/layout.component';
import { MarkdownPipe } from './markdown.pipe';
import { MetadataFilterPipe } from './metadata-list/metadata-filter.pipe';
import { MetadataListComponent } from './metadata-list/metadata-list.component';
import { PaginatorComponent } from './paginator/paginator.component';
import { ResourceLogoComponent } from './resource/resource-logo/resource-logo.component';
import { ResourceViewItemComponent } from './resource/resource-view-item/resource-view-item.component';
import { ViewAccessComponent } from './view-access/view-access.component';

@NgModule({
  declarations: [
    LayoutComponent,
    ResourceLogoComponent,
    ResourceViewItemComponent,
    ViewAccessComponent,
    BeaconSearchBarComponent,
    BeaconResultComponent,
    LimitSearchComponent,
    MetadataListComponent,
    MetadataFilterPipe,
    PaginatorComponent,
    MarkdownPipe,
    AccessDeniedScreenComponent,
    DataTableComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ClipboardModule,
    ReactiveFormsModule,
    TagInputModule,

    MatAutocompleteModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDialogModule,
    MatProgressBarModule,
    MatSidenavModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatOptionModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
    MatSlideToggleModule,
    MatTabsModule,

    DdapLayoutModule,
    DdapFormModule,

    MenuModule,

    AgGridModule.withComponents([]),
  ],
  exports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ClipboardModule,
    ReactiveFormsModule,
    TagInputModule,

    MatAutocompleteModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDialogModule,
    MatProgressBarModule,
    MatSidenavModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatOptionModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
    MatSlideToggleModule,
    MatTabsModule,

    DdapLayoutModule,
    DdapFormModule,

    BeaconSearchBarComponent,
    BeaconResultComponent,
    ResourceLogoComponent,
    ResourceViewItemComponent,
    ViewAccessComponent,
    LimitSearchComponent,
    MetadataListComponent,
    MetadataFilterPipe,
    PaginatorComponent,
    MarkdownPipe,
    MenuModule,
    AccessDeniedScreenComponent,
    DataTableComponent,
  ],
  providers: [
    ViewControllerService,
  ],
})
export class SharedModule {
}
