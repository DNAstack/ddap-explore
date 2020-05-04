import { Component, Input } from '@angular/core';

@Component({
  selector: 'ddap-table-cell-data',
  templateUrl: './table-cell-data.component.html',
  styleUrls: ['./table-cell-data.component.scss'],
})
export class TableCellDataComponent {

  @Input()
  title: string;
  @Input()
  value?: string;

}
