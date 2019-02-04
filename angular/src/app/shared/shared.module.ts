import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  MatButtonModule,
  MatCardModule,
  MatExpansionModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatProgressBarModule,
  MatSidenavModule
} from '@angular/material';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgJsonEditorModule } from 'ang-jsoneditor';
import { ClipboardModule } from 'ngx-clipboard';

import { AppRoutingModule } from '../app-routing.module';

import { BeaconResultComponent } from './beaconResult/beaconResult.component';
import { EntityListComponent } from './entityList/entityList.component';
import { EntityListHeaderComponent } from './entityListHeader/entityListHeader.component';
import { JsonPanelComponent } from './jsonPanel/jsonPanel.component';
import { NavBackComponent } from './navBack/navBack.component';
import { ResourceLogoComponent } from './resourceLogo/resource-logo.component';
import { SearchBarComponent } from './searchBar/searchBar.component';

@NgModule({
  declarations: [
    JsonPanelComponent,
    EntityListComponent,
    EntityListHeaderComponent,
    NavBackComponent,
    ResourceLogoComponent,
    SearchBarComponent,
    BeaconResultComponent,
  ],
  imports: [
    AppRoutingModule,

    ClipboardModule,
    CommonModule,
    FormsModule,
    BrowserAnimationsModule,

    MatCardModule,
    MatProgressBarModule,
    MatSidenavModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,

    NgJsonEditorModule,
  ],
  exports: [
    ClipboardModule,
    CommonModule,
    FormsModule,
    BrowserAnimationsModule,

    MatCardModule,
    MatProgressBarModule,
    MatSidenavModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,

    NgJsonEditorModule,
    NavBackComponent,
    SearchBarComponent,
    BeaconResultComponent,
    JsonPanelComponent,
    EntityListComponent,
    EntityListHeaderComponent,
    ResourceLogoComponent,
  ],
})
export class SharedModule { }
