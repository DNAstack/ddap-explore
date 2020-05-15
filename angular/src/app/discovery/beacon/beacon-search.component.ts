import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatDrawer } from '@angular/material/sidenav';
import { ActivatedRoute, Router } from '@angular/router';
import { LoadingBarService } from '@ngx-loading-bar/core';
import { ErrorHandlerService } from 'ddap-common-lib';
import { BehaviorSubject, EMPTY, Observable } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { AppConfigStore } from '../../shared/app-config/app-config.store';
import { AppDiscoveryService } from '../../shared/apps/app-discovery/app-discovery.service';
import { BeaconQueryAlleleRequestModel, BeaconQueryResponseModel } from '../../shared/beacon/beacon-search.model';
import { DataTableEventsService } from '../../shared/data-table/data-table-events.service';

import { BeaconInfoFormModel } from './beacon-info-bar/beacon-info-form.model';
import { HelpDialogComponent } from './help-dialog/help-dialog.component';

@Component({
  selector: 'ddap-beacon-search',
  templateUrl: './beacon-search.component.html',
  styleUrls: ['./beacon-search.component.scss'],
  providers: [DataTableEventsService],
})
export class BeaconSearchComponent implements OnInit {

  @ViewChild('selectedRowDetailDrawer', { static: false })
  selectedRowDetailDrawer: MatDrawer;

  beaconForm: BeaconInfoFormModel;
  beaconQuery: BeaconQueryAlleleRequestModel;
  beaconQueryResponse$: Observable<BeaconQueryResponseModel>;
  isStandaloneMode$: Observable<boolean>;
  selectedRowData: any;

  private readonly refreshBeaconResult$ = new BehaviorSubject<BeaconQueryAlleleRequestModel>(undefined);

  constructor(
    public loader: LoadingBarService,
    private route: ActivatedRoute,
    private router: Router,
    private appConfigStore: AppConfigStore,
    private appDiscoveryService: AppDiscoveryService,
    private errorHandlerService: ErrorHandlerService,
    private dataTableEventsService: DataTableEventsService,
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
    if (this.beaconForm.beacon.error) {
      // TODO: clean table
    }
    if (!this.refreshBeaconResult$.getValue() && this.beaconForm && this.beaconQuery) {
      this.submitQuery();
    }
  }

  submitQuery(): void {
    this.refreshBeaconResult$.next({ ...this.beaconQuery, datasetIds: this.beaconForm.datasets });
    this.resetDrawer();
  }

  closeDetail(): void {
    this.selectedRowDetailDrawer.close();
    this.dataTableEventsService.deselectRows();
  }

  buildUrlForResourceAuthorization(authorizationUrlBase: string): string {
    // TODO: add redirect with preselected beacon which was requested for authorization
    //       OR save form state before clicking Authorize and always load saved state
    return `${authorizationUrlBase}`
      + `&redirect_uri=${encodeURIComponent(this.router.url)}`;
  }

  private setUpBeaconQueryObservable() {
    this.beaconQueryResponse$ = this.refreshBeaconResult$.pipe(
      filter((params: BeaconQueryAlleleRequestModel) => {
        return params !== undefined && this.beaconForm !== undefined;
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

  private resetDrawer() {
    if (this.selectedRowDetailDrawer && this.selectedRowDetailDrawer.opened) {
      this.selectedRowDetailDrawer.toggle();
    }
  }

}
