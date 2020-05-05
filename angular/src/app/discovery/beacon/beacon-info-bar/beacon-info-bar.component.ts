import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ErrorHandlerService } from 'ddap-common-lib';
import { EMPTY, Subscription } from 'rxjs';
import { catchError, debounceTime, tap } from 'rxjs/operators';

import { BeaconDatasetModel, BeaconInfoResourcePair } from '../../../shared/apps/app-discovery/app-discovery.model';
import { ResourceService } from '../../../shared/apps/resource.service';
import { DiscoveryBeaconService } from '../../discovery-beacon.service';

import { BeaconInfoFormBuilder } from './beacon-info-form-builder.service';
import { BeaconInfoFormModel } from './beacon-info-form.model';

@Component({
  selector: 'ddap-beacon-info-bar',
  templateUrl: './beacon-info-bar.component.html',
  styleUrls: ['./beacon-info-bar.component.scss'],
})
export class BeaconInfoBarComponent implements OnInit, OnDestroy {

  @Output()
  readonly beaconChanged: EventEmitter<BeaconInfoFormModel> = new EventEmitter();

  beacons: BeaconInfoResourcePair[];
  form: FormGroup;
  formValueChangesSubscription: Subscription;

  constructor(
    private beaconInfoFormBuilder: BeaconInfoFormBuilder,
    private resourceService: ResourceService,
    private discoveryBeaconService: DiscoveryBeaconService
  ) {
  }

  get selectedBeaconDatasets(): BeaconDatasetModel[] {
    const selectedBeacon: BeaconInfoResourcePair = this.form.get('beacon').value;
    return selectedBeacon.beaconInfo.datasets;
  }

  ngOnInit(): void {
    this.discoveryBeaconService.getBeaconInfoResourcePairs()
      .subscribe((beaconInfoResourcePairs: BeaconInfoResourcePair[]) => {
        this.beacons = beaconInfoResourcePairs;
        this.initForm();
        this.beaconChanged.emit(this.form.value);
      });
  }

  ngOnDestroy(): void {
    if (this.formValueChangesSubscription) {
      this.formValueChangesSubscription.unsubscribe();
    }
  }

  private initForm(): void {
    this.form = this.beaconInfoFormBuilder.buildForm(this.beacons);
    this.formValueChangesSubscription = this.form.valueChanges
      .pipe(
        debounceTime(300),
        tap(() => {
          this.beaconChanged.emit(this.form.value);
        })
      )
      .subscribe();
  }

}