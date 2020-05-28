import { KeyValuePair } from '../../../shared/key-value-pair.model';

export interface WorkflowExecutionModel {
  wdl: string;
  inputsJson: object;
  credentials: KeyValuePair<CredentialsModel>;
}

export interface CredentialsModel {
  accessKeyId: string; // needed only for AWS
  accessToken: string;
  sessionToken: string; // needed only for AWS
}
