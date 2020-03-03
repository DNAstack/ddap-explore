  export interface Table {
    name: string;
    description: string;
    data_model: DataModel;
  }

  export interface DataModel {
    id: string;
    description: string;
    properties: any[];
  }

  export interface Dataset {
    id: string;
    description: string;
    schema: any;
  }

  export interface Tables {
    tables: Table[];
  }

  export interface Datasets {
    datasets: Dataset[];
  }

  export interface TableData {
    data_model: DataModel;
    pagination: Pagination;
    data: any[];
  }

  export interface Pagination {
    next_page_url: URL;
    previous_page_url: URL;
  }

  export interface Query {
    query: string;
  }
