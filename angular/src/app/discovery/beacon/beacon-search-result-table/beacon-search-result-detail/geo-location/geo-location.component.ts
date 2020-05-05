import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { IMapOptions } from 'angular-maps';

@Component({
  selector: 'ddap-geo-location',
  templateUrl: './geo-location.component.html',
  styleUrls: ['./geo-location.component.scss'],
})
export class GeoLocationComponent implements OnChanges {

  @Input()
  title: string;
  @Input()
  latitude: number;
  @Input()
  longitude: number;

  readonly defaultMapOptions: IMapOptions = {
    disableBirdseye: true,
    disableStreetside: true,
    showCopyright: false,
    showMapTypeSelector: false,
    navigationBarMode: 2,
    mapTypeId: 7,
    zoom: 4,
    center: {
      longitude: 0,
      latitude: 0,
    },
  };

  ngOnChanges(changes: SimpleChanges): void {
    this.defaultMapOptions.center.longitude = this.longitude;
    this.defaultMapOptions.center.latitude = this.latitude;
  }

}
