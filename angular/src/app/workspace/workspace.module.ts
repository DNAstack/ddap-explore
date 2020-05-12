import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatGridListModule } from '@angular/material/grid-list';
import { MaterialDesignFrameworkModule } from 'angular7-json-schema-form';

import { SharedModule } from '../shared/shared.module';

import { WorkspaceListComponent } from './workspace-list/workspace-list.component';
import { WorkspaceRoutingModule } from './workspace-routing.module';
import { WorkspaceComponent } from './workspace/workspace.component';

@NgModule({
  declarations: [
    WorkspaceComponent,
    WorkspaceListComponent,
  ],
  imports: [
    // First/third-party modules
    FlexLayoutModule,
    MaterialDesignFrameworkModule,
    MatGridListModule,
    // Common modules
    SharedModule,
    // Internal modules
    WorkspaceRoutingModule,
  ],
})
export class WorkspaceModule {}
