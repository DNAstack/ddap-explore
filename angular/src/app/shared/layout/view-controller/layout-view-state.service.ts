import { Inject, Injectable } from '@angular/core';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';

import { defaultState, LayoutViewStateModel } from './layout-view-state.model';

@Injectable({
  providedIn: 'root',
})
export class LayoutViewStateService {

  readonly storageKey = 'active_flags';

  constructor(@Inject(LOCAL_STORAGE) private storage: StorageService) { }

  storeViewFlags(viewState: LayoutViewStateModel): void {
    this.storage.set(this.storageKey, JSON.stringify(viewState));
  }

  getViewFlags(): LayoutViewStateModel {
    return this.storage.has(this.storageKey)
           ? JSON.parse(this.storage.get(this.storageKey))
           : defaultState;
  }

}
