import { Component, Input, OnInit } from '@angular/core';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;
import _startcase from 'lodash.startcase';

import { dam } from '../../../../shared/proto/dam-service';

import { CollectionResourceCredentialsEntryModel } from './collection-resource-credentials-entry.model';

@Component({
  selector: 'ddap-collection-resource-credentials',
  templateUrl: './collection-resource-credentials.component.html',
  styleUrls: ['./collection-resource-credentials.component.scss'],
})
export class CollectionResourceCredentialsComponent implements OnInit {

  @Input()
  access: IResourceAccess;

  credentialEntries: CollectionResourceCredentialsEntryModel[] = [];

  ngOnInit(): void {
    if (!this.access.credentials) {
      return;
    }

    this.credentialEntries = this.mapResourceAccessToCredentials();
  }

  private mapResourceAccessToCredentials(): CollectionResourceCredentialsEntryModel[] {
    // format keys with _ to Start Case
    const toStartCase = (val: string): string => /^\w*$/.test(val) ? _startcase(val) : val;

    return Object.entries(this.access.credentials)
      .filter(([_, value]) => value.length > 0)
      .map(([key, value]) => {
        return {
          label: toStartCase(key),
          value,
          key,
        };
      });
  }

}
