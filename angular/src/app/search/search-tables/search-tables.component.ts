import { KeyValue } from '@angular/common';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import 'brace';
import 'brace/mode/sql';
import 'brace/theme/eclipse';
import Table = WebAssembly.Table;
import { filter, flatMap, map, shareReplay } from 'rxjs/operators';

import { dam } from '../../shared/proto/dam-service';
import { ResourceService } from '../../shared/resource/resource.service';
import { SearchEditorComponent } from '../search-editor/search-editor.component';
import IResourceResults = dam.v1.IResourceResults;
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;
import { SearchResourceModel } from '../search-resources/search-resource.model';
import { SearchService } from '../search.service';

import { JsonViewerService } from './json-viewer/json-viewer.component';
import { BeaconQuery, BeaconRegistry, SearchView } from './search-tables.model';

@Component({
  selector: 'ddap-search-detail',
  templateUrl: './search-tables.component.html',
  styleUrls: ['./search-tables.component.scss'],
})
export class SearchTablesComponent implements OnInit {
  @ViewChild(SearchEditorComponent, {static: false}) searchEditor: SearchEditorComponent;
  searchTables: object[] = [];
  registry: BeaconRegistry;

  view: SearchView;

  options: {
    wrapBehavioursEnabled: true
  };

  search: {
    text: string,
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

  private query: string;
  private properties: string[];


  constructor( private searchService: SearchService,
               private route: ActivatedRoute,
               private jsonViewerService: JsonViewerService,
               private router: Router,
               private resourceService: ResourceService) {

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

  refreshBeacons() {
    // this.setRegistry(this.registry);
  }

  closeTables() {

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

  viewTableAsJSON(table: Table) {
    this.jsonViewerService.viewJSON(table);
  }

//  EDITOR STUFF


  doSearch(query: string) {

    // if (query.length === 0) {
    //   this.snackBar.open('Empty query', 'Dismiss', {
    //     panelClass: 'error-snack',
    //   });
    //   return;
    // }

    this.view.isSearching = true;
    this.view.errorQueryingTables = false;
    this.searchService.search(this.currentView.resourcePath,
      { 'query' : query }, this.accessToken, this.connectorDetails).subscribe(result => {
      this.query = query;
      this.result = result;
      this.searchService.updateTableData(result);
      this.view.isSearching = false;
      this.queryHistory.unshift(query);
      const schema = result['data_model'] ? result['data_model']['properties'] : {};
      const properties = Object.keys(schema);
      this.properties = properties.filter(e => e !== 'description');
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
      resourcesPath.push(
        `${view.damId};${view.resourceName}/views/` +
        `${view.viewName}/roles/${view.roleName}/interfaces/${view.interfaceName}`
      );
    });
    this.resourceService.getAccessTokensForAuthorizedResources(resourcesPath).subscribe(data => {
      this.resourceAccessMap = this.resourceService.toResourceAccessMap(data);
      this.interfaceAccessTokensMap = this.interfaceAccessMap(data);
      Object.keys(this.resourceAccessMap).map(key => {
        if (key.indexOf(this.viewName) !== -1) {
          this.accessToken = this.resourceAccessMap[key].credentials.access_token;
        }
      });

      this.tableApiRequests = 0;
      this.getTables();
    });
  }

  // FIXME: Done for demo, remove this
  interfaceAccessMap(resourceTokens: IResourceResults): {[key: string]: IResourceAccess} {
    const accessMap = {};
    Object.entries(resourceTokens.resources)
      .forEach(([resource, value]) => {
        const items = Object.values(value.interfaces)[0].items;
        items.map(item => {
          accessMap[item.uri] = resourceTokens.access[value.access];
        });
        // accessMap[resource] = resourceTokens.access[value.access];
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
    this.searchService.getTables(
      this.currentView.resourcePath,
      this.accessToken,
      this.connectorDetails
    ).subscribe(
      ({tables}) => {
        this.searchTables = tables;
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
}
