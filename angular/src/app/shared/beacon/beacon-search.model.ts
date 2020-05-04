import { KeyValuePair } from '../key-value-pair.model';

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

export interface BeaconQueryAlleleRequestModel extends KeyValuePair<any> {
  referenceName: string;
  referenceBases: string;
  alternateBases: string;
  assemblyId: string;
  start: string;
  end?: string;
  variantType?: string;
  datasetIds?: string[];
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
  info?: AlleleResponseInfoModel;
}

export interface BeaconQueryErrorModel {
  status: number;
  message: string;
}

export interface AlleleResponseInfoModel extends KeyValuePair<any> {
  data?: any;
  data_model?: {
    $id: string;
    $schema: string;
    properties: KeyValuePair<any>;
    required: string[];
    type: string;
  };
}

export enum Assembly {
  grch37 = 'GRCh37',
  grch38 = 'GRCh38',
  ncbi36 = 'NCBI36',
}

export enum BeaconInterfaceType {
  base = 'http:beacon',
  virus = 'http:beacon:virus',
}
