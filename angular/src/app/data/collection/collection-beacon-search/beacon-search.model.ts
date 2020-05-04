import { KeyValuePair } from '../../../shared/key-value-pair.model';

export interface BeaconSearchResponseModel {
  beaconInfo: {
    name: string,
    damId: string,
    resourceLabel: string,
    resourceId: string,
    viewId: string,
    resourcePath: string,
  };
  datasetAlleleResponses: [{
    datasetId: string,
    exists: boolean,
    info: {[key: string]: string},
  }];
  queryError?: {
    status: number,
    message: string
  };
  error?: {
    errorCode: number,
    errorMessage: string
  };
  exists: boolean;
}

export interface BeaconSearchRequestModel extends KeyValuePair<string> {
  limitSearch: string;
  query: string;
  assembly: string;
  resource?: string;
  collection?: string;
  damId?: string;
}
