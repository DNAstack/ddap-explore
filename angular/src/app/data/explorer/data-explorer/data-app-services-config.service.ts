import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { ServiceInfo } from '../model/service-info';

@Injectable({
  providedIn: 'root',
})
export class DataAppServicesConfigService {
  services;

  constructor(private http: HttpClient) {

    this.services = [
        {
          'id' : 'dnastack-search-service',
          'type' : 'search',
          'url' : 'https://ga4gh-search-adapter-presto-public.staging.dnastack.com',
        },
        {
          'id' : 'shgp-search-service',
          'type' : 'search',
          'url' : 'https://ga4gh-search-adapter-presto-public.staging.sc-dev.dnastack.com',
        },
        {
          'id' : 'dnastack-mssng-search-service',
          'type' : 'search',
          'url' : 'http://localhost:8083/api',
        },
        {
          'id' : 'dnastack-beacon-network',
          'type' : 'search',
          'url' : 'http://localhost:8082',
        },
      ];

  }

  getServices(): ServiceInfo[] {
    return this.services;
  }

  getServicesOfType(type: string): ServiceInfo[] {
    let services: ServiceInfo[];
    services = [];
    for (let i = 0; i < this.services.length; i++) {
      if (this.services[i]['type'] === type) {
        services.push(this.services[i]);
      }
    }
    return services;
  }

  getServiceById(id: string): ServiceInfo {
    for (let i = 0; i < this.services.length; i++) {
      if (this.services[i]['id'] === id) {
        return this.services[i];
      }
    }
  }
}
