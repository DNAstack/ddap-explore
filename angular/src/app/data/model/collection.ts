export class Collection {
    id: string;
    name: string;
    description: string;
    imgUrl: string;
    organization: Organization;
    tableFilters: string[];
    searchServiceId: string;
}

export class Organization {
    name: string;
    url: string;
    logoUrl: string;
    contact: Contact;
}

export class Contact {
    name: string;
    email: string;
    address: string;
}
