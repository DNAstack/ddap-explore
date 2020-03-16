import { Injectable } from '@angular/core';
import { MapsAPILoader } from '@agm/core';
import { tap, map, switchMap } from 'rxjs/operators';
import { Observable, Subject, of, from } from 'rxjs';
import { ILatLong } from 'angular-maps';

declare var google: any;

@Injectable()
export class GeocodeService {
  private geocoder: any;

  constructor(private mapLoader: MapsAPILoader) {}

  private initGeocoder() {
    this.geocoder = new google.maps.Geocoder();
  }

  private waitForMapsToLoad(): Observable<boolean> {
    if(!this.geocoder) {
      return from(this.mapLoader.load())
      .pipe(
        tap(() => this.initGeocoder()),
        map(() => true)
      );
    }
    return of(true);
  }

  geocodeAddress(location: string): Observable<any> {
    return this.waitForMapsToLoad().pipe(
      // filter(loaded => loaded),
      switchMap(() => {
        return new Observable(observer => {
          this.geocoder.geocode({'address': location}, (results, status) => {
            if (status == google.maps.GeocoderStatus.OK) {
              observer.next(<ILatLong>{
                latitude: results[0].geometry.location.lat(), 
                longitude: results[0].geometry.location.lng()
              });
            } else {
                observer.next(<ILatLong>{ latitude: 0, longitude: 0 });
            }
            observer.complete();
          });
        })        
      })
    )
  }
  
}