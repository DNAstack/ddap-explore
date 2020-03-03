import { Component } from '@angular/core';

import { Collection } from '../model/collection';

import { DataCollectionsConfigService } from './data-explorer-collections-config.service';

@Component({
  selector: 'ddap-data-explorer',
  templateUrl: './data-explorer.component.html',
  styleUrls: ['./data-explorer.component.scss'],
})
export class DataExplorerComponent {

  private view: {
    showSearchBar: boolean;
    showQueryEditor: boolean;
  };

  private collections: Collection[];

  constructor(
    private dataExplorerConfig: DataCollectionsConfigService
  ) {

    this.view = {
      showSearchBar : false,
      showQueryEditor : false,
    };

    this.collections = dataExplorerConfig.getCollections().sort((a, b) => a.name.localeCompare(b.name));
  }

  toggleQueryEditor() {
    this.view.showQueryEditor = !this.view.showQueryEditor;
  }
}

