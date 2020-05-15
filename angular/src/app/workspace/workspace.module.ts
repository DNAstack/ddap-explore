import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatRippleModule } from '@angular/material/core';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MaterialDesignFrameworkModule } from 'angular7-json-schema-form';

import { DiscoveryModule } from '../discovery/discovery.module';
import { SharedModule } from '../shared/shared.module';

import { SimpleSearchComponent } from './simple-search/simple-search.component';
import { WorkspaceListComponent } from './workspace-list/workspace-list.component';
import { WorkspaceRoutingModule } from './workspace-routing.module';
import { WorkspaceComponent } from './workspace/workspace.component';

@NgModule({
  declarations: [
    SimpleSearchComponent,
    WorkspaceComponent,
    WorkspaceListComponent,
  ],
  imports: [
    // First/third-party modules
    FlexLayoutModule,
    MaterialDesignFrameworkModule,
    MatRippleModule,
    MatToolbarModule,
    // Common modules
    SharedModule,
    // Internal modules
    WorkspaceRoutingModule,
    // Feature modules
    DiscoveryModule,
  ],
})
export class WorkspaceModule {}
