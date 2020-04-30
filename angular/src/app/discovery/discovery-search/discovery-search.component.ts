import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'ddap-discovery-search',
  templateUrl: './discovery-search.component.html',
  styleUrls: ['./discovery-search.component.scss'],
})
export class DiscoverySearchComponent {

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

  constructor() {
    this.view = {
      isRefreshingBeacons: false,
      errorLoadingBeacons: false,
      wrapTableContent: false,
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
      { field: 'name' },
      { field: 'organization' },
    ];

    this.rowData = [];
  }

  onRowClicked(event) {
    const index = event.rowIndex;
  }

  onGridReady(params) {
    this.gridApi = params.api;
    this.gridColumnApi = params.columnApi;
    if (this.grid.makeFullWidth) {
      params.api.sizeColumnsToFit();
      window.addEventListener('resize', function () {
        setTimeout(function () {
          params.api.sizeColumnsToFit();
        });
      });
    }
  }

}
