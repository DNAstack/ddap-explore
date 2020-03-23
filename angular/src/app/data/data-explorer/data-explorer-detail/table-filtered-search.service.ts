import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Table, Tables } from '../search-service/ga4gh-discovery-search.models';
import { Ga4ghDiscoverySearchService } from '../search-service/ga4gh-discovery-search.service';

export class TableFilteredSearchService extends Ga4ghDiscoverySearchService {

  constructor(
    client: HttpClient,
    private tableFilters: string[]
  ) {
    super(client);
  }

  getTables(headers?: any): Promise<Tables> {
    return super.getTables(headers).then(
      data => {

        const tables = data['tables'];

        if (this.tableFilters == null || this.tableFilters.length === 0) {
          return { 'tables': tables };
        }

        const filtered: Table[] = [];
        for (let i = 0; i < tables.length; i++) {
          for (let j = 0; j < this.tableFilters.length; j++) {
            if (tables[i].name.startsWith(this.tableFilters[j])) {
              filtered.push(tables[i]);
              continue;
            }
          }
        }

        return { 'tables': filtered };
      }
    );
  }
}
