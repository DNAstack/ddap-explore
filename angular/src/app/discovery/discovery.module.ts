import { AgmCoreModule, MapsAPILoader } from '@agm/core';
import { NgModule } from '@angular/core';
import { MatDialogModule, MatDialogRef, MatToolbarModule } from '@angular/material';
import { AgGridModule } from 'ag-grid-angular';
import { BingMapAPILoader, BingMapAPILoaderConfig, DocumentRef, MapAPILoader, MapModule, WindowRef } from 'angular-maps';

import { SharedModule } from '../shared/shared.module';

import { DiscoveryBeaconComponent } from './discovery-beacon/discovery-beacon.component';
import { GeocodeService } from './discovery-beacon/geocode/geocode.service';
import { DiscoveryBeaconHelpDialogComponent } from './discovery-beacon/help/discovery-beacon.help.dialog';
import { DiscoveryRoutingModule } from './discovery-routing.module';
import { KeyValuePairComponent } from './ui-components/key-value-pair/key-value-pair.component';

@NgModule({
  declarations: [
    DiscoveryBeaconComponent,
    KeyValuePairComponent,
    DiscoveryBeaconHelpDialogComponent,
  ],
    imports: [
      SharedModule,
      DiscoveryRoutingModule,
      MatToolbarModule,
      MatDialogModule,
      AgGridModule.withComponents([]),
      MapModule.forRoot(),
      AgmCoreModule.forRoot({
        apiKey: 'AIzaSyBzofbWCwf9myxQpZTME0TcyccBDdhSg88',
      }),
    ],
    providers: [
      GeocodeService,
      {
        provide: MapAPILoader, deps: [], useFactory: MapServiceProviderFactory,
      },
      {
        provide: MatDialogRef,
        useValue: {},
      },
    ],
    entryComponents: [
      DiscoveryBeaconHelpDialogComponent,
    ],

})
export class DiscoveryModule { }

export function MapServiceProviderFactory() {
  const bc: BingMapAPILoaderConfig = new BingMapAPILoaderConfig();
  bc.apiKey = 'Ag39DG994D1KXnAPKO0x_rH7HpKEIUhWtDwtHZ0q3P-6SUWDB9VCUNlnO1IrS9gp'; // your bing map key
  bc.branch = 'experimental';
      // to use the experimental bing brach. There are some bug fixes for
      // clustering in that branch you will need if you want to use
      // clustering.
  return new BingMapAPILoader(bc, new WindowRef(), new DocumentRef());
}
