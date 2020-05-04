import { KeyValuePair } from '../key-value-pair.model';

export interface CollectionModel {
  id: string;
  description: string;
  name: string;
  imageUrl?: string;
  metadata?: { [key: string]: string };
}

export interface CollectionsResponseModel {
  data: CollectionModel[];
  nextPageToken?: string;
}

export interface CollectionsRequestModel extends KeyValuePair<string> {
  pageToken?: string;
}
