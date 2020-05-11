export class Resource {
  collectionId: string;
  description: string;
  id: string;
  interfaces: Interface[];
  name: string;
}

class Interface {
  type: string;
  uri: string;
  id: string;
  authRequired: boolean;
}
