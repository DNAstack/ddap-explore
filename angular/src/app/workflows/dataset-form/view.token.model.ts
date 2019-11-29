import { dam } from '../../shared/proto/dam-service';
import ResourceToken = dam.v1.ResourceTokens.ResourceToken;

export interface ViewToken {
  view: string;
  locationAndToken?: ResourceToken;
  exception?: {[key: string]: any};
}

export interface FileViewToken {
  file: string;
  token: ViewToken;
}
