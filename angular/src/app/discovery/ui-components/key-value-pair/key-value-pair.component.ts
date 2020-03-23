import { Component, Input } from '@angular/core';

@Component({
  selector: 'ddap-key-value-pair',
  templateUrl: './key-value-pair.component.html',
  styleUrls: ['./key-value-pair.component.scss'],
})
export class KeyValuePairComponent {

    @Input() key;
    @Input() value;

}
