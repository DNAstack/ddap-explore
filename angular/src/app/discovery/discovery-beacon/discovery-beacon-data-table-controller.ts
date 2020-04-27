import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ILatLong } from 'angular-maps';
import { catchError } from 'rxjs/operators';

import { ColumnDefinition, DataTableController } from '../../shared/data-table/data-table-controller';
import { BeaconResponse } from '../beacon-service/beacon.model';
import { BeaconService } from '../beacon-service/beacon.service';

export class DiscoveryBeaconDataTableController extends DataTableController {

  columnDefinitionList: ColumnDefinition[]; // private?

  queryForm = new FormGroup({
    start: new FormControl('',
      {
        validators: [
          Validators.required,
          Validators.pattern(/^\d+$/),
          Validators.max(Number.MAX_SAFE_INTEGER),
        ],
      }),
    referenceBases: new FormControl('',
      {
        validators: [
          Validators.required,
          Validators.pattern(/^[ACGT]+$/i),
        ],
      }),
    alternateBases: new FormControl('',
      {
        validators: [
          Validators.required,
          Validators.pattern(/^[ACGT]+$/i),
        ],
      }),
  });
  private _onSelectionChanged: CallableFunction;
  private _canSearch: CallableFunction;
  private _beforeSearch: CallableFunction;
  private _afterSuccessfulSearch: CallableFunction;
  private _afterFailedSearch: CallableFunction;

  private beaconService: BeaconService;

  constructor(beaconService: BeaconService,
              onSelectionChanged: CallableFunction,
              canSearch: CallableFunction,
              beforeSearch: CallableFunction,
              afterSuccessfulSearch: CallableFunction,
              afterFailedSearch: CallableFunction) {
    super();

    this.beaconService = beaconService;

    this._onSelectionChanged = onSelectionChanged;
    this._canSearch = canSearch;
    this._beforeSearch = beforeSearch;
    this._afterSuccessfulSearch = afterSuccessfulSearch;
    this._afterFailedSearch = afterFailedSearch;
  }

  onSelectionChanged(selectedRow: Map<string, any>) {
    this._onSelectionChanged(selectedRow);
  }

  initialize() {
    this.beginQuery();
  }

  beginQuery() {
    if (!this._canSearch()) {
      return;
    }

    const query = this.getQuerySnapshot();

    this.find(query);
  }

  find(query: QuerySnapshot) {
    this._beforeSearch();

    this.beaconService.runObservableBeaconSearch(
      'hCoV-19',
      '1',
      query.start,
      query.referenceBases,
      query.alternateBases
    ).pipe(
      catchError((e) => {
        this._afterFailedSearch(e);
        throw e;
      })
    ).subscribe(
      data => {
        this.queryForm.enable();

        const responses: BeaconResponse[] = data['datasetAlleleResponses'];

        if (responses.length === 0) {
          // No results
          this.setResultList([]);
          this.setColumnDefinitionList([]);
          return;
        }

        const info = responses[0].info;

        if (info.length === 0) {
          // No results
          this.setResultList([]);
          this.setColumnDefinitionList([]);
          return;
        }

        const {resultList, columnList} = (info['data'] === undefined) ? this.parseLegacyResponse(info) : this.parseTableResponse(info);

        const columnDefinitionList: ColumnDefinition[] = columnList.map(keyStr => {
          return {
            field: keyStr,
            headerName: this.titleCase(keyStr.replace(/_/g, ' ')),
          };
        });

        this.setResultList(resultList);
        this.setColumnDefinitionList(columnDefinitionList);

        this._afterSuccessfulSearch();
      },
      error => this._afterFailedSearch()
    );
  }

  getQuerySnapshot(): QuerySnapshot {
    const snapshot = this.queryForm.value;
    return {
      start: parseInt(snapshot['start'], 10),
      referenceBases: snapshot['referenceBases'].toUpperCase(),
      alternateBases: snapshot['alternateBases'].toUpperCase(),
    };
  }

  buildNextStrainUrl(source): string {
    const tokens = source.split('/');
    if (tokens.length === 1) {
      return null;
    }
    return 'https://nextstrain.org/ncov?s=' + tokens[1] + '/' + tokens[2] + '/' + tokens[3];
  }

  private parseTableResponse(info) {
    const resultList: object[] = info['data'];
    const columnList: string[] = [];

    const propertyTypeMap: Map<string, string> = new Map<string, string>();
    let propertyMap: object = {};

    if (info && info['data_model'] && info['data_model']['properties']) {
      propertyMap = info['data_model']['properties'];
    }

    for (const name in propertyMap) {
      propertyTypeMap.set(name, propertyMap[name]['type']);
      columnList.push(name);
    }

    resultList.forEach(result => {
      // Please note that Map.prototype.forEach gives value, and then key to the callback function. For example,
      //
      //  map.forEach((value, key) => { ... });
      //
      // See more https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map/forEach
      propertyTypeMap.forEach((type, name) => {
        if (result[name] === undefined) {
          result[name] = null; // Fill in a missing property.
        } else if (type === 'integer') {
          result[name] = parseInt(result[name], 10);
        }
      });

      if (propertyTypeMap.size === 0) {
        Object.keys(result).forEach(name => {
          if (!columnList.includes(name)) {
            columnList.push(name);
          }
        });
      }
    });


    return {
      resultList,
      columnList,
    };
  }

  // FIXME This will soon be deprecated.
  private parseLegacyResponse(info) {
    const resultList = [];
    const columnList: string[] = [];

    for (let i = 0; i < info.length; i++) {
      const key = info[i].key;
      const keyTokens = key.split('=');
      const keyType = keyTokens[0];

      const value = info[i].value;
      const valueTokens = value.split(':');

      const valueDict = {};

      for (let j = 0; j < valueTokens.length; j++) {

        const valueToken = valueTokens[j];

        const valueTokenTokens = valueToken.split('=');
        const valueTokenKey = valueTokenTokens[0];
        const valueTokenValue = valueTokenTokens[1];

        valueDict[valueTokenKey] = valueTokenValue;

        if (!columnList.includes(valueTokenKey)) {
          columnList.push(valueTokenKey);
        }
      }

      if (keyType === 'case') {
        resultList.push(valueDict);
      }
    }

    return {
      resultList,
      columnList,
    };
  }

  private titleCase(str) {
    const splitStr = str.toLowerCase().split(' ');
    for (let i = 0; i < splitStr.length; i++) {
      // You do not need to check if i is larger than splitStr length, as your for does that for you
      // Assign it back to the array
      splitStr[i] = splitStr[i].charAt(0).toUpperCase() + splitStr[i].substring(1);
    }
    // Directly return the joined string
    return splitStr.join(' ');
  }
}
