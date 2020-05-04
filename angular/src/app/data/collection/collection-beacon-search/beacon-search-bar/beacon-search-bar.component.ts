import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Assembly } from '../../../../shared/beacon/beacon-search.model';
import { BeaconSearchRequestModel } from '../beacon-search.model';

import { BeaconSearchFormBuilder } from './beacon-search-form-builder.service';

@Component({
  selector: 'ddap-beacon-search-bar',
  templateUrl: './beacon-search-bar.component.html',
  styleUrls: ['./beacon-search-bar.component.scss'],
})
export class BeaconSearchBarComponent implements OnInit {

  @Input()
  limitSearch = false;

  assemblies = Object.values(Assembly);
  placeholder = 'Type query in form "1 : 156105028 T > C"';
  form: FormGroup;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private beaconSearchFormBuilder: BeaconSearchFormBuilder
  ) {
    this.form = this.beaconSearchFormBuilder.buildForm();
  }

  ngOnInit(): void {
    this.activatedRoute.queryParams
      .subscribe(({ assembly, query, limitSearch }) => {
        if (assembly) {
          this.form.patchValue({ assembly });
        }
        if (limitSearch) {
          this.form.patchValue({ limitSearch });
        }
        if (query) {
          this.form.patchValue({ query });
        }
      });
  }

  submitQuery() {
    const { collection, damId } = this.activatedRoute.snapshot.params;
    const realmId = this.activatedRoute.root.firstChild.snapshot.params.realmId;

    const searchParams: BeaconSearchRequestModel = {
      ...this.form.value,
      limitSearch: this.limitSearch,
    };

    if (collection) {
      searchParams.collection = collection;
    }
    if (damId) {
      searchParams.damId = damId;
    }

    this.activatedRoute.queryParams
      .subscribe((params) => {
        const { limitSearch } = params;
        if (!limitSearch) {
          return;
        }
        if (!collection && params.resource) {
          searchParams.collection = params.resource;
        }
        if (!damId && params.damId) {
          searchParams.damId = params.damId;
        }
      });

    this.router.navigate([realmId, 'data', 'collections', 'search'], {
      replaceUrl: false,
      queryParams: { ...searchParams },
    });
  }

}
