import { NgModule } from '@angular/core';
import { MatToolbarModule } from '@angular/material';
import { AgGridModule } from 'ag-grid-angular';

import { SharedModule } from '../shared/shared.module';

import { BeaconsRoutingModule } from './beacon-routing.module';
import { BeaconListComponent } from './beacons-list/beacons-list.component';
import { BeaconSearchComponent } from './beacons-search/beacon-search.component';

@NgModule({
  declarations: [
      BeaconListComponent,
      BeaconSearchComponent,
  ],
    imports: [
      SharedModule,
      BeaconsRoutingModule,
      MatToolbarModule,
      AgGridModule.withComponents([]),
    ],
})
export class BeaconModule { }
