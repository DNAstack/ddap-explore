import { JsonSchema } from '../../../search/json-schema.model';
import { ResourceModel } from '../../resource.model';

/**
 * @deprecated
 */
export interface SPIAppSearchSimple {
  data_model: JsonSchema;
  name: string;
  requiresAdditionalAuth: boolean;
  resource: ResourceModel;
}
