import { NgModule } from '@angular/core';
import { MatToolbarModule } from '@angular/material';
import { AgGridModule } from 'ag-grid-angular';

import { SharedModule } from '../shared/shared.module';

import { DiscoveryRoutingModule } from './discovery-routing.module';
import { DiscoverySearchComponent } from './discovery-search/discovery-search.component';
import { DiscoveryBeaconComponent } from './discovery-beacon/discovery-beacon.component';
import { KeyValuePairComponent } from './ui-components/key-value-pair/key-value-pair.component';
import { AgmCoreModule } from '@agm/core';

@NgModule({
  declarations: [
    DiscoverySearchComponent,
    DiscoveryBeaconComponent,
    KeyValuePairComponent
  ],
    imports: [
      SharedModule,
      DiscoveryRoutingModule,
      MatToolbarModule,
      AgGridModule.withComponents([]),
      AgmCoreModule.forRoot({
        apiKey: 'AIzaSyBzofbWCwf9myxQpZTME0TcyccBDdhSg88'
      })
    ],
})
export class DiscoveryModule { }
