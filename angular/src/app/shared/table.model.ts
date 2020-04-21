interface DataModel {
  property?: object;
}

export interface Table {
  data_model?: DataModel;
  data: object[];
  pagination?: {
    next_page_url: string,
    previous_page_url: string,
  };
}
