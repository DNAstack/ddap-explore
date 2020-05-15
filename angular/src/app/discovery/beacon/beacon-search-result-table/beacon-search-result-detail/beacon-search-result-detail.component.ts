import { ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ILatLong } from 'angular-maps';
import _get from 'lodash.get';
import { EMPTY, Observable } from 'rxjs';

import { JsonSchema } from '../../../../shared/search/json-schema.model';

import { GeocodeService } from './geo-location/geocode.service';

@Component({
  selector: 'ddap-beacon-search-result-detail',
  templateUrl: './beacon-search-result-detail.component.html',
  styleUrls: ['./beacon-search-result-detail.component.scss'],
})
export class BeaconSearchResultDetailComponent implements OnChanges {

  @Input()
  selectedRowData: any;

  @Input()
  schema: JsonSchema;

  mapCoordinates: ILatLong;

  constructor(
    private changeDetectorRef: ChangeDetectorRef,
    private geocodeService: GeocodeService
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.buildMapCoordinates()
      .subscribe((coordinates: ILatLong) => {
        this.mapCoordinates = coordinates;
        // This needs to be present otherwise map is not redrawn until detail panel is clicked, since all changes
        // happen in x-map component
        this.changeDetectorRef.detectChanges();
      });
  }

  buildNextStrainUrl(source?): string {
    if (!source) {
      return;
    }
    const tokens = source.split('/');
    if (tokens.length < 3) {
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
