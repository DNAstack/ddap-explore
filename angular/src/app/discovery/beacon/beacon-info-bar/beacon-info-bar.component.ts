import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { debounceTime, tap } from 'rxjs/operators';

import { BeaconDatasetModel, BeaconInfoResourcePair } from '../../../shared/apps/app-discovery/app-discovery.model';
import { ResourceService } from '../../../shared/apps/resource.service';
import { DiscoveryBeaconService } from '../../discovery-beacon.service';
import { BeaconQueryStateService } from '../beacon-query-state.service';

import { BeaconInfoFormBuilder } from './beacon-info-form-builder.service';
import { BeaconInfoFormModel } from './beacon-info-form.model';

@Component({
  selector: 'ddap-beacon-info-bar',
  templateUrl: './beacon-info-bar.component.html',
  styleUrls: ['./beacon-info-bar.component.scss'],
})
export class BeaconInfoBarComponent implements OnInit, OnDestroy {

  @Input()
  hideInputs: boolean;

  @Output()
  readonly beaconChanged: EventEmitter<BeaconInfoFormModel> = new EventEmitter();

  beacons: BeaconInfoResourcePair[];
  form: FormGroup;
  formValueChangesSubscriptions: Subscription[] = [];

  constructor(
    private beaconInfoFormBuilder: BeaconInfoFormBuilder,
    private resourceService: ResourceService,
    private discoveryBeaconService: DiscoveryBeaconService,
    private beaconQueryStateService: BeaconQueryStateService
  ) {
  }

  get selectedBeaconDatasets(): BeaconDatasetModel[] {
    const selectedBeacon: BeaconInfoResourcePair = this.form.get('beacon').value;
    return selectedBeacon.beaconInfo ? selectedBeacon.beaconInfo.datasets : [];
  }

  get selectedBeaconError(): any {
    const selectedBeacon: BeaconInfoResourcePair = this.form.get('beacon').value;
    return selectedBeacon && selectedBeacon.error ? selectedBeacon.error : undefined;
  }

  ngOnInit(): void {
    this.discoveryBeaconService.getBeaconInfoResourcePairs()
      .subscribe((beaconInfoResourcePairs: BeaconInfoResourcePair[]) => {
        this.beacons = beaconInfoResourcePairs;
        this.initForm(this.beaconQueryStateService.getValueFromQuery('beaconId'));
        this.beaconChanged.emit(this.form.value);
      });
  }

  ngOnDestroy(): void {
    this.formValueChangesSubscriptions.forEach((subscription) => subscription.unsubscribe());
  }

  private initForm(beaconId: string = ''): void {
    this.form = this.beaconInfoFormBuilder.buildForm(beaconId, this.beacons);
    this.selectAllDatasetsOnBeaconValueChange();
    this.formValueChangesSubscriptions.push(this.form.valueChanges
      .pipe(
        debounceTime(300),
        tap(() => {
          this.beaconChanged.emit(this.form.value);
        })
      )
      .subscribe());
  }

  private selectAllDatasetsOnBeaconValueChange() {
    this.formValueChangesSubscriptions.push(this.form.get('beacon').valueChanges
      .pipe(
        tap(() => this.selectAllDatasets())
      )
      .subscribe());
  }

  private selectAllDatasets(): void {
    const datasetsIds: string[] = this.selectedBeaconDatasets.map((dataset) => dataset.id);
    this.form.get('datasets').setValue(datasetsIds);
  }

}
