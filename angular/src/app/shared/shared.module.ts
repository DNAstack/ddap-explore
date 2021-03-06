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
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { AgGridModule } from 'ag-grid-angular';
import { DdapFormModule, DdapLayoutModule, MenuModule, ViewControllerService } from 'ddap-common-lib';
import { TagInputModule } from 'ngx-chips';
import { ClipboardModule } from 'ngx-clipboard';

import { AccessDeniedScreenComponent } from './access-denied-screen/access-denied-screen.component';
import { CellRendererComponent } from './data-table/cell-renderer/cell-renderer.component';
import { DataTableComponent } from './data-table/data-table.component';
import { DotLoadingIndicatorComponent } from './dot-loading-indicator/dot-loading-indicator.component';
import { LayoutComponent } from './layout/layout.component';
import { MarkdownPipe } from './markdown.pipe';
import { MetadataFilterPipe } from './metadata-list/metadata-filter.pipe';
import { MetadataListComponent } from './metadata-list/metadata-list.component';
import { PaginatorComponent } from './paginator/paginator.component';

@NgModule({
  declarations: [
    LayoutComponent,
    MetadataListComponent,
    MetadataFilterPipe,
    PaginatorComponent,
    MarkdownPipe,
    AccessDeniedScreenComponent,
    DataTableComponent,
    CellRendererComponent,
    DotLoadingIndicatorComponent,
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
    MatToolbarModule,
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

    MetadataListComponent,
    MetadataFilterPipe,
    PaginatorComponent,
    MarkdownPipe,
    MenuModule,
    AccessDeniedScreenComponent,
    DataTableComponent,
    DotLoadingIndicatorComponent,
  ],
  providers: [
    ViewControllerService,
  ],
  entryComponents: [
    CellRendererComponent,
  ],
})
export class SharedModule {
}
