import { Assembly, BeaconQueryAlleleRequestModel } from '../../../shared/beacon/beacon-search.model';

export const defaultState: BeaconQueryAlleleRequestModel = {
  assemblyId: Assembly.grch37,
  referenceName: '1',
  start: '156105028',
  referenceBases: 'T',
  alternateBases: 'C',
};
