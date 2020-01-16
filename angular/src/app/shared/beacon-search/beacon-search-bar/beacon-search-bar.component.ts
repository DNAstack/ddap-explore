import { Component, Input, OnDestroy, OnInit } from "@angular/core";
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EntityModel } from 'ddap-common-lib';

import { assemblyIds } from '../assembly.model';
import { BeaconSearchParams } from '../beacon-search-params.model';

import { VariantValidators } from './variant.validator';
import { Subscription } from "rxjs";

@Component({
  selector: 'ddap-beacon-search-bar',
  templateUrl: './beacon-search-bar.component.html',
  styleUrls: ['./beacon-search-bar.component.scss'],
})
export class BeaconSearchBarComponent implements OnInit, OnDestroy {

  @Input()
  placeholder = '1 : 156105028 T > C';
  @Input()
  disabled: boolean;
  @Input()
  limitSearch = false;
  @Input()
  replaceUrl = false;

  assemblyIds = assemblyIds;
  searchForm: FormGroup;

  private queryParamsSubscription: Subscription;

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute) {

    this.searchForm = new FormGroup({
      assembly: new FormControl(this.assemblyIds[0], [Validators.required]),
      query: new FormControl('', [Validators.required, VariantValidators.variant]),
    });
  }

  onSubmit({value}) {
    const { resourceName, damId } = this.activatedRoute.snapshot.params;
    const realmId = this.activatedRoute.root.firstChild.snapshot.params.realmId;

    const searchParams: BeaconSearchParams = {
      ...value,
      limitSearch: this.limitSearch,
    };

    if (resourceName) {
      searchParams.resource = resourceName;
    }
    if (damId) {
      searchParams.damId = damId;
    }

    this.queryParamsSubscription = this.activatedRoute.queryParams
      .subscribe((params) => {
        const { limitSearch } = params;
        if (!limitSearch) {
          return;
        }
        if (!resourceName && params.resource) {
          searchParams.resource = params.resource;
        }
        if (!damId && params.damId) {
          searchParams.damId = params.damId;
        }
      });

    this.router.navigate([realmId, 'data', 'search'],  {
      replaceUrl: this.replaceUrl,
      queryParams: { ...searchParams },
    });
  }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(({assembly, query, resource, limitSearch}) => {
      if (assembly) {
        this.searchForm.patchValue({assembly});
      }

      if (limitSearch) {
        this.searchForm.patchValue({limitSearch});
      }

      if (query) {
        this.searchForm.patchValue({query});
      }
    });
  }

  ngOnDestroy(): void {
    if (this.queryParamsSubscription) {
      this.queryParamsSubscription.unsubscribe();
    }
  }
}
