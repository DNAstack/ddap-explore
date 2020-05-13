import { BeaconQueryAlleleRequestModel } from '../../beacon/beacon-search.model';
import { KeyValuePair } from '../../key-value-pair.model';
import { ResourceModel } from '../resource.model';

export interface BeaconInfoResourcePair {
  resource: ResourceModel;
  beaconInfo?: BeaconInfoResponseModel;
  error?: KeyValuePair<any>;
}

export interface BeaconInfoResponseModel {
  id: string;
  name: string;
  apiVersion: string;
  description: string;
  organization: BeaconOrganizationModel;
  datasets: BeaconDatasetModel[];
  sampleAlleleRequests?: BeaconQueryAlleleRequestModel[];
  info?: KeyValuePair<string>;
}

export interface BeaconOrganizationModel {
  id: string;
  name: string;
  description: string;
  info?: KeyValuePair<string>;
}

export interface BeaconDatasetModel {
  id: string;
  name: string;
  assemblyId: string;
}

export interface BeaconInfoRequestModel extends KeyValuePair<string> {

}
