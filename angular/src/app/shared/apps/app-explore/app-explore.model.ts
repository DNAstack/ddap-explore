import { KeyValuePair } from '../../key-value-pair.model';

export interface TokensResponseModel {
  requiresAdditionalAuth: boolean;
  access?: KeyValuePair<AccessModel>;
  authorizationUrlBase?: string;
}

export interface AccessModel {
  creationTime: string;
  expirationTime: string;
  credentials: KeyValuePair<string>;
}

export interface TokensRequestModel extends KeyValuePair<string> {
  minimum_ttl?: string;
}
