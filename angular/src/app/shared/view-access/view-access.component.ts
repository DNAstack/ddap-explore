import { Component, Input } from '@angular/core';

import { dam } from '../proto/dam-service';
import ResourceToken = dam.v1.ResourceTokens.ResourceToken;

@Component({
  selector: 'ddap-view-access',
  templateUrl: './view-access.component.html',
  styleUrls: ['./view-access.component.scss'],
})
export class ViewAccessComponent {

  @Input()
  access: ResourceToken;
  @Input()
  url?: string;

}
