import { Component, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { BeaconConfigService } from '../beacon-config.service';
import { BeaconNetworkService } from '../beacon-service/beacon-network.service';
import { Beacon, BeaconQuery, BeaconRegistry, BeaconResponse } from '../beacon-service/beacon.model';

@Component({
  selector: 'ddap-beacon-search',
  templateUrl: './beacon-search.component.html',
  styleUrls: ['./beacon-search.component.scss'],
})
export class BeaconSearchComponent implements OnInit {
  appConfig: AppConfigModel;
  registries: BeaconRegistry[];
  registry: BeaconRegistry;
  beacons: Beacon[];
  beacon: Beacon;
  assemblies: string[];
  assembly: string;

  query: BeaconQuery;

  beaconResponses: BeaconResponse[];

  view: {
    isRefreshingBeacons: boolean,
    errorLoadingBeacons: boolean,
    isSearching: boolean,
    errorSearching: boolean,
    wrapTableContent: boolean,
    showQuery: boolean
  };

  constructor(private router: Router,
              private appConfigService: AppConfigService,
              private beaconConfigService: BeaconConfigService,
              private beaconNetworkService: BeaconNetworkService
              ) {

                this.beaconResponses = [];

                this.view = {
                  isRefreshingBeacons: false,
                  errorLoadingBeacons: false,
                  isSearching : false,
                  errorSearching : false,
                  wrapTableContent : false,
                  showQuery: true,
                };
  }

  ngOnInit(): void {
    // Ensure that the user can only access this component when it is enabled.
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      this.appConfig = data;
      if (this.appConfig.featureBeaconsEnabled) {
        this.initialize();
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  refreshBeacons() {
    this.setRegistry(this.registry);
  }

  refreshAssemblies() {
    this.setBeacon(this.beacon);
  }

  doSearch() {

    this.beaconNetworkService.searchBeacon(this.beacon.id, this.query.allele, this.query.chromosome,
      this.query.position - 1, // UI is 1-based, API is 0-based
      this.query.reference, this.query.referenceAllele).then(
      data => {
        this.beaconResponses = data;
        this.view.isSearching = false;
      },
      error => {
        this.view.errorSearching = true;
        this.view.isSearching = false;
      }
    );
  }

  private initialize() {
    this.registries = this.beaconConfigService.getRegistries();
    if (this.registries.length > 0) {
      this.setRegistry(this.registries[0]);
    }

    this.query = new BeaconQuery();
    this.query.chromosome = '1';
    this.query.position = 1;
    this.query.allele = 'A';
    this.query.referenceAllele = 'A';
  }

  private setRegistry(r: BeaconRegistry) {
    this.registry = r;
    this.beaconNetworkService.setApiUrl(r.apiUrl);

    // Refresh Beacons
    this.view.errorLoadingBeacons = false;
    this.view.isRefreshingBeacons = true;
    this.beaconNetworkService.getBeacons().then(
      data => {
        this.beacons = data;
        this.view.isRefreshingBeacons = false;
        if (this.beacons.length > 0) {
          this.setBeacon(this.beacons[0]);
        }
      },
      error => {
        this.view.errorLoadingBeacons = true;
        this.view.isRefreshingBeacons = false;
      }
    );
  }

  private setBeacon(b: Beacon) {
    this.beacon = b;
    this.assemblies = this.beacon.supportedReferences;
    if (this.assemblies.length > 0) {
      this.query.reference = this.assemblies[0];
    }
  }
}
