import { Component, Input } from '@angular/core';

import { dam } from '../proto/dam-service';
import IResourceToken = dam.v1.ResourceTokens.IResourceToken;

@Component({
  selector: 'ddap-view-access',
  templateUrl: './view-access.component.html',
  styleUrls: ['./view-access.component.scss'],
})
export class ViewAccessComponent {

  @Input()
  access: IResourceToken;
  @Input()
  url?: string;

}
