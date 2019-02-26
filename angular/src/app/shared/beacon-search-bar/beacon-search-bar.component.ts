import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { assemblyIds } from '../assembly.model';
import { SearchState } from '../search-state.model';
import { SearchStateService } from '../search-state.service';

import { ValidateVariant } from './variant.validator';

@Component({
  selector: 'ddap-beacon-search-bar',
  templateUrl: './beacon-search-bar.component.html',
  styleUrls: ['./beacon-search-bar.component.scss'],
})
export class BeaconSearchBarComponent implements OnInit {

  @Input()
  placeholder: string;

  @Input()
  disabled: boolean;

  assemblyIds = assemblyIds;
  limitSearch = false;
  search: FormGroup;

  private resource;

  constructor(private router: Router,
              private searchStateService: SearchStateService) {

    this.search = new FormGroup({
      assembly: new FormControl(this.assemblyIds[0], [Validators.required]),
      query: new FormControl('', [Validators.required, ValidateVariant]),
    });
  }

  onSubmit({value, valid}: { value: any, valid: boolean }) {
    const currentRoute = this.router.url;
    if (currentRoute.startsWith('/data')) {
      this.searchStateService.patch({
        backLink: currentRoute,
      });
    }

    const resource = this.resource;
    this.router.navigate(['/data/search'], {
      queryParams: {
        ...value,
        resource,
        limitSearch: this.limitSearch,
      },
    });
  }

  ngOnInit(): void {
    this.searchStateService.searchState.subscribe((state: SearchState) => {
      const {assembly, query, resource, limitSearch} = state;
      this.resource = resource;
      this.limitSearch = limitSearch;

      if (assembly) {
        this.search.patchValue({assembly});
      }

      if (limitSearch) {
        this.search.patchValue({limitSearch});
      }

      if (query) {
        this.search.patchValue({query});
      }
    });
  }
}
