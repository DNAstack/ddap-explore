import { Component, Input } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

@Component({
  selector: 'ddap-cell-renderer',
  templateUrl: './cell-renderer.component.html',
})
export class CellRendererComponent implements ICellRendererAngularComp {
  params: any;

  agInit(params: ICellRendererParams): void {
    this.params = params;
  }

  generateDataSE(): string {
    return `${this.params.tableName}-${this.params.colDef.field}-${this.params.rowIndex}`;
  }

  isValueURL(): boolean {
    return this.params && this.params.value && this.params.value.indexOf('https://') === 0;
  }

  refresh(params: ICellRendererParams): boolean {
    this.params = params;
    return true;
  }
}

