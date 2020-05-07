import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { LoadingBarService } from '@ngx-loading-bar/core';
import { ErrorHandlerService } from 'ddap-common-lib';
import { BehaviorSubject, EMPTY, Observable } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';

import { AppConfigStore } from '../../shared/app-config/app-config.store';
import { AppDiscoveryService } from '../../shared/apps/app-discovery/app-discovery.service';
import { BeaconQueryAlleleRequestModel, BeaconQueryResponseModel } from '../../shared/beacon/beacon-search.model';

import { BeaconInfoFormModel } from './beacon-info-bar/beacon-info-form.model';
import { HelpDialogComponent } from './help-dialog/help-dialog.component';

@Component({
  selector: 'ddap-beacon-search',
  templateUrl: './beacon-search.component.html',
  styleUrls: ['./beacon-search.component.scss'],
})
export class BeaconSearchComponent implements OnInit {

  beaconForm: BeaconInfoFormModel;
  beaconQuery: BeaconQueryAlleleRequestModel;
  beaconQueryResponse$: Observable<BeaconQueryResponseModel>;
  isStandaloneMode$: Observable<boolean>;

  private readonly refreshBeaconResult$ = new BehaviorSubject<BeaconQueryAlleleRequestModel>(undefined);

  constructor(
    public loader: LoadingBarService,
    private appConfigStore: AppConfigStore,
    private appDiscoveryService: AppDiscoveryService,
    private errorHandlerService: ErrorHandlerService,
    private helpDialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    this.isStandaloneMode$ = this.appConfigStore.state$
      .pipe(
        map((appConfig) => {
          return appConfig.inStandaloneMode;
        })
      );

    this.setUpBeaconQueryObservable();
  }

  openHelpDialog(): void {
    this.helpDialog.open(HelpDialogComponent);
  }

  loadResultTable() {
    if (!this.refreshBeaconResult$.getValue() && this.beaconForm && this.beaconQuery) {
      this.submitQuery();
    }
  }

  submitQuery(): void {
    this.refreshBeaconResult$.next({ ...this.beaconQuery, datasetIds: this.beaconForm.datasets });
  }

  private setUpBeaconQueryObservable() {
    this.beaconQueryResponse$ = this.refreshBeaconResult$.pipe(
      filter((params: BeaconQueryAlleleRequestModel) => {
        return params && params.datasetIds && params.datasetIds.length > 0;
      }),
      switchMap((params: BeaconQueryAlleleRequestModel) => {
        const interfaceId = this.beaconForm.beacon.resource.interfaces[0].id;
        return this.appDiscoveryService.queryBeacon(interfaceId, params)
          .pipe(
            catchError((error) => {
              this.errorHandlerService.openSnackBarWithError(error, 'error.message');
              return EMPTY;
            })
          );
      })
    );
  }

}
