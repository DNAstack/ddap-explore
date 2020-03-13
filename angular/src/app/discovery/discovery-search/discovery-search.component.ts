import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AgGridModule } from 'ag-grid-angular';
import { ViewControllerService } from 'ddap-common-lib';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { DiscoveryConfigService } from '../discovery-config.service';
import { BeaconService } from '../beacon-service/beacon.service';

@Component({
  selector: 'ddap-discovery-search',
  templateUrl: './discovery-search.component.html',
  styleUrls: ['./discovery-search.component.scss'],
})
export class DiscoverySearchComponent implements OnInit {
  appConfig: AppConfigModel;

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
              private configService: DiscoveryConfigService,
              private beaconNetworkService: BeaconService,
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
  }
}
