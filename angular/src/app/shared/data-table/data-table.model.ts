import { KeyValuePair } from '../key-value-pair.model';

export interface DataTableModel {
  columnDefs: ColumnDef[];
  rowData: RowData[];
}

export interface ColumnDef {
  headerName: string;
  headerTooltip?: string;
  field: string;
  sortable?: boolean;
  filter?: boolean;
  hide?: boolean;
  cellRenderer?: any;
}

export interface DefaultColumnDef {
  sortable?: boolean;
  filter?: boolean;
  editable?: boolean;
  resizable?: boolean;
  width?: number;
}

// https://www.ag-grid.com/javascript-grid-properties/
export interface TableConfig {
  suppressCellSelection?: boolean;
  enableCellTextSelection?: boolean;
  multiSortKey?: string;
}

export enum TableRowSelection {
  single = 'single',
  multiple = 'multiple',
}

export interface RowData extends KeyValuePair<any> {

}
