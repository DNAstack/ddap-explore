import { Component, Input, OnInit } from '@angular/core';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;
import _startcase from 'lodash.startcase';

import { dam } from '../proto/dam-service';

@Component({
  selector: 'ddap-view-access',
  templateUrl: './view-access.component.html',
  styleUrls: ['./view-access.component.scss'],
})
export class ViewAccessComponent implements OnInit {

  @Input()
  access: IResourceAccess;
  @Input()
  url?: string;
  credentials: object[] = [];

  ngOnInit(): void {
    if (this.access.credentials) {
      Object.entries(this.access.credentials)
        .map(([name, credVal]) => {
          if (credVal.length > 0) {
            this.credentials.push({
              label: this.formatVal(name),
              value: credVal,
            });
          }
        });
    }
  }

  // format keys with _ to Start Case
  private formatVal(val: string) {
    return /^\w*$/.test(val) ? _startcase(val) : val;
  }

}
