import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'ddap-component-frame',
  templateUrl: './component-frame.component.html',
  styleUrls: ['./component-frame.component.scss'],
})
export class ComponentFrameComponent implements OnInit {

  @Input()
  disableMobile = false;
  @Input()
  maxWidth = '599px';
  isMobile = false;

  constructor(private breakpointObserver: BreakpointObserver) { }

  ngOnInit() {
    this.isMobile = this.breakpointObserver.isMatched(`(max-width: ${this.maxWidth})`);
  }

}
