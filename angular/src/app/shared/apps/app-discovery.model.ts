import { KeyValuePair } from '../key-value-pair.model';

export interface BeaconInfoResponseModel {
  id: string;
  name: string;
  apiVersion: string;
  description: string;
  organization: BeaconOrganizationModel;
  info?: KeyValuePair<string>;
}

export interface BeaconOrganizationModel {
  id: string;
  name: string;
  description: string;
  info?: KeyValuePair<string>;
}

export interface BeaconInfoRequestModel extends KeyValuePair<string> {

}

export interface BeaconQueryResponseModel {
  beaconId: string;
  apiVersion: string;
  exists: boolean;
  alleleRequest: BeaconQueryAlleleRequestModel;
  datasetAlleleResponses: BeaconQueryAlleleResponseModel[];
  requiresAdditionalAuth: boolean;
  authorizationUrlBase?: string;
  queryError?: BeaconQueryErrorModel;
}

export interface BeaconQueryAlleleRequestModel {
  referenceName: string;
  referenceBases: string;
  alternateBases: string;
  variantType: string;
  assemblyId: string;
  start: number;
  end: number;
  datasetIds: string[];
}

export interface BeaconQueryAlleleResponseModel {
  datasetId: string;
  exists: boolean;
  variantCount: number;
  callCount: number;
  sampleCount: number;
  frequency: number;
  note: string;
  externalUrl: string;
}

export interface BeaconQueryErrorModel {
  status: number;
  message: string;
}

export interface BeaconQueryRequestModel extends KeyValuePair<string> {
  assemblyId?: string;
  referenceName?: string;
  start?: string;
  referenceBases?: string;
  alternateBases?: string;
}
