import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ILatLong } from 'angular-maps';
import _get from 'lodash.get';
import { EMPTY, Observable } from 'rxjs';

import { GeocodeService } from './geo-location/geocode.service';

@Component({
  selector: 'ddap-beacon-search-result-detail',
  templateUrl: './beacon-search-result-detail.component.html',
  styleUrls: ['./beacon-search-result-detail.component.scss'],
})
export class BeaconSearchResultDetailComponent implements OnChanges {

  @Input()
  selectedRowData: any;

  mapCoordinates: ILatLong;

  constructor(private geocodeService: GeocodeService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.buildMapCoordinates()
      .subscribe((coordinates: ILatLong) => {
        this.mapCoordinates = coordinates;
      });
  }

  buildNextStrainUrl(source?): string {
    if (!source) {
      return;
    }
    const tokens = source.split('/');
    if (tokens.length === 1) {
      return;
    }
    return `https://nextstrain.org/ncov?s=${tokens[1]}/${tokens[2]}/${tokens[3]}`;
  }

  buildMapCoordinates(): Observable<ILatLong> {
    const location: string = _get(this.selectedRowData, 'Location');

    if (!location) {
      return EMPTY;
    }
    return this.geocodeService.geocodeAddress(location);
  }

}
