import { ResourceModel } from '../apps/resource.model';
import { JsonSchema } from '../search/json-schema.model';

export interface SPIAppSearchSimple {
  data_model: JsonSchema;
  name: string;
  requiresAdditionalAuth: boolean;
  resource: ResourceModel;
}
