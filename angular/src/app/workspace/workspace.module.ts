import { NgModule } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MaterialDesignFrameworkModule } from 'angular7-json-schema-form';

import { SharedModule } from '../shared/shared.module';

import { WorkspaceListComponent } from './workspace-list/workspace-list.component';
import { WorkspaceRoutingModule } from './workspace-routing.module';

@NgModule({
  declarations: [
    WorkspaceListComponent,
  ],
  imports: [
    // First/third-party modules
    FlexLayoutModule,
    MaterialDesignFrameworkModule,
    // Common modules
    SharedModule,
    // Internal modules
    WorkspaceRoutingModule,
  ],
})
export class WorkspaceModule {}
