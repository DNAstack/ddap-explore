export class BeaconRequest {
    referenceName: string;
    referenceBases: string;
    alternateBases: string;
    assemblyId: string;
    start: number;
}

export class BeaconResponse {
    datasetId: string;
    exists: boolean;
    info: any[];
}