import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogConfig, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { ILatLong, IMapOptions, MapAPILoader, MarkerTypeId } from 'angular-maps';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { BeaconRequest, BeaconResponse } from '../beacon-service/beacon.model';
import { BeaconService } from '../beacon-service/beacon.service';
import { DiscoveryConfigService } from '../discovery-config.service';

import { GeocodeService } from './geocode/geocode.service';
import { DiscoveryBeaconHelpDialogComponent } from './help/discovery-beacon.help.dialog';

@Component({
  selector: 'ddap-discovery-beacon',
  templateUrl: './discovery-beacon.component.html',
  styleUrls: ['./discovery-beacon.component.scss'],
})
export class DiscoveryBeaconComponent implements OnInit {
  appConfig: AppConfigModel;
  // assemblies: string[]; // marked for removal
  // assembly: string; // marked for removal

  query: BeaconRequest;
  lastQuery: BeaconRequest;

  searchBoxActive = false;

  beaconResponses: BeaconResponse[];
  cases: any[];
  caseColumnDefs: any;
  selectedCase: any;
  // sample: any; // marked for removal

  infoPanelActivated: boolean;

  view: {
    isSearching: boolean,
    errorSearching: boolean,
    wrapTableContent: boolean,
    showQuery: boolean,
    isGeocoding: boolean,
    errorGeocoding: boolean,
    isLocation: boolean
    isMobile: boolean
  };

  map: any;

  grid: any;

  private gridApi;
  private gridColumnApi;

  // private queryParameters: any;

  /** Bing map */
  // private _markerTypeId = MarkerTypeId;
  private _options: IMapOptions = {
    disableBirdseye: true,
    disableStreetside: true,
    showCopyright: false,
    showMapTypeSelector: false,
    navigationBarMode: 2,
    mapTypeId: 7,
    zoom: 4,
    center: {
      latitude: 0,
      longitude: 0,
    },
  };

  constructor(private router: Router,
              private appConfigService: AppConfigService,
              private beaconService: BeaconService,
              private route: ActivatedRoute,
              private geocodeService: GeocodeService,
              private changeDetector: ChangeDetectorRef,
              public helpDialog: MatDialog,
              private activatedRoute: ActivatedRoute
  ) {
    this.cases = [];

    const isMobile = this.isMobileWidth(window.innerWidth);

    this.infoPanelActivated = false;

    this.onSelectionChanged = this.onSelectionChanged.bind(this);
    this.navigateToCell = this.navigateToCell.bind(this);

    this._options.center = {latitude: 0, longitude: 0};

    this.grid = {
      animateRows: false,
      multiSortKey: 'ctrl',
      defaultColumnDefinition: {
        sortable: true,
        resizable: true,
        filter: true,
      },
      makeFullWidth: false,
      pagination: !isMobile,
      domLayout: 'normal',
      enableStatusBar: true,
      suppressCellSelection: true,
      rowSelection: 'single',
    };

    this.view = {
      isSearching: false,
      errorSearching: false,
      wrapTableContent: false,
      showQuery: true,
      isGeocoding: false,
      errorGeocoding: false,
      isLocation: true,
      isMobile : isMobile,
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

  openHelpDialog() {
    const dialogConfig = new MatDialogConfig();

    const dialogRef = this.helpDialog.open(
      DiscoveryBeaconHelpDialogComponent,
      {
        width: '500px',
      }
    );
  }

  doSearch() {
    const query = this.query;

    this.setQueryParameters();

    this.view.isSearching = true;
    this.searchBoxActive = false;
    this.selectedCase = null;
    this.infoPanelActivated = false;

    this.beaconService.searchBeacon(
      'hCoV-19',
      '1',
      this.query.start,
      this.query.referenceBases,
      this.query.alternateBases
    ).then(
      data => {
        this.lastQuery = JSON.parse(JSON.stringify(query));

        // const beaconId = data['beaconId'] as string;
        // const request = data['alleleRequest'] as BeaconRequest;
        const responses = data['datasetAlleleResponses'] as BeaconResponse[];
        const info = responses[0].info;

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
              field: keyStr,
              headerName: this.titleCase(keyStr.replace(/_/g, ' ')),
            }
          );
        }

        this.beaconResponses = data;
        this.cases = cases;
        this.caseColumnDefs = caseColumnDefs;
        this.view.isSearching = false;
      },
      error => {
        this.view.errorSearching = true;
        this.view.isSearching = false;
      }
    );
  }

  rowDataChanged(event) {
    if (!this.gridApi) {
      return;
    }
    this.hideAndResizeColumns();
  }

  hideAndResizeColumns() {

    // Resize columns
    const hiddenFieldIds = ['start', 'ref', 'alt', 'type', 'vep', 'nuc_completeness'];

    const allColumnIds = [];
    const hiddenColumnIds = [];

    this.gridColumnApi.getAllColumns().forEach(function (column) {
      allColumnIds.push(column.colId);
      if (hiddenFieldIds.includes(column.userProvidedColDef.field)) {
        hiddenColumnIds.push(column.colId);
      }
    });

    this.gridColumnApi.setColumnsVisible(hiddenColumnIds, false);
    this.gridColumnApi.autoSizeColumns(allColumnIds);
  }

  nextStrainUrl(source) {
    const tokens = source.split('/');
    if (tokens.length === 1) {
      return null;
    }
    return 'https://nextstrain.org/ncov?s=' + tokens[1] + '/' + tokens[2] + '/' + tokens[3];
  }

  setSearchBoxActive(active: boolean) {
    this.searchBoxActive = active;

    if (active) {
      this.infoPanelActivated = false;
    }
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

    this.hideAndResizeColumns();
  }

  onSelectionChanged(event) {
    this.selectedCase = this.gridApi.getSelectedRows()[0];
    this.infoPanelActivated = true;

    const that = this;

    const locationText = this.selectedCase['Location'];

    if (locationText) {
      that.view.isGeocoding = true;
      that.view.errorGeocoding = false;
      that.view.isLocation = true;

      // Geocode
      that.view.isGeocoding = true;
      this.geocodeService.geocodeAddress(locationText)
        .subscribe((location: ILatLong) => {
            that.setCaseLocation(location.latitude, location.longitude);
            this.view.isGeocoding = false;
            that.changeDetector.detectChanges();
          }
        );

    } else {
      that.view.isLocation = false;
      that.setCaseLocation(0, 0);
    }
  }

  setCaseLocation(lat: number, lng: number) {
    this._options.center = {latitude: lat, longitude: lng};
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
        this.gridApi.forEachNode(function (node) {
          if (previousCell.rowIndex + 1 === node.rowIndex) {
            node.setSelected(true);
          }
        });
        return suggestedNextCell;
      case KEY_UP:
        previousCell = params.previousCellPosition;
        // set selected cell on current cell - 1
        this.gridApi.forEachNode(function (node) {
          if (previousCell.rowIndex - 1 === node.rowIndex) {
            node.setSelected(true);
          }
        });
        return suggestedNextCell;
      case KEY_LEFT:
      case KEY_RIGHT:
        return suggestedNextCell;
      default:
        throw new Error('this will never happen');
    }
  }

  onResize(event) {
    this.view.isMobile = this.isMobileWidth(event.target.innerWidth);
    this.grid.pagination = !this.view.isMobile;
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
    if (this.appConfig.covidBeaconUrl) {
      this.beaconService.setApiUrl(this.appConfig.covidBeaconUrl);
    }


    const that = this;

    this.route.queryParams
      .subscribe(params => {
        const position = Number(params['position']);
        const reference = params['referenceBases'];
        const alternate = params['alternateBases'];

        const q = new BeaconRequest();

        if (position === 0 || !reference || !alternate) {
          q.start = 3840;
          q.referenceBases = 'A';
          q.alternateBases = 'G';
        } else {
          q.start = position;
          q.referenceBases = reference;
          q.alternateBases = alternate;
        }

        that.query = q;

        that.doSearch();
      });

  }

  private setQueryParameters() {
    this.router.navigate(
      [],
      {
        relativeTo: this.route,
        queryParams: {
          position : this.query.start,
          referenceBases: this.query.referenceBases,
          alternateBases: this.query.alternateBases,
        },
        queryParamsHandling: 'merge', // remove to replace all query params by provided
      });
  }

  private isMobileWidth(width: number) {
    return width < 760;
  }

}
