import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { debounceTime, tap } from 'rxjs/operators';

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
export class BeaconInfoBarComponent implements OnInit, OnChanges, OnDestroy {

  @Input()
  resourceId?: string;

  @Input()
  hideInputs: boolean;

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
    this.initialize();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.resourceId) {
      this.initialize();
    }
  }

  ngOnDestroy(): void {
    if (this.formValueChangesSubscription) {
      this.formValueChangesSubscription.unsubscribe();
    }
  }

  private initialize() {
    if (this.resourceId) {
      // TODO This section will go away if we decide NOT to have Discovery Beacon as part of Workspace.
      this.discoveryBeaconService.getBeaconInfoResourcePairByResourceId(this.resourceId)
        .subscribe((beaconInfoResourcePair: BeaconInfoResourcePair) => {
          this.beacons = [beaconInfoResourcePair];
          this.initForm();
          this.beaconChanged.emit(this.form.value);
        });
    } else {
        // TODO This section will go away if we decide to have Discovery Beacon as part of Workspace.
        this.discoveryBeaconService.getBeaconInfoResourcePairs()
          .subscribe((beaconInfoResourcePairs: BeaconInfoResourcePair[]) => {
            this.beacons = beaconInfoResourcePairs;
            this.initForm();
            this.beaconChanged.emit(this.form.value);
        });
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
