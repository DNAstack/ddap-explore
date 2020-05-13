import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, combineAll, flatMap, map } from 'rxjs/operators';

import { BeaconInfoResourcePair, BeaconInfoResponseModel } from '../shared/apps/app-discovery/app-discovery.model';
import { AppDiscoveryService } from '../shared/apps/app-discovery/app-discovery.service';
import { ResourceModel, ResourcesResponseModel } from '../shared/apps/resource.model';
import { ResourceService } from '../shared/apps/resource.service';

@Injectable({
  providedIn: 'root',
})
export class DiscoveryBeaconService {

  constructor(
    private http: HttpClient,
    private resourceService: ResourceService,
    private appDiscoveryService: AppDiscoveryService
  ) {
  }

  getBeaconInfoResourcePairs(): Observable<BeaconInfoResourcePair[]> {
    return this.resourceService.getResources({ interface_type: 'http:beacon' })
      .pipe(
        map((resourcesResponse: ResourcesResponseModel) => resourcesResponse.data),
        flatMap((resources: ResourceModel[]) => {
          return resources.map((resource: ResourceModel) => this.getBeaconInfoResourcePair(resource));
        }),
        combineAll()
      );
  }

  private getBeaconInfoResourcePair(resource: ResourceModel): Observable<BeaconInfoResourcePair> {
    return this.appDiscoveryService.getBeaconInfo(resource.interfaces[0].id)
      .pipe(
        map((beaconInfo: BeaconInfoResponseModel) => {
          return { resource, beaconInfo };
        }),
        catchError((error) => {
          return of({ resource, error });
        })
      );
  }

}
