import { ArrayDataSource } from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { AfterViewInit, Component, ElementRef, HostListener, Input, OnInit, ViewChild } from '@angular/core';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { ViewControllerService } from 'ddap-common-lib';
import * as ngl from 'ngl';
import { SearchResourceModel } from 'src/app/search/search-resources/search-resource.model';
import { SearchService } from 'src/app/search/search.service';
import { AppConfigModel } from 'src/app/shared/app-config/app-config.model';
import { AppConfigService } from 'src/app/shared/app-config/app-config.service';
import { TableModel } from 'src/app/shared/search/table.model';

import { DiscoveryConfigService } from '../discovery-config.service';

import { Arity } from './field-filter/field-filter.component';

@Component({
    selector: 'ddap-search-table',
    templateUrl: './search-table.component.html',
    styleUrls: [],
  })
  export class SearchTableComponent implements OnInit, AfterViewInit {

    @Input() service: string;
    @Input() table: string;
    @Input() fieldMap?: any;
    @Input() showLeftSidebar?: boolean;
    @Input() showRightSidebar?: boolean;

    appConfig: AppConfigModel;

    columnDefs: any[];
    rowData: any[];
    filters: any;

    grid: any;

    view: {
        isSearching: boolean,
        errorQueryingTable: boolean
    };

    searchServiceView: any;

    queryError: ParsedBackendError;
    results: TableModel;
    connectorDetails: object = {};

    private gridApi;
    private gridColumnApi;

    constructor(private router: Router,
                private appConfigService: AppConfigService,
                private route: ActivatedRoute,
                private searchService: SearchService,
                private configService: DiscoveryConfigService,
                private viewController: ViewControllerService
                ) {

                    this.view = {
                        isSearching: false,
                        errorQueryingTable: false,
                    };

                    this.grid = {
                        animateRows: true,
                        multiSortKey: 'ctrl',
                        defaultColumnDefinition: {
                          sortable: true,
                          resizable: true,
                          filter: true,
                        },
                        floatingFilter: false,
                        paginationAutoPageSize: true,
                        makeFullWidth: true,
                        pagination: true,
                        domLayout: 'normal',
                        suppressCellSelection: true,
                      };


    }

    initializeSearchServiceWithName(searchServiceName: string) {
        const that = this;
        this.searchService.getResourceDetail(searchServiceName).subscribe((views: SearchResourceModel[]) => {
            that.searchServiceView = views[0];
            that.executeSearch();
        });
    }

    compileStats() {

    }

    operatorsForColumn(colDef) {
      if (!colDef.type || colDef.type === 'textColumn') {
        return [
          { name: 'contains', arity: Arity.unary },
          { name: 'starts with', arity: Arity.unary },
          { name: 'ends with', arity: Arity.unary },
          { name: 'is', arity: Arity.unary },
          { name: 'is not', arity: Arity.unary },
        ];
      } else if (colDef.type === 'numericColumn') {
        return [
          { name: 'equals', arity: Arity.unary },
          { name: 'greater than', arity: Arity.unary },
          { name: 'less than', arity: Arity.unary },
          { name: 'between', arity: Arity.binary },
        ];
      }
    }

    getQuery() {
      return 'SELECT * FROM ' + this.table + ' WHERE ' + this.getWhereClause() + ' LIMIT 50';
    }

    getWhereClause() {
      return '1 = 1';
    }

    executeSearch() {
        this.executeSearchWithQuery(this.getQuery());
    }

    executeSearchWithQuery(query: string) {
        this.results = null;
        this.view.isSearching = true;
        this.view.errorQueryingTable = false;

        this.searchService.observableSearch(
            this.searchServiceView.interfaceUri,
          {query: query},
          null,
          this.connectorDetails,
          (error) => {
            this.queryError = JSON.parse(error.error.message);
            this.view.isSearching = false;
            throw error;
          }
        ).subscribe(result => {
          this.queryError = null;
          this.results = result;

          // console.log(result.data_model.properties);

          const that = this;

          // Set column definitions
          const columnDefs = [];
          Object.keys(result.data_model.properties).map(function(key) {

            const field = {'field': key};
            if (result.data_model.properties[key]['type'] === 'int') {
              field['type'] = 'numericColumn';
              field['filter'] = 'agNumberColumnFilter';
            }

            if (result.data_model.properties[key]['type'] === 'string') {
              field['type'] = 'textColumn';
            }

            if (that.fieldMap && that.fieldMap[key]) {
              field['headerName'] = that.fieldMap[key];
            }

            columnDefs.push(field);
            return columnDefs;
          });
          this.columnDefs = columnDefs;

          // Set row data
          this.rowData = result.data;

          this.gridApi.setRowData(this.rowData);

          this.view.isSearching = false;
        });
      }

    ngAfterViewInit(): void {
    }

    ngOnInit(): void {
      this.initializeSearchServiceWithName(this.service);
    }

    onFilterChanged(event) {
      const filters = {};
      for (let i = 0; i < this.columnDefs.length; i++) {
        const filter = this.gridApi.getFilterInstance(this.columnDefs[i].field).appliedModel;
        if (filter) {
          filters[this.columnDefs[i].field] = filter;
        }
      }
      this.filters = filters;
      // this.executeSearch();
    }

    onRowClicked(event) {
      // console.log(event);
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
}

interface ParsedBackendError {
    errorName: string;
    message: string;
  }

