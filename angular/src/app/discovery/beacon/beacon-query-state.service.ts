import { Inject, Injectable } from '@angular/core';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';

const storageKey = 'BEACON_QUERY';
@Injectable({
  providedIn: 'root',
})
export class BeaconQueryStateService {
  constructor(@Inject(LOCAL_STORAGE) private storage: StorageService) {}

  saveQueryState(queryParams) {
    this.storage.set(storageKey, JSON.stringify(queryParams));
  }

  loadQueryState(): object {
    return this.storage.has(storageKey)
      ? JSON.parse(this.storage.get(storageKey))
      : {};
  }

  getValueFromQuery(key: string): string {
    return this.storage.has(storageKey)
      ? JSON.parse(this.storage.get(storageKey))[key]
      : '';
  }

  removeQuery() {
    this.storage.remove(storageKey);
  }
}
