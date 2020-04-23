import { JsonSchema } from './json-schema.model';

export interface TableModel {
  data_model?: JsonSchema;
  data: object[];
  pagination?: {
    next_page_url: string,
    previous_page_url: string,
  };
}
