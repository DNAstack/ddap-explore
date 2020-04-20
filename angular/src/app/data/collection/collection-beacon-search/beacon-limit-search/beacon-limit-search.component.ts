import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'ddap-beacon-limit-search',
  templateUrl: './beacon-limit-search.component.html',
  styleUrls: ['./beacon-limit-search.component.scss'],
})
export class BeaconLimitSearchComponent {

  @Input()
  checked;
  @Input()
  collectionName: string;

  @Output()
  readonly change: EventEmitter<any> = new EventEmitter<any>();

}
