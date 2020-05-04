import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { FormGroup } from '@angular/forms';
import _get from 'lodash.get';
import { Subscription } from 'rxjs';
import { debounceTime, tap } from 'rxjs/operators';

import { BeaconInfoResponseModel } from '../../../shared/apps/app-discovery/app-discovery.model';
import {
  Assembly,
  BeaconInterfaceType,
  BeaconQueryAlleleRequestModel,
} from '../../../shared/beacon/beacon-search.model';

import { BeaconSearchFormBuilder } from './beacon-search-form-builder.service';

@Component({
  selector: 'ddap-beacon-search-bar',
  templateUrl: './beacon-search-bar.component.html',
  styleUrls: ['./beacon-search-bar.component.scss'],
})
export class BeaconSearchBarComponent implements OnChanges, OnDestroy {

  @Input()
  interfaceType: BeaconInterfaceType;
  @Input()
  beaconInfo: BeaconInfoResponseModel;

  @Output()
  readonly queryChanged: EventEmitter<BeaconQueryAlleleRequestModel> = new EventEmitter();

  assemblies = Object.values(Assembly);
  form: FormGroup;
  formValueChangesSubscription: Subscription;

  constructor(private beaconSearchFormBuilder: BeaconSearchFormBuilder) {
  }

  get isVirusInterface(): boolean {
    return this.interfaceType === BeaconInterfaceType.virus;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.form && this.formValueChangesSubscription) {
      this.formValueChangesSubscription.unsubscribe();
    }

    const sampleRequest: BeaconQueryAlleleRequestModel = _get(this.beaconInfo, 'sampleAlleleRequests[0]');
    this.initForm(sampleRequest);
    this.queryChanged.emit(this.form.value);
  }

  ngOnDestroy(): void {
    if (this.formValueChangesSubscription) {
      this.formValueChangesSubscription.unsubscribe();
    }
  }

  private initForm(sampleRequest: BeaconQueryAlleleRequestModel) {
    this.form = this.beaconSearchFormBuilder.buildForm(this.isVirusInterface, sampleRequest);
    this.formValueChangesSubscription = this.form.valueChanges
      .pipe(
        debounceTime(300),
        tap(() => this.queryChanged.emit(this.form.value))
      )
      .subscribe();
  }

}
