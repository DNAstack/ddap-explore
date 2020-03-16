import { KeyValue } from '@angular/common';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import 'brace';
import 'brace/mode/sql';
import 'brace/theme/eclipse';

import { SearchService } from '../search.service';

import { JsonViewerService } from './json-viewer/json-viewer.component';
import { BeaconQuery, BeaconRegistry, SearchView } from './search-tables.model';
import Table = WebAssembly.Table;

@Component({
  selector: 'ddap-search-detail',
  templateUrl: './search-tables.component.html',
  styleUrls: ['./search-tables.component.scss'],
})
export class SearchTablesComponent implements OnInit, AfterViewInit {
  @ViewChild('editor', {static: false}) editor;
  searchTables: object[] = [];
  registry: BeaconRegistry;

  private QUERY_EDITOR_DELIMITER = ';';
  private QUERY_EDITOR_NEWLINE = '\n';

  private view: SearchView;

  private options: {
    wrapBehavioursEnabled: true
  };

  private search: {
    text: string,
  };

  private query: string;
  private result: any;
  private properties: string[];
  private queryHistory: string[];


  constructor( private searchService: SearchService,
               private route: ActivatedRoute,
               private jsonViewerService: JsonViewerService) {

    this.search = { text : '' };

    this.view = {
      errorLoadingTables : true,
      errorQueryingTables : true,
      showQueryEditor : true,
      showTables : true,
      wrapSearchResults : false,
      isSearching : false,
      isRefreshingTables : false,
    };

    this.queryHistory = [];
  }

  ngOnInit() {
    this.searchService.getTables().subscribe(data => {
      this.searchTables = data['tables'];
    });
  }

  ngAfterViewInit(): void {
    this.editor.setTheme('eclipse');
    this.editor.setMode('sql');
    this.editor.getEditor().setOptions({
      enableBasicAutocompletion: true,
        /*enableLiveAutocompletion: true*/
    // this introduces weird behaviour where multiple characters get typed with every keypressResults
    });

    this.editor.getEditor().commands.addCommand({
        name: 'showOtherCompletions',
        bindKey: 'Ctrl-.',
        exec: function (editor) {
        },
    });

    this.editor.getEditor().commands.addCommand({
      name: 'run',
      exec: (e) => {
        this.doSearchFromEditor();
      },
      bindKey: {mac: 'cmd-return', win: 'ctrl-enter'},
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

//  EDITOR STUFF
  doSearchFromEditor() {
    this.doSearch(this.getQueryFromEditor());
  }

  getQueryFromEditor() {

    let query = this.editor.getEditor().getValue();

    query = query.trim();
    query = query.replace('/' + this.QUERY_EDITOR_DELIMITER + '+$/', '');

    let cursorPosition = this.editor.getEditor()
      .session.doc.positionToIndex(this.editor.getEditor().selection.getCursor());
    if (cursorPosition >= query.length) {
      cursorPosition = query.length - 1;
    }

    while (query[cursorPosition] === this.QUERY_EDITOR_NEWLINE || query[cursorPosition] === ' ') {
      cursorPosition = cursorPosition - 1;
      if (cursorPosition === 0) {
        break;
      }
    }

    if (cursorPosition > 0 && query[cursorPosition - 1] === this.QUERY_EDITOR_DELIMITER) {
      cursorPosition = cursorPosition - 1;
    }

    let leftPosition = cursorPosition;

    while (leftPosition > 0) {
      if (query[leftPosition] === this.QUERY_EDITOR_DELIMITER && cursorPosition !== leftPosition) {
        leftPosition = leftPosition + 1;
        break;
      }
      leftPosition = leftPosition - 1;
    }

    let rightPosition = cursorPosition;
    while (rightPosition <= query.length + 1) {
      if (query[rightPosition] === this.QUERY_EDITOR_DELIMITER) {
        break;
      }
      rightPosition = rightPosition + 1;
    }

    query = query.substring(leftPosition, rightPosition).trim();

    return query;
  }

  doSearch(query: string) {

    // if (query.length === 0) {
    //   this.snackBar.open('Empty query', 'Dismiss', {
    //     panelClass: 'error-snack',
    //   });
    //   return;
    // }

    this.view.isSearching = true;
    this.view.errorQueryingTables = false;
    this.searchService.search({ 'query' : query }).subscribe(result => {
      this.query = query;
      this.result = result;
      this.view.isSearching = false;
      this.queryHistory.unshift(query);
      const schema = result['data_model']['properties'];
      const properties = Object.keys(schema);
      this.properties = properties.filter(e => e !== 'description');
    });
    // this.searchService.search({ 'query' : query }, this.headers).then(
    //   result => {
    //     this.query = query;
    //     this.result = result;
    //     this.view.isSearching = false;
    //     this.queryHistory.unshift(query);
    //     const schema = result['data_model']['properties'];
    //     const properties = Object.keys(schema);
    //     this.properties = properties.filter(e => e !== 'description');
    //   }
    // ).catch(error => {
    //   this.view.isSearching = false;
    //   this.view.errorQueryingTables = true;
    //   this.snackBar.open(error, 'Dismiss', {
    //     panelClass: 'error-snack',
    //   });
    //   return;
    // });
  }

  queryChanged($event) {
  }

  viewResultsJSON() {
    this.jsonViewerService.viewJSON(this.result);
  }

}
