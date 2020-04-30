import { KeyValuePair } from '../../key-value-pair.model';

export interface LayoutViewStateModel extends KeyValuePair<any> {
  exp_flag: string;
}

export const defaultState: LayoutViewStateModel = {
  exp_flag: undefined,
};
