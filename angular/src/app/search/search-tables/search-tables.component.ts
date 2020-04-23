import { KeyValue } from '@angular/common';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatSnackBar, MatSnackBarRef } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import 'brace';
import 'brace/mode/sql';
import 'brace/theme/eclipse';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AppConfigService } from '../../shared/app-config/app-config.service';
import { dam } from '../../shared/proto/dam-service';
import { ResourceService } from '../../shared/resource/resource.service';
import { TableInfo } from '../../shared/search/table-info.model';
import { SearchEditorComponent } from '../search-editor/search-editor.component';
import IResourceResults = dam.v1.IResourceResults;
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;
import { SearchResourceModel } from '../search-resources/search-resource.model';
import { SearchService } from '../search.service';

import { JsonViewerService } from './json-viewer/json-viewer.component';
import { BeaconRegistry, SearchView } from './search-tables.model';
import Table = WebAssembly.Table;


@Component({
  selector: 'ddap-search-detail',
  templateUrl: './search-tables.component.html',
  styleUrls: ['./search-tables.component.scss'],
})
export class SearchTablesComponent implements OnInit {
  @ViewChild(SearchEditorComponent, {static: false})
  searchEditor: SearchEditorComponent;

  tableInfoList: TableInfo[] = [];
  uiTableInfoList: UITableInfo[] = [];

  fixedFlags = {
    workflowIntegrationEnabled: false,
    viewResultsAsJSON: false,  // This is set to false until we have an official approval to use the data for search.
  };

  registry: BeaconRegistry;

  view: SearchView;

  currentQuery = '';

  options: {
    wrapBehavioursEnabled: true
  };

  result: any;
  queryHistory: string[];
  resourcePath: string;
  realm: string;
  accessToken;
  resourceName: string;
  damId: string;
  viewName: string;
  resourceAccessMap;
  interfaceAccessTokensMap;
  currentView;
  tableApiRequests = 0;
  connectorDetails: object = {};

  completedQuery: string;
  properties: string[];

  private snackBarRef: any;

  constructor(private appConfigService: AppConfigService,
              private searchService: SearchService,
              private route: ActivatedRoute,
              private jsonViewerService: JsonViewerService,
              private router: Router,
              private snackBar: MatSnackBar,
              private resourceService: ResourceService) {
    this.view = {
      errorLoadingTables: true,
      errorQueryingTables: true,
      showQueryEditor: true,
      showTables: true,
      wrapSearchResults: false,
      isSearching: false,
      isRefreshingTables: false,
    };

    this.queryHistory = [];
    this.realm = this.route.root.firstChild.snapshot.params.realmId;
  }

  ngOnInit() {
    this.route
      .queryParams
      .subscribe(params => {
        if (params['checkout']) {
          this.damId = this.route.snapshot.params.damId;
          this.resourceName = this.route.snapshot.params.resourceName;
          this.viewName = this.route.snapshot.params.viewName;
          this.searchService.getResourceDetail(this.resourceName).subscribe((views: SearchResourceModel[]) => {
            this.authorizeResource(views);
          });
        } else {
          this.router.navigate([`/${this.realm}`, 'search', 'resources']);
        }
      });
  }

  onTableNameClick(tableName: string) {
    this.searchEditor.addAtCursor(tableName);
  }

  onTableColumnNameClick(columnName: string) {
    this.searchEditor.addAtCursor(`"${columnName}"`);
  }

  onPreviewQueryClick(tableName: string) {
    const previewQuery = this.previewTableQuery(tableName);

    // NOTE If we want to have the query also display in the query editor, we can set the preview query to the current query.
    // this.currentQuery = previewQuery;

    this.doSearch(previewQuery, false);
  }

  onTableInfoExpanded(table: UITableInfo) {
    this.searchService.resolveJsonSchemaReference(table.metadata).subscribe(newSchema => {
      table.metadata.data_model.properties = newSchema.data_model.properties;
    });
  }

  isTablePropertyListFinal(table: UITableInfo): boolean {
    return this.searchService.isTableInfoPropertyListFinal(table.metadata);
  }

  propertyOrder = (a: KeyValue<string, any>, b: KeyValue<string, any>): number => {
    const positionKey = 'x-ga4gh-position';
    const aPos = a[positionKey];
    const bPos = b[positionKey];
    return aPos > bPos ? -1 : (bPos > aPos ? 1 : 0);
  }

  previewTableQuery(tableName: string) {
    return `SELECT * FROM ${tableName} LIMIT 50;`;
  }

  viewTableAsJSON(table: Table) {
    this.jsonViewerService.viewJSON(table);
  }

  buildUiTableInfoList() {
    this.uiTableInfoList = this.tableInfoList.map(tableInfo => {
      const tableNameSegments = tableInfo.name.split(/\./);
      return {
        label: tableNameSegments[tableNameSegments.length - 1],
        metadata: tableInfo,
      };
    });
  }

  doSearch(query: string, addToQueryHistory: boolean) {
    query = query.replace(/;\s*$/, '');

    this.view.isSearching = true;
    this.view.errorQueryingTables = false;

    this.searchService.observableSearch(
      this.isUsingPublicView() ? this.currentView.interfaceUri : this.currentView.resourcePath,
      {query: query},
      this.accessToken,
      this.connectorDetails,
      catchError(error => {
        const actualError: {errorName: string, message: string} = JSON.parse(error.error.message);
        console.error('CUSTOM ERROR HANDLER: actualError:', actualError);

        this.showFeedback(
          `${actualError.errorName.replace(/_/, ' ')}: ${actualError.message}`,
          {
            label: 'Try again',
            callback: () => {
              this.doSearch(this.currentQuery, false);
            },
          }
        );
        throw error;
      })
    ).subscribe(result => {
      this.completedQuery = query;
      this.result = result;
      this.searchService.updateTableData(result);
      this.view.isSearching = false;

      // if (addToQueryHistory) {
        this.queryHistory.unshift(query);
      // }

      this.properties = Object
        .keys(result.data_model ? result.data_model.properties : {})
        .filter(e => e !== 'description')
      ;
    });
  }

  viewResultsJSON() {
    this.jsonViewerService.viewJSON(this.result);
  }

  authorizeResource(views: SearchResourceModel[]) {
    const resourcesPath = [];

    views.map(view => {
      if (view.viewName === this.viewName) {
        this.currentView = view;
      }
      resourcesPath.push(this.searchService.buildResourcePath(view.damId, view));
    });

    // It is publicly accessible.
    if (this.currentView.interfaceUri) {
      this.initializeTableList();
      return;
    }

    // From this point, the access to the resource is controlled.
    this.resourceService.getAccessTokensForAuthorizedResources(resourcesPath).subscribe(data => {
      this.resourceAccessMap = this.resourceService.toResourceAccessMap(data);
      this.interfaceAccessTokensMap = this.interfaceAccessMap(data);
      Object.keys(this.resourceAccessMap).map(key => {
        if (key.indexOf(this.viewName) !== -1) {
          this.accessToken = this.resourceAccessMap[key].credentials.access_token;
        }
      });

      this.initializeTableList();
    });
  }

  // FIXME: Done for demo, remove this
  interfaceAccessMap(resourceTokens: IResourceResults): { [key: string]: IResourceAccess } {
    const accessMap = {};
    Object.entries(resourceTokens.resources)
      .forEach(([resource, value]) => {
        const items = Object.values(value.interfaces)[0].items;
        items.map(item => {
          accessMap[item.uri] = resourceTokens.access[value.access];
        });
      });
    return accessMap;
  }

  getTables(additionalAuthDetails?) {
    if (additionalAuthDetails && this.tableApiRequests < 3) {
      this.tableApiRequests++;
      this.connectorDetails['key'] = additionalAuthDetails['key'];
      this.connectorDetails['token'] = this.interfaceAccessTokensMap[additionalAuthDetails['resource-description']['interface-uri']]
        .credentials.access_token;
    }
    let observableTables: Observable<any>;

    if (this.isUsingPublicView()) {
      observableTables = this.searchService.getPublicTables(this.currentView.interfaceUri);
    } else {
      observableTables = this.searchService.getTables(this.currentView.resourcePath, this.accessToken, this.connectorDetails);
    }

    observableTables.subscribe(
      ({tables}) => {
        this.tableInfoList = tables;
        this.buildUiTableInfoList();
      },
      ({error}) => {
        if (error && error.message) {
          const errorDetails = JSON.parse(error.message);
          if (errorDetails.hasOwnProperty('authorization-request')) {
            const authRequestDetails = errorDetails['authorization-request'];
            this.getTables(authRequestDetails);
          }
        }
      }
    );
  }

  isUsingPublicView() {
    return this.accessToken === undefined || this.accessToken === null;
  }

  private showFeedback(message, action?: FeedbackAction) {
    if (!action) {
      action = {
        label: 'Dismiss',
        callback: () => {
          this.snackBarRef.dismiss();
          this.snackBarRef = null;
        },
      };
    }

    if (this.snackBarRef) {
      this.snackBarRef.dismiss();
    }

    this.snackBarRef = this.snackBar.open(message, action.label, {panelClass: 'ddap-error'});
    this.snackBarRef.onAction().subscribe();
  }

  private initializeTableList() {
    this.tableApiRequests = 0;
    this.getTables();

    this.appConfigService.get().subscribe(config => {
      this.currentQuery = config.search.defaultQuery;

      if (this.currentQuery && this.currentQuery.length > 0) {
        this.doSearch(this.currentQuery, false);
      }
    });
  }
}

interface FeedbackAction {
  label: string;
  callback: Function;
}

interface UITableInfo {
  label: string;
  metadata: TableInfo;
}
