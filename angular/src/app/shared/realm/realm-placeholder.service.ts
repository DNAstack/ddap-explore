import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { defaultRealm, realmIdPlaceholder } from 'ddap-common-lib';
import _get from 'lodash.get';

@Injectable({
  providedIn: 'root',
})
export class RealmPlaceholderService {

  constructor(private activatedRoute: ActivatedRoute) {
  }

  get(): string {
    const realm = _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId');
    return realm ? realmIdPlaceholder : defaultRealm;
  }

}
