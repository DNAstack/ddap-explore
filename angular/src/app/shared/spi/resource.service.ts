import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ResourceListResponseModel } from './resource-list-response.model';
import { Resource } from './resource.model';

@Injectable({
  providedIn: 'root',
})
export class SPIResourceService {
  constructor(private http: HttpClient) {
  }

  find(interfaceType: string): Observable<ResourceListResponseModel> {
    return this.http.get<ResourceListResponseModel>(`/api/v1beta/app/resources?interface_type=${interfaceType}`);
  }
}
