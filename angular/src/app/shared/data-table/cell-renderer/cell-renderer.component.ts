import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';

const urlPattern = /https?:\/\/[^\s]+/;

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
    if (!this.params || !this.params.value) {
      return false;
    }
    const matchResult = this.params.value.match(urlPattern);
    return matchResult && matchResult.index === 0;
  }

  refresh(params: ICellRendererParams): boolean {
    this.params = params;
    return true;
  }
}

