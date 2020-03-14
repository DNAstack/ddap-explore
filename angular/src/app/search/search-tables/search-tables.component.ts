import { KeyValue } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { SearchService } from '../search.service';

import { JsonViewerService } from './json-viewer/json-viewer.component';
import { BeaconQuery, BeaconRegistry } from './search-tables.model';
import Table = WebAssembly.Table;

@Component({
  selector: 'ddap-search-detail',
  templateUrl: './search-tables.component.html',
  styleUrls: ['./search-tables.component.scss'],
})
export class SearchTablesComponent implements OnInit {
  @ViewChild('editor', {static: false}) editor;
  searchTables: object[] = [];
  registry: BeaconRegistry;

  query: BeaconQuery;
  private QUERY_EDITOR_DELIMITER = ';';
  private QUERY_EDITOR_NEWLINE = '\n';

  constructor( private searchService: SearchService,
               private route: ActivatedRoute,
               private jsonViewerService: JsonViewerService) { }

  ngOnInit() {
    this.searchService.getTables().subscribe(data => {
      this.searchTables = data['tables'];
    });
  }

  refreshBeacons() {
    // this.setRegistry(this.registry);
  }

  closeTables() {

  }

  addAtCursor(text: string) {
    if (this.editor != null) {
      const cursorPosition = this.editor.getEditor().session.doc.positionToIndex(this.editor.getEditor().selection.getCursor());

      if (cursorPosition > 0 && this.editor.getEditor().getValue()[cursorPosition - 1] === this.QUERY_EDITOR_DELIMITER) {
        text = this.QUERY_EDITOR_NEWLINE + text;
      }

      this.editor.getEditor().session.replace(this.editor.getEditor().selection.getRange(), text);
      this.editor.getEditor().focus();
    }
  }

  propertyOrder = (a: KeyValue<string, any>, b: KeyValue<string, any>): number => {
    const positionKey = 'x-ga4gh-position';
    const aPos = a[positionKey];
    const bPos = b[positionKey];
    return aPos > bPos ? -1 : (bPos > aPos ? 1 : 0);
  }

  previewTableQuery(tableName: string) {
    return 'SELECT * FROM ' + tableName + ' LIMIT 50;';
  }

  viewTableAsJSON(table: Table) {
    this.jsonViewerService.viewJSON(table);
  }

  private initialize() {

    this.query = new BeaconQuery();
    this.query.chromosome = '1';
    this.query.position = 1;
    this.query.allele = 'A';
    this.query.referenceAllele = 'A';

    // this.selectedRegistryId = this.route.snapshot.queryParamMap.get('network');
    // this.selectedBeaconId = this.route.snapshot.queryParamMap.get('beacon');
    //
    // this.registries = this.beaconConfigService.getRegistries();
    // if (this.registries.length > 0) {
    //
    //   if (this.selectedRegistryId == null || this.selectedBeaconId.length === 0) {
    //     this.setRegistry(this.registries[0]);
    //   } else {
    //     for (let i = 0; i < this.registries.length; i++) {
    //       if (this.registries[i].id === this.selectedRegistryId) {
    //         this.setRegistry(this.registries[i]);
    //       }
    //     }
    //   }
    // }
  }

}
