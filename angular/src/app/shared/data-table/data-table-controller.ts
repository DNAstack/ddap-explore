/**
 * Abstract Data Table Controller
 *
 * This is designed to maximize the flexibility over how the data table retrieves the data and the interactions with the table.
 */
export abstract class DataTableController {
  ready: boolean;
  inflight: boolean;
  resultList: any[]; // The result may have dynamic type.
  columnDefinitionList: ColumnDefinition[];

  constructor() {
  }

  abstract beginQuery();
  abstract find(query: any);
  abstract initialize();
  abstract onSelectionChanged(selectedRow: Map<string, any>);

  /**
   * Get the query snapshot
   */
  getQuerySnapshot(): any {
    return null;
  }

  setInflight(inflight: boolean) {
    this.inflight = inflight;
  }

  setReady(ready: boolean) {
    this.ready = ready;
  }

  getResultCount() {
    return this.isFreshStart() ? 0 : this.resultList.length;
  }

  isFreshStart() {
    return this.resultList === undefined || this.resultList === null;
  }

  setResultList(resultList: any[]) {
    this.resultList = resultList;
  }

  setColumnDefinitionList(columnDefinitionList: ColumnDefinition[]) {
    this.columnDefinitionList = columnDefinitionList;
  }
}

export interface ColumnDefinition {
  field: string;
  headerName: string;
}
