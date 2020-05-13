import { Component, Input, OnInit } from '@angular/core';

import { SimpleSearchRequest } from '../../shared/spi/app-search-simple-filter-request.model';
import { SPIAppSearchSimple } from '../../shared/spi/app-search-simple.model';
import { SPIAppService } from '../../shared/spi/spi-app.service';

/**
 * Simple Search Component
 *
 * It is a reusable component within the context of Workspace.
 */
@Component({
  selector: 'ddap-simple-search',
  templateUrl: './simple-search.component.html',
  styleUrls: ['./simple-search.component.scss'],
})
export class SimpleSearchComponent implements OnInit {
  @Input()
  resource: SPIAppSearchSimple;

  constructor(private spiAppService: SPIAppService) {
  }

  ngOnInit(): void {
    this.initialize();
  }

  getFields() {
    const fields: Field[] = [];

    for (const fieldName in this.resource.data_model.properties) {
      fields.push({
        name: fieldName,
      });
    }

    return fields;
  }

  update() {
    const interfaceId = this.resource.resource.interfaces[0].id;
    const filter: SimpleSearchRequest = {
      filters: {},
      order: [],
    }; // default filter

    this.spiAppService.submitSimpleSearchFilter(interfaceId, filter)
      .subscribe(response => {
        // TODO either bind this a component property or move the entire method invocation to a local service.
      });
  }

  private initialize() {
    this.update();
  }
}

interface Field {
  name: string;
}
