export interface ResourceAccessFormModel {
  ttl: {
    numericValue: number;
    timeUnit: string;
  };
  interfaceType: string;
}

export const defaultState: ResourceAccessFormModel = {
  ttl: {
    numericValue: 1,
    timeUnit: 'h',
  },
  interfaceType: undefined,
};
