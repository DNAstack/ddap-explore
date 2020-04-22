import { KeyValuePair } from './key-value-pair.model';

export interface ResourceModel {
  id: string;
  collectionId: string;
  description: string;
  name: string;
  imageUrl?: string;
  interfaces?: InterfaceModel[];
  metadata?: { [key: string]: string };
}

export interface InterfaceModel {
  type: string;
  uri: string;
  authorizationId?: string; // If not present -> public
}

export interface ResourcesResponseModel {
  data: ResourceModel[];
  nextPageToken?: string;
}

export interface ResourcesRequestModel extends KeyValuePair<string> {
  collection?: string;
  interface_type?: string;
  interface_uri?: string;
  pageToken?: string;
}
