import { Component, Input } from '@angular/core';

import { dam } from '../proto/dam-service';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;

@Component({
  selector: 'ddap-view-access',
  templateUrl: './view-access.component.html',
  styleUrls: ['./view-access.component.scss'],
})
export class ViewAccessComponent {

  @Input()
  access: IResourceAccess;
  @Input()
  url?: string;

}
