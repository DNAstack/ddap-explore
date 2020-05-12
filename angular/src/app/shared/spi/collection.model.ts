import { SPIMetadata } from './metadata.model';

export interface SPICollection {
  id: string;
  name: string;
  description: string;
  metadata?: SPIMetadata;
}
