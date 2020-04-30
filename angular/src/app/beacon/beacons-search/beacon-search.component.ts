import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { BeaconConfigService } from '../beacon-config.service';
import { BeaconNetworkService } from '../beacon-service/beacon-network.service';
import { Beacon, BeaconQuery, BeaconRegistry, BeaconResponse } from '../beacon-service/beacon.model';

@Component({
  selector: 'ddap-beacon-search',
  templateUrl: './beacon-search.component.html',
  styleUrls: ['./beacon-search.component.scss'],
})
export class BeaconSearchComponent {

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

  selectedRegistryId: string;
  selectedBeaconId: string;

  grid: any;

  columnDefs: any[];
  rowData: any[];

  private gridApi;
  private gridColumnApi;

  private queryParameters: any;

  constructor(private router: Router,
              private beaconConfigService: BeaconConfigService,
              private beaconNetworkService: BeaconNetworkService,
              private route: ActivatedRoute
              ) {

                this.grid = {
                  animateRows: true,
                  multiSortKey: 'ctrl',
                  defaultColumnDefinition: {
                    sortable: true,
                    resizable: true,
                    filter: true,
                  },
                  makeFullWidth: true,
                  pagination: false,
                  domLayout: 'autoHeight',
                  enableStatusBar: true,
                  suppressCellSelection: true,
                };

                this.beaconResponses = [];

                this.view = {
                  isRefreshingBeacons: false,
                  errorLoadingBeacons: false,
                  isSearching : false,
                  errorSearching : false,
                  wrapTableContent : false,
                  showQuery: true,
                };


                this.columnDefs = [
                  {field: 'beacon'},
                  {field: 'organization'},
                  {field: 'exists'},
                ];

                this.rowData = [];
  }

  refreshBeacons() {
    this.setRegistry(this.registry);
  }

  refreshAssemblies() {
    this.setBeacon(this.beacon);
  }

  doSearch() {

    const that = this;

    this.beaconNetworkService.searchBeacon(this.beacon.id, this.query.allele, this.query.chromosome,
      this.query.position - 1, // UI is 1-based, API is 0-based
      this.query.reference, this.query.referenceAllele).then(
      data => {

        const results = [];
        for (let i = 0; i < data.length; i++) {
          const row = {
            beacon : data[i]['beacon']['name'],
            organization : data[i]['beacon']['organization'],
            exists : data[i]['response'],
          };
          results.push(row);
        }
        that.rowData = results;
        that.gridApi.setRowData(results);
        that.beaconResponses = data;
        that.view.isSearching = false;
      },
      error => {
        that.view.errorSearching = true;
        that.view.isSearching = false;
      }
    );
  }

  onGridReady(params) {
    this.gridApi = params.api;
    this.gridColumnApi = params.columnApi;
    if (this.grid.makeFullWidth) {
      params.api.sizeColumnsToFit();
      window.addEventListener('resize', function() {
        setTimeout(function() {
          params.api.sizeColumnsToFit();
        });
      });
    }
  }

  private initialize() {

    this.query = new BeaconQuery();
    this.query.chromosome = '1';
    this.query.position = 1;
    this.query.allele = 'A';
    this.query.referenceAllele = 'A';

    this.selectedRegistryId = this.route.snapshot.queryParamMap.get('network');
    this.selectedBeaconId = this.route.snapshot.queryParamMap.get('beacon');

    this.registries = this.beaconConfigService.getRegistries();
    if (this.registries.length > 0) {

      if (this.selectedRegistryId == null || this.selectedBeaconId.length === 0) {
        this.setRegistry(this.registries[0]);
      } else {
        for (let i = 0; i < this.registries.length; i++) {
          if (this.registries[i].id === this.selectedRegistryId) {
            this.setRegistry(this.registries[i]);
          }
        }
      }
    }
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

        if (this.selectedBeaconId == null || this.selectedBeaconId.length === 0) {
          this.setBeacon(this.beacons[0]);
        } else {
          for (let i = 0; i < this.beacons.length; i++) {
            if (this.beacons[i].id === this.selectedBeaconId) {
              this.setBeacon(this.beacons[i]);
            }
          }
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
    this.setQueryParameters();
    this.assemblies = this.beacon.supportedReferences;
    if (this.assemblies.length > 0) {
      this.query.reference = this.assemblies[0];
    }
  }

  private setQueryParameters() {
    this.router.navigate(
      [],
      {
        relativeTo: this.route,
        queryParams: {
          network : this.registry.id,
          beacon: this.beacon.id,
        },
        queryParamsHandling: 'merge', // remove to replace all query params by provided
      });
  }
}
