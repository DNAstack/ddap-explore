import { Component, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';
import { AgGridModule } from 'ag-grid-angular';

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

  grid: any;

  columnDefs: any[];
  rowData: any[];

  private gridApi;
  private gridColumnApi;

  constructor(private router: Router,
              private appConfigService: AppConfigService,
              private beaconConfigService: BeaconConfigService,
              private beaconNetworkService: BeaconNetworkService
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

        const results = [];
        for (let i = 0; i < data.length; i++) {
          const row = {
            beacon : data[i]['beacon']['name'],
            organization : data[i]['beacon']['organization'],
            exists : data[i]['response'],
          };
          results.push(row);
        }
        this.rowData = results;
        this.gridApi.setRowData(results);
        this.beaconResponses = data;
        this.view.isSearching = false;
      },
      error => {
        this.view.errorSearching = true;
        this.view.isSearching = false;
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
