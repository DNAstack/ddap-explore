import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { AccessControlService } from '../access-control.service';

@Component({
  selector: 'ddap-access-denied-screen',
  templateUrl: './access-denied-screen.component.html',
  styleUrls: ['./access-denied-screen.component.scss'],
})
export class AccessDeniedScreenComponent {
  @Input()
  authorizationUrl: string;

  constructor(private accessControl: AccessControlService) {}

  onReauthorizeClick() {
    this.accessControl.purgeSession(true);
  }
}
