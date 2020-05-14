
import { ResourceModel } from '../apps/resource.model';

export interface SPIAppBeacon {
  resource: ResourceModel;
  id: string;
  name: string;
  apiVersion: string;
  organization: Organization;
  datasets: Dataset[];
  sampleAlleleRequests: SampleRequest[];
}

interface Organization {
  id: string;
  name: string;
  description: string;
  address: string;
  welcomeUrl: string;
  contactUrl: string;
  logoUrl: string;
}

interface Dataset {
  id: string;
  name: string;
  assemblyId: string;
}

interface SampleRequest {
  referenceName: string;
  referenceBases: string;
  alternateBases: string;
  assemblyId: string;
  start: number;
}
