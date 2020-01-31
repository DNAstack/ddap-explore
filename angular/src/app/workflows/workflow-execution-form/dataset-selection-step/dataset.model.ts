export interface Dataset {
  data_model: {[key: string]: any};
  data: object[];
  pagination?: {
    previous_page_url?: string;
    prev_page_url?: string;
    next_page_url: string;
  };
}
