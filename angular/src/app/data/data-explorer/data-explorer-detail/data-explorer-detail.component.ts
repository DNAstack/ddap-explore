import { KeyValue } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AfterViewInit, Component, Pipe, PipeTransform, ViewChild } from '@angular/core';
import { MatSnackBar } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import linkifyStr from 'linkifyjs/string';
import { catchError, delay, map } from 'rxjs/operators';

import { Collection } from '../../model/collection';
import { ServiceInfo } from '../../model/service-info';
import { DataCollectionsConfigService } from '../data-explorer-collections-config.service';
import { Table, TableData } from '../search-service/ga4gh-discovery-search.models';
import { Ga4ghDiscoverySearchService } from '../search-service/ga4gh-discovery-search.service';

import { JsonViewerService } from './json-viewer/json-viewer.component';
import { SearchFilter } from './search-filter/search-filter';
import { TableFilteredSearchService } from './table-filtered-search.service';

@Component({
  templateUrl: './data-explorer-detail.component.html',
  styleUrls: [ './data-explorer-detail.component.scss' ],
})
export class DataExplorerDetailComponent implements AfterViewInit {

  @ViewChild('editor', {static: false}) editor;

  tableData: TableData;
  tables: Table[];
  tableNames: Set<string>;
  propertyNames: Set<string>;

  private QUERY_EDITOR_DELIMITER = ';';
  private QUERY_EDITOR_NEWLINE = '\n';

  private search: {
    text: string,
  };

  private collection: Collection;
  private searchServiceInfo: ServiceInfo;
  private queryHistory: string[];
  private query: string;
  private result: any;
  private properties: string[];

  private searchService: Ga4ghDiscoverySearchService;

  private view: {
    errorLoadingTables: boolean;
    errorQueryingTables: boolean;
    showQueryEditor: boolean;
    showTables: boolean;
    wrapSearchResults: boolean;
    isSearching: boolean;
    isRefreshingTables: boolean;
  };

  private options: {
    wrapBehavioursEnabled: true
  };

  private datasets: any;
  private accessToken: string;
  private headers: HttpHeaders;
  private filters: SearchFilter[];

  constructor(
    private http: HttpClient, // TODO: only here to construct TableFilteredSearchService, consider getting this out of here.
    private snackBar: MatSnackBar,
    private dataAppConfigService: DataCollectionsConfigService,
    private jsonViewerService: JsonViewerService,
    private route: ActivatedRoute,
    private httpClient: HttpClient
  ) {

    this.tables = [];

    const collectionId = this.route.snapshot.paramMap.get('id');
    this.collection = dataAppConfigService.getCollectionById(collectionId);
    this.searchServiceInfo = dataAppConfigService.getSearchServiceInfoForCollection(this.collection);
    this.searchService = new TableFilteredSearchService(http, this.collection.tableFilters);
    this.searchService.setApiUrl(this.searchServiceInfo.url);
    this.accessToken = this.searchServiceInfo.accessToken;

    this.headers = new HttpHeaders().append('Content-Type', 'application/json');
    if (this.accessToken) {
      this.headers = this.headers.append('Authorization', 'Bearer ' + this.accessToken);
    }

    this.search = { text : '' };

    this.view = {
      errorLoadingTables : true,
      errorQueryingTables : true,
      showQueryEditor : true,
      showTables : true,
      wrapSearchResults : false,
      isSearching : false,
      isRefreshingTables : false,
    };

    this.queryHistory = [];
    this.queryHistory.unshift = function () {
      if (this.length >= 5) {
          this.pop();
      }
      return Array.prototype.unshift.apply(this, arguments);
    };

    this.refreshTables();
  }

  queryChanged($event) {
  }

  refreshTables() {
    this.view.isRefreshingTables = true;
    this.view.errorLoadingTables = false;

    this.searchService.setApiUrl(this.searchServiceInfo.url);
    this.searchService.getTables(this.headers).then(
      data => {
        this.tables = data['tables'];
        this.tableNames = new Set<string>(this.tables.map(a => a.name));

        let propertyNames = [];
        for (let i = 0; i < this.tables.length; i++) {
          propertyNames = propertyNames.concat(Object.keys(this.tables[i].data_model.properties));
        }
        this.propertyNames = new Set<string>(propertyNames);

        this.view.isRefreshingTables = false;
      },
      error => {
        this.view.errorLoadingTables = true;
        this.view.isRefreshingTables = false;
      }
    );
  }

  closeTables() {
    this.view.showTables = false;
  }

  propertyOrder = (a: KeyValue<string, any>, b: KeyValue<string, any>): number => {
    const positionKey = 'x-ga4gh-position';
    const aPos = a[positionKey];
    const bPos = b[positionKey];
    return aPos > bPos ? -1 : (bPos > aPos ? 1 : 0);
  }

  previewTableQuery(tableName: string) {
    return 'SELECT * FROM ' + tableName + ' LIMIT 50;';
  }

  getQueryFromEditor() {

    let query = this.editor.getEditor().getValue();

    query = query.trim();
    query = query.replace('/' + this.QUERY_EDITOR_DELIMITER + '+$/', '');

    let cursorPosition = this.editor.getEditor().session.doc.positionToIndex(this.editor.getEditor().selection.getCursor());
    if (cursorPosition >= query.length) {
      cursorPosition = query.length - 1;
    }

    while (query[cursorPosition] === this.QUERY_EDITOR_NEWLINE || query[cursorPosition] === ' ') {
      cursorPosition = cursorPosition - 1;
      if (cursorPosition === 0) {
        break;
      }
    }

    if (cursorPosition > 0 && query[cursorPosition - 1] === this.QUERY_EDITOR_DELIMITER) {
      cursorPosition = cursorPosition - 1;
    }

    let leftPosition = cursorPosition;

    while (leftPosition > 0) {
      if (query[leftPosition] === this.QUERY_EDITOR_DELIMITER && cursorPosition !== leftPosition) {
        leftPosition = leftPosition + 1;
        break;
      }
      leftPosition = leftPosition - 1;
    }

    let rightPosition = cursorPosition;
    while (rightPosition <= query.length + 1) {
      if (query[rightPosition] === this.QUERY_EDITOR_DELIMITER) {
        break;
      }
      rightPosition = rightPosition + 1;
    }

    query = query.substring(leftPosition, rightPosition).trim();

    return query;
  }


  doSearchFromEditor() {
    this.doSearch(this.getQueryFromEditor());
  }

  doSearch(query: string) {

    if (query.length === 0) {
      this.snackBar.open('Empty query', 'Dismiss', {
        panelClass: 'error-snack',
      });
      return;
    }

    this.view.isSearching = true;
    this.view.errorQueryingTables = false;
    this.searchService.search({ 'query' : query }, this.headers).then(
      result => {
        this.query = query;
        this.result = result;
        this.view.isSearching = false;
        this.queryHistory.unshift(query);
        const schema = result['data_model']['properties'];
        const properties = Object.keys(schema);
        this.properties = properties.filter(e => e !== 'description');
      }
    ).catch(error => {
      this.view.isSearching = false;
        this.view.errorQueryingTables = true;
        this.snackBar.open(error, 'Dismiss', {
          panelClass: 'error-snack',
        });
        return;
    });
  }

  viewTableAsJSON(table: Table) {
    this.jsonViewerService.viewJSON(table);
  }

  viewResultsJSON() {
    this.jsonViewerService.viewJSON(this.result);
  }

  addAtCursor(text: string) {
    if (this.editor != null) {
      const cursorPosition = this.editor.getEditor().session.doc.positionToIndex(this.editor.getEditor().selection.getCursor());

      if (cursorPosition > 0 && this.editor.getEditor().getValue()[cursorPosition - 1] === this.QUERY_EDITOR_DELIMITER) {
        text = this.QUERY_EDITOR_NEWLINE + text;
      }

      this.editor.getEditor().session.replace(this.editor.getEditor().selection.getRange(), text);
      this.editor.getEditor().focus();
    }
  }

  getTableNames() {
    return this.tableNames;
  }

  getPropertyNames() {
    return this.propertyNames;
  }

  ngAfterViewInit() {
    this.editor.getEditor().setOptions({
        enableBasicAutocompletion: true,
        /*enableLiveAutocompletion: true*/ // this introduces weird behaviour where multiple characters get typed with every keypressResults
    });

    this.editor.getEditor().commands.addCommand({
        name: 'showOtherCompletions',
        bindKey: 'Ctrl-.',
        exec: function (editor) {
        },
    });

    this.editor.getEditor().commands.addCommand({
      name: 'run',
      exec: (e) => {
        this.doSearchFromEditor();
      },
      bindKey: {mac: 'cmd-return', win: 'ctrl-enter'},
    });

    const that = this;

    const customCompleter = {

      getCompletions: function(editor, session, pos, prefix, callback) {

        callback(null, Array.from(that.getTableNames()).map(function(word) {
            return {
                caption: word,
                value: word,
                meta: 'table',
            };
        }));

        callback(null, Array.from(that.getPropertyNames()).map(function(word) {
            return {
                caption: word,
                value: word,
                meta: 'property',
            };
        }));
      },

     };
     this.editor.getEditor().completers = [
        this.editor.getEditor().completers,
        customCompleter,
     ];
  }
}
