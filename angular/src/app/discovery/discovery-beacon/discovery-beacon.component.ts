import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { ILatLong, IMapOptions } from 'angular-maps';
import { ViewControllerService } from 'ddap-common-lib';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { DataTableComponent } from '../../shared/data-table/data-table.component';
import { BeaconService } from '../beacon-service/beacon.service';

import { DiscoveryBeaconDataTableController } from './discovery-beacon-data-table-controller';
import { GeocodeService } from './geocode/geocode.service';
import { DiscoveryBeaconHelpDialogComponent } from './help/discovery-beacon.help.dialog';

@Component({
  selector: 'ddap-discovery-beacon',
  templateUrl: './discovery-beacon.component.html',
  styleUrls: ['./discovery-beacon.component.scss'],
})
export class DiscoveryBeaconComponent implements OnInit {
  @ViewChild(DataTableComponent, {static: false})
  dataTable: DataTableComponent;

  appConfig: AppConfigModel;
  dataTableController: DiscoveryBeaconDataTableController;

  searchBoxActive = false;

  contentContainer: any; // an actual DOM element

  cases: any[];
  selectedCase: any;

  infoPanelActivated: boolean;

  queryForm: FormGroup;

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

  /** Bing map */
  mapOptions: IMapOptions = {
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
              private viewController: ViewControllerService,
              private route: ActivatedRoute,
              private geocodeService: GeocodeService,
              private changeDetector: ChangeDetectorRef,
              public helpDialog: MatDialog
  ) {
    this.dataTableController = new DiscoveryBeaconDataTableController(
      this.beaconService,
      (selectedRow) => this.onRowSelectionChanged(selectedRow),
      () => this.canSearch(),
      () => {
        this.dataTableController.setInflight(true);

        this.setQueryParameters();

        this.searchBoxActive = false;
        this.selectedCase = null;
        this.infoPanelActivated = false;

        this.queryForm.disable();
      },
      () => {
        this.dataTableController.setInflight(false);
        this.view.errorSearching = false;
      },
      () => {
        this.dataTableController.setInflight(false);
        this.view.errorSearching = true;
      }
    );

    this.cases = [];
    this.infoPanelActivated = false;
    this.queryForm = this.dataTableController.queryForm;

    this.setMapCenterCoordinate(0, 0);

    this.view = {
      isSearching: false,
      errorSearching: false,
      wrapTableContent: false,
      showQuery: true,
      isGeocoding: false,
      errorGeocoding: false,
      isLocation: true,
      isMobile: this.viewController.isMobile(),
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

  canSearch() {
    return this.beaconService.isReady() && !this.dataTableController.inflight && this.queryForm.valid;
  }

  setSearchBoxActive(active: boolean) {
    this.searchBoxActive = active;

    if (active) {
      this.infoPanelActivated = false;
    }
  }

  onRowSelectionChanged(selectedRow: Map<string, any>) {
    this.infoPanelActivated = true;
    this.selectedCase = selectedRow;

    const locationText = this.selectedCase['Location'];

    if (locationText) {
      this.view.errorGeocoding = false;
      this.view.isLocation = true;

      // Geocode
      this.view.isGeocoding = true;
      this.geocodeService.geocodeAddress(locationText)
        .subscribe((location: ILatLong) => {
            this.setMapCenterCoordinate(location.latitude, location.longitude);
            this.view.isGeocoding = false;
            this.changeDetector.detectChanges();
          }
        );

    } else {
      this.view.isLocation = false;
      this.setMapCenterCoordinate(0, 0);
    }
  }

  setMapCenterCoordinate(lat: number, lng: number) {
    this.mapOptions.center = {latitude: lat, longitude: lng};
  }

  onResize(event) {
    this.contentContainer = event.target;
    this.view.isMobile = this.viewController.isMobileWidth(this.contentContainer.innerWidth);

    if (this.dataTable) {
      this.dataTable.enablePagination(!this.view.isMobile);
    }
  }

  getQuerySnapshot(): QuerySnapshot {
    const snapshot = this.queryForm.value;
    return {
      start: parseInt(snapshot['start'], 10),
      referenceBases: snapshot['referenceBases'].toUpperCase(),
      alternateBases: snapshot['alternateBases'].toUpperCase(),
    };
  }

  isQueryReadyForSubmission(): boolean {
    return this.queryForm.enabled && this.queryForm.valid;
  }

  getTooltipMessageFor(fieldName: string, defaultMessage: string) {
    const errors = this.queryForm.controls[fieldName].errors;

    if (errors === null) {
      return defaultMessage;
    }

    if (errors['required']) {
      return defaultMessage;
    } else if (errors['pattern']) {
      return fieldName === 'start' ? 'Only accept numbers' : 'Must be a sequence of bases (e.g., TCAG)';
    }

    return defaultMessage; // handle unknown cases.
  }

  private initialize() {
    if (this.appConfig.covidBeaconUrl) {
      this.beaconService.setApiUrl(this.appConfig.covidBeaconUrl);
    }

    this.route.queryParams
      .subscribe(params => {
        const position = Number(params['position']);
        const reference = params['referenceBases'];
        const alternate = params['alternateBases'];

        let q: QuerySnapshot;

        if (position === 0 || !reference || !alternate) {
          q = {
            start: 3840,
            referenceBases: 'A',
            alternateBases: 'G',
          };
        } else {
          q = {
            start: position,
            referenceBases: reference,
            alternateBases: alternate,
          };
        }

        this.queryForm.patchValue(q);

        this.dataTableController.beginQuery();
      });
  }

  private setQueryParameters() {
    const query = this.dataTableController.getQuerySnapshot();

    this.router.navigate(
      [],
      {
        relativeTo: this.route,
        queryParams: {
          position: query.start,
          referenceBases: query.referenceBases,
          alternateBases: query.alternateBases,
        },
        queryParamsHandling: 'merge', // remove to replace all query params by provided
      });
  }
}
