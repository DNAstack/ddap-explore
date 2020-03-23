export class Beacon {
    id: string;
    name: string;
    url: string;
    organization: string;
    description: string;
    homePage: string;
    email: string;
    aggregator: boolean;
    enabled: boolean;
    visible: boolean;
    createdDate: string;
    supportedReferences: string[];
    aggregatedBeacons: boolean;
}

export class BeaconRegistry {
    id: string;
    url: string;
    apiUrl: string;
}

export class BeaconQuery {
    chromosome: string;
    position: number;
    referenceAllele: string;
    allele: string;
    reference: string;
}

export class AuthHint {
    accessType: string;
    // authRequirements: string[];
}

export class BeaconResponse {
    beacon: Beacon;
    query: BeaconQuery;
    chromosome: string;
    position: number;
    referenceAllele: string;
    allele: string;
    reference: string;
    response: boolean;
    frequency: number;
    externalUrl: string;
    // info: string;
    authHint: AuthHint;
    accessType: string;
    // authRequirements: string[];
    // fullBeaconResponse: string;
}
