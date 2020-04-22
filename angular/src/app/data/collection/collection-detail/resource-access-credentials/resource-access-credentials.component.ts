import { Component, Input, OnInit } from '@angular/core';
import * as dayjs from 'dayjs';
import * as relativeTime from 'dayjs/plugin/relativeTime';
import _startcase from 'lodash.startcase';

import { AccessModel } from '../../../../shared/apps/app-explore.model';

import { ResourceAccessCredentialsEntryModel } from './resource-access-credentials-entry.model';

@Component({
  selector: 'ddap-resource-access-credentials',
  templateUrl: './resource-access-credentials.component.html',
  styleUrls: ['./resource-access-credentials.component.scss'],
})
export class ResourceAccessCredentialsComponent implements OnInit {

  @Input()
  access: AccessModel;

  credentialEntries: ResourceAccessCredentialsEntryModel[] = [];

  constructor() {
    dayjs.extend(relativeTime);
  }

  ngOnInit(): void {
    if (!this.access.credentials) {
      return;
    }
    this.credentialEntries = this.mapResourceAccessToCredentialEntries();
  }

  get isExpired(): boolean {
    const expirationTime = dayjs(this.access.expirationTime);
    return expirationTime.isBefore(dayjs());
  }

  getRelativeExpiresIn(): string {
    const expirationTime = dayjs(this.access.expirationTime);
    return `${this.isExpired ? 'Expired' : 'Expires'} ${expirationTime.fromNow()}`;
  }

  private mapResourceAccessToCredentialEntries(): ResourceAccessCredentialsEntryModel[] {
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
