import { SPIMetadata } from './metadata.model';

export class SPIResource {
  collectionId: string;
  description: string;
  id: string;
  imageUrl?: string;
  interfaces: SPIInterface[];
  name: string;
  metadata?: SPIMetadata;
}

class SPIInterface {
  type: string;
  uri: string;
  id: string;
  authRequired: boolean;
}
