import { Component, Input } from '@angular/core';

@Component({
  selector: 'ddap-access-denied-screen',
  templateUrl: './access-denied-screen.component.html',
  styleUrls: ['./access-denied-screen.component.scss'],
})
export class AccessDeniedScreenComponent {
  @Input()
  authorizationUrl: string;

  @Input()
  message: string;

  @Input()
  align = '';
}
