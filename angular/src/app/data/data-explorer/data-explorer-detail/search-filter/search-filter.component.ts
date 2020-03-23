import { Component, Input } from '@angular/core';

import { SearchFilter } from './search-filter';

// @Component({
//     selector: 'ddap-search-filter',
//     templateUrl: './search-filter.component.html',
//     styleUrls: ['./search-filter.component.scss', '../../base/base.component.scss'],
// })
export class SearchFilterComponent {

    @Input() filter: SearchFilter;

    constructor(
    ) {
    }

    selectAll(filter: SearchFilter) {
        if (filter.type === 'enum' && !filter.exclusive) {
            filter.value = filter.options;
        }
    }

    selectNone(filter: SearchFilter) {
        if (filter.type === 'enum' && !filter.exclusive) {
            filter.value = [];
        }
    }
}
