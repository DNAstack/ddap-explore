import { AgmCoreModule } from '@agm/core';
import { NgModule } from '@angular/core';
import { MatDialogModule, MatToolbarModule } from '@angular/material';
import { AgGridModule } from 'ag-grid-angular';
import {
  BingMapAPILoader,
  BingMapAPILoaderConfig,
  DocumentRef,
  MapAPILoader,
  MapModule,
  WindowRef,
} from 'angular-maps';

import { SharedModule } from '../shared/shared.module';

import { BeaconInfoBarComponent } from './beacon/beacon-info-bar/beacon-info-bar.component';
import { BeaconSearchBarComponent } from './beacon/beacon-search-bar/beacon-search-bar.component';
import {
  BeaconSearchResultDetailComponent
} from './beacon/beacon-search-result-table/beacon-search-result-detail/beacon-search-result-detail.component';
import {
  GeoLocationComponent
} from './beacon/beacon-search-result-table/beacon-search-result-detail/geo-location/geo-location.component';
import { GeocodeService } from './beacon/beacon-search-result-table/beacon-search-result-detail/geo-location/geocode.service';
import {
  TableCellDataComponent
} from './beacon/beacon-search-result-table/beacon-search-result-detail/table-cell-data/table-cell-data.component';
import {
  BeaconSearchResultTableComponent
} from './beacon/beacon-search-result-table/beacon-search-result-table.component';
import { BeaconSearchComponent } from './beacon/beacon-search.component';
import { HelpDialogComponent } from './beacon/help-dialog/help-dialog.component';
import { DiscoveryRoutingModule } from './discovery-routing.module';

@NgModule({
  declarations: [
    BeaconSearchComponent,
    BeaconInfoBarComponent,
    BeaconSearchBarComponent,
    BeaconSearchResultTableComponent,
    BeaconSearchResultDetailComponent,
    TableCellDataComponent,
    GeoLocationComponent,
    HelpDialogComponent,
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
  exports: [
    BeaconSearchComponent,
  ],
  providers: [
    GeocodeService,
    {
      provide: MapAPILoader, deps: [], useFactory: MapServiceProviderFactory,
    },
  ],
  entryComponents: [
    HelpDialogComponent,
  ],
})
export class DiscoveryModule {
}

export function MapServiceProviderFactory() {
  const bc: BingMapAPILoaderConfig = new BingMapAPILoaderConfig();
  bc.apiKey = 'Ag39DG994D1KXnAPKO0x_rH7HpKEIUhWtDwtHZ0q3P-6SUWDB9VCUNlnO1IrS9gp'; // your bing map key
  bc.branch = 'experimental';
  // to use the experimental bing brach. There are some bug fixes for
  // clustering in that branch you will need if you want to use
  // clustering.
  return new BingMapAPILoader(bc, new WindowRef(), new DocumentRef());
}
