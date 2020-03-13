import { Component, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { AgGridModule } from 'ag-grid-angular';

import { tap, map, switchMap } from 'rxjs/operators';
import { of, from } from 'rxjs';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { BeaconRequest, BeaconResponse } from '../beacon-service/beacon.model';
import { BeaconService } from '../beacon-service/beacon.service';
import { DiscoveryConfigService } from '../discovery-config.service';
import { MapsAPILoader, Geocoder } from '@agm/core';
import { Observable } from 'rxjs';
import { SimpleLocation } from './location.model';
import { threadId } from 'worker_threads';

@Component({
  selector: 'ddap-discovery-beacon',
  templateUrl: './discovery-beacon.component.html',
  styleUrls: ['./discovery-beacon.component.scss'],
})
export class DiscoveryBeaconComponent implements OnInit {
  appConfig: AppConfigModel;
  assemblies: string[];
  assembly: string;

  query: BeaconRequest;
  lastQuery: BeaconRequest;

  beaconResponses: BeaconResponse[];
  cases: any[];
  caseColumnDefs: any;
  selectedCase: any;
  selectedCaseLocation: SimpleLocation;
  sample: any;

  geocoder: Geocoder;

  zoom = 4;

  view: {
    isSearching: boolean,
    errorSearching: boolean,
    wrapTableContent: boolean,
    showQuery: boolean,
    isGeocoding: boolean,
    errorGeocoding: boolean,
    isLocation: boolean
  };

  grid: any;

  private gridApi;
  private gridColumnApi;

  private queryParameters: any;

  constructor(private router: Router,
              private appConfigService: AppConfigService,
              private configService: DiscoveryConfigService,
              private beaconService: BeaconService,
              private route: ActivatedRoute,
              private mapLoader: MapsAPILoader
              ) {

                this.beaconService.setApiUrl(this.configService.getBeaconApiUrl());

                this.cases = [];

                this.onSelectionChanged = this.onSelectionChanged.bind(this);
                this.navigateToCell = this.navigateToCell.bind(this);

                this.selectedCaseLocation = { lat: 0, lng: 0}

                this.grid = {
                  animateRows: false,
                  multiSortKey: 'ctrl',
                  defaultColumnDefinition: {
                    sortable: true,
                    resizable: true,
                    filter: true,
                  },
                  makeFullWidth: false,
                  pagination: false,
                  domLayout: 'normal',
                  enableStatusBar: true,
                  suppressCellSelection: true,
                  rowSelection: 'single',
                };

                this.beaconResponses = [];

                this.view = {
                  isSearching : false,
                  errorSearching : false,
                  wrapTableContent : false,
                  showQuery: true,
                  isGeocoding: false,
                  errorGeocoding: false,
                  isLocation: true
                };
  }

  ngOnInit(): void {
    // Ensure that the user can only access this component when it is enabled.
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      this.appConfig = data;
      if (this.appConfig.featureDiscoveryEnabled) {
        this.initialize();
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  geocodeAddress(location: string): Observable<SimpleLocation> {
    console.log('Geocoding ' + location);
    
    return <Observable<SimpleLocation>> this.waitForMapsToLoad().pipe(
      switchMap(() => {
        return new Observable(observer => {
          this.geocoder.geocode({'address': location}, (results, status) => {
            if (status == google.maps.GeocoderStatus.OK) {
              console.log('Geocoding complete!');
              observer.next({
                lat: results[0].geometry.location.lat(), 
                lng: results[0].geometry.location.lng()
              });
            } else {
                console.log('Error - ', results, ' & Status - ', status);
                observer.next(null);
            }
            observer.complete();
          });
        })        
      })
    )
  }

  private initGeocoder() {
    console.log('Init geocoder!');
    this.geocoder = new google.maps.Geocoder();
  }

  private waitForMapsToLoad(): Observable<boolean> {
    if(!this.geocoder) {
      return from(this.mapLoader.load())
      .pipe(
        tap(() => this.initGeocoder()),
        map(() => true)
      );
    }
    return of(true);
  }

  doSearch() {

    const that = this;
    const query = this.query;

    this.beaconService.searchBeacon(
        'hCoV-19',
        '1',
        this.query.start,
        this.query.referenceBases,
        this.query.alternateBases
      ).then(
      data => {

        that.lastQuery = JSON.parse(JSON.stringify(query));

        that.selectedCase = undefined;

        const beaconId = data['beaconId'] as string;
        const request = data['alleleRequest'] as BeaconRequest;
        const responses = data['datasetAlleleResponses'] as BeaconResponse[];

        const response = responses[0];
        const info = response.info;

        const cases = [];
        const caseColumnKeys = [];

        for (let i = 0; i < info.length; i++) {

          const key = info[i].key;
          const keyTokens = key.split('=');
          const keyType = keyTokens[0];

          const value = info[i].value;
          const valueTokens = value.split(':');

          const valueDict = {};

          for (let j = 0; j < valueTokens.length; j++) {

            const valueToken = valueTokens[j];

            const valueTokenTokens = valueToken.split('=');
            const valueTokenKey = valueTokenTokens[0];
            const valueTokenValue = valueTokenTokens[1];

            valueDict[valueTokenKey] = valueTokenValue;

            if (!caseColumnKeys.includes(valueTokenKey)) {
              caseColumnKeys.push(valueTokenKey);
            }
          }

          if (keyType === 'case') {
            cases.push(valueDict);
          }
        }

        const caseColumnDefs = [];
        for (let k = 0; k < caseColumnKeys.length; k++) {
          const keyStr = caseColumnKeys[k];
          caseColumnDefs.push(
            {
              field : keyStr,
              headerName : this.titleCase(keyStr.replace(/_/g, ' ')),
            }
          );
        }

        that.beaconResponses = data;
        that.cases = cases;
        that.caseColumnDefs = caseColumnDefs;
        that.view.isSearching = false;
      },
      error => {
        that.view.errorSearching = true;
        that.view.isSearching = false;
      }
    );
  }

 rowDataChanged(event) {

  if (!this.gridApi) {
    return;
  }

  // Set column visibility
  this.gridColumnApi.setColumnsVisible(['start', 'ref', 'alt', 'type', 'vep', 'nuc_completeness'], false);

  // Resize columns
  const allColumnIds = [];
  this.gridColumnApi.getAllColumns().forEach(function(column) {
    allColumnIds.push(column.colId);
  });
  this.gridColumnApi.autoSizeColumns(allColumnIds);


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

  onSelectionChanged() {
    this.selectedCase = this.gridApi.getSelectedRows()[0];

    const that = this;

    if (this.selectedCase['Location']) {
      that.view.isGeocoding = true;
      that.view.errorGeocoding = false;
      that.view.isLocation = true;
      this.geocodeAddress(this.selectedCase['Location']).subscribe({
        next(location) {
          console.log(location);
          that.selectedCaseLocation = location;
        },
        error(msg) {
          that.view.errorGeocoding = true;
          that.selectedCaseLocation = { lat: 0, lng: 0 };
          console.log('Error getting Location: ', msg);
        }
      });
    } else {
      that.view.isLocation = false;
      this.selectedCaseLocation = { lat: 0, lng: 0 };
    }
  }

  navigateToCell(params) {
    let previousCell = params.previousCellPosition;
    const suggestedNextCell = params.nextCellPosition;

    const KEY_UP = 38;
    const KEY_DOWN = 40;
    const KEY_LEFT = 37;
    const KEY_RIGHT = 39;

    switch (params.key) {
        case KEY_DOWN:
            previousCell = params.previousCellPosition;
            // set selected cell on current cell + 1
            this.gridApi.forEachNode(function(node) {
                if (previousCell.rowIndex + 1 === node.rowIndex) {
                    node.setSelected(true);
                }
            });
            return suggestedNextCell;
        case KEY_UP:
            previousCell = params.previousCellPosition;
            // set selected cell on current cell - 1
            this.gridApi.forEachNode(function(node) {
                if (previousCell.rowIndex - 1 === node.rowIndex) {
                    node.setSelected(true);
                }
            });
            return suggestedNextCell;
        case KEY_LEFT:
        case KEY_RIGHT:
            return suggestedNextCell;
        default:
            throw new Error('this will never happen, navigation is always one of the 4 keys above');
    }
 }

  private titleCase(str) {
    const splitStr = str.toLowerCase().split(' ');
    for (let i = 0; i < splitStr.length; i++) {
        // You do not need to check if i is larger than splitStr length, as your for does that for you
        // Assign it back to the array
        splitStr[i] = splitStr[i].charAt(0).toUpperCase() + splitStr[i].substring(1);
    }
    // Directly return the joined string
    return splitStr.join(' ');
 }

  private initialize() {
    this.query = new BeaconRequest();
    this.query.start = 3840;
    this.query.referenceBases = 'A';
    this.query.alternateBases = 'G';
  }

  private setQueryParameters() {
    this.router.navigate(
      [],
      {
        relativeTo: this.route,
        queryParams: {
          start : this.query.start,
          referenceBases: this.query.referenceBases,
          alternateBases: this.query.alternateBases,
        },
        queryParamsHandling: 'merge', // remove to replace all query params by provided
      });
  }
}
