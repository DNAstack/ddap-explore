import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AgGridModule } from 'ag-grid-angular';
import { ViewControllerService } from 'ddap-common-lib';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { BeaconConfigService } from '../beacon-config.service';
import { BeaconNetworkService } from '../beacon-service/beacon-network.service';
import { Beacon, BeaconRegistry } from '../beacon-service/beacon.model';

@Component({
  selector: 'ddap-beacon-list',
  templateUrl: './beacons-list.component.html',
  styleUrls: ['./beacons-list.component.scss'],
})
export class BeaconListComponent implements OnInit {
  appConfig: AppConfigModel;
  registries: BeaconRegistry[];
  registry: BeaconRegistry;
  beacons: Beacon[];

  view: {
    isRefreshingBeacons: boolean,
    errorLoadingBeacons: boolean,
    wrapTableContent: boolean
  };
  grid: any;

  columnDefs: any[];
  rowData: any[];

  private gridApi;
  private gridColumnApi;

  constructor(private router: Router,
              private appConfigService: AppConfigService,
              private beaconConfigService: BeaconConfigService,
              private beaconNetworkService: BeaconNetworkService,
              private viewController: ViewControllerService
              ) {

                this.view = {
                  isRefreshingBeacons : false,
                  errorLoadingBeacons : false,
                  wrapTableContent : false,
                };

                this.grid = {
                  animateRows: true,
                  multiSortKey: 'ctrl',
                  defaultColumnDefinition: {
                    sortable: true,
                    resizable: true,
                    filter: true,
                  },
                  floatingFilter: true,
                  paginationAutoPageSize: true,
                  makeFullWidth: true,
                  pagination: false,
                  domLayout: 'normal',
                  suppressCellSelection: true,
                };

                this.columnDefs = [
                  {field: 'name'},
                  {field: 'organization'},
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

  onRowClicked(event) {
    const index = event.rowIndex;
    const selectedBeacon = this.beacons[index];
    this.goToSearchPageForBeacon(selectedBeacon.id);
  }

  goToSearchPageForBeacon(beaconId: string) {
    this.router.navigate([this.viewController.getRealmId(), 'beacon', 'search'],
    { queryParams: { network: this.registry.id, beacon: beaconId } });
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
  }

  private setRegistry(r: BeaconRegistry) {
    this.registry = r;
    this.beaconNetworkService.setApiUrl(r.apiUrl);

    // Refresh Beacons
    this.view.errorLoadingBeacons = false;
    this.view.isRefreshingBeacons = true;

    const that = this;

    this.beaconNetworkService.getBeacons().then(
      data => {
        const results = [];
        for (let i = 0; i < data.length; i++) {
          const row = {
            name : data[i]['name'],
            organization : data[i]['organization'],
          };
          results.push(row);
        }
        that.rowData = results;
        that.gridApi.setRowData(results);
        that.beacons = data;
        that.view.isRefreshingBeacons = false;
      },
      error => {
        that.view.errorLoadingBeacons = true;
        that.view.isRefreshingBeacons = false;
      }
    );
  }

}