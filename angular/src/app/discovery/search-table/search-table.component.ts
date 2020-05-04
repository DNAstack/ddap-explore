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

    conditionMap = {};

    grid: any;

    view: {
        isSearching: boolean,
        errorQueryingTable: boolean,
        fieldConditionsChanged: boolean,
        resultSize: number
    };

    resultSizes = [10, 50, 100];

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
                        fieldConditionsChanged: false,
                        resultSize: 50,
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
                        paginationAutoPageSize: false,
                        makeFullWidth: true,
                        pagination: false,
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

    conditionChangedForField(condition, colDef) {

      this.view.fieldConditionsChanged = true;

      // remove the condition if all values are the empty string
      let removeCondition = true;
      for (let i = 0; i < condition.values.length; i++) {
        if (condition.values[i] !== '') {
          removeCondition = false;
          break;
        }
      }
      if (removeCondition) {
        delete this.conditionMap[colDef.field];
        return;
      }

      this.conditionMap[colDef.field] = condition;
    }

    resultSizeChanged() {
      this.view.fieldConditionsChanged = true;
    }

    operatorsForColumn(colDef) {
      // text
      if (!colDef.type) {
        return [
          { name: 'contains', arity: Arity.unary },
          { name: 'doesn\'t contain', arity: Arity.unary },
          { name: 'starts with', arity: Arity.unary },
          { name: 'ends with', arity: Arity.unary },
          { name: 'is', arity: Arity.unary },
          { name: 'is not', arity: Arity.unary },
        ];

        // number
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
      const query = 'SELECT * FROM ' + this.table + ' WHERE ' + this.getWhereClause() + ' LIMIT ' + this.view.resultSize;
      // console.log(query);
      return query;
    }

    getWhereClause() {
      let clause = '1 = 1';
      const that = this;
      Object.keys(this.conditionMap).forEach(function(key) {
        clause = clause + ' AND ' + that.clauseForCondition(key, that.conditionMap[key]);
      });
      return clause;
    }

    clauseForCondition(field: string, condition: any) {
      const op = condition.operator;
      const values = condition.values;

      // text operations
      if (op.name === 'contains') {
        return '(' + field + ' LIKE \' %' + values[0] + '%\'' + ')';
      } else if (op.name === 'doesn\'t contain') {
        return '(' + field + ' NOT LIKE \' %' + values[0] + '%\'' + ')';
      } else if (op.name === 'starts with') {
        return '(' + field + ' LIKE \'' + values[0] + '%\'' + ')';
      } else if (op.name === 'ends with') {
        return '(' + field + ' LIKE \' %' + values[0] + '\'' + ')';
      } else if (op.name === 'is') {
        return '(' + field + ' = \'' + values[0] + '\'' + ')';
      } else if (op.name === 'is not') {
        return '(' + field + ' <> \'' + values[0] + '\'' + ')';
      }

      // numeric operations
      if (op.name === 'equals') {
        return '(' + field + ' = ' + Number(values[0]) + ')';
      } else if (op.name === 'greater than') {
        return '(' + field + ' > ' + Number(values[0]) + ')';
      } else if (op.name === 'less than') {
        return '(' + field + ' < ' + Number(values[0]) + ')';
      } else if (op.name === 'between') {
        return '(' + field + ' BETWEEN ' + Number(values[0]) + ' AND ' + Number(values[1]) + ')';
      }

      return '1 = 1';
    }

    executeSearch() {
        this.executeSearchWithQuery(this.getQuery());
    }

    executeSearchWithQuery(query: string) {
        this.results = null;
        this.view.isSearching = true;
        this.view.errorQueryingTable = false;

        // console.log(query);

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
          this.view.fieldConditionsChanged = false;
          const that = this;

          // console.log(result);

          // Set column definitions
          if (!that.columnDefs) {

            const columnDefs = [];

            Object.keys(result.data_model.properties).map(function(key) {

              const field = {'field': key};
              if (result.data_model.properties[key]['type'] === 'int') {
                field['type'] = 'numericColumn';
                field['filter'] = 'agNumberColumnFilter';
              }

              /*if (result.data_model.properties[key]['type'] === 'string') {
                field['type'] = 'textColumn';
              }*/

              if (that.fieldMap && that.fieldMap[key]) {
                field['headerName'] = that.fieldMap[key];
              }

              columnDefs.push(field);
              return columnDefs;
            });

            this.columnDefs = columnDefs;
          }

          // Set row data
          this.rowData = result.data;

          this.gridApi.setRowData(this.rowData);

          this.view.isSearching = false;
        },
        error => {
          // console.log(error);
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

