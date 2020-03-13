import { Inject, Injectable } from '@angular/core';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';

import { dam } from './proto/dam-service';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;
import IResourceResults = dam.v1.IResourceResults;

const storageKey = `RESOURCE_TOKENS`;

@Injectable({
  providedIn: 'root',
})
export class ResourceAuthStateService {

  constructor(@Inject(LOCAL_STORAGE) private storage: StorageService) { }

  storeAccess(resourceTokens: {[key: string]: IResourceAccess}): void {
    const existing: IResourceResults = this.getAccess();
    const merged = { ...existing, ...resourceTokens };
    this.storage.set(storageKey, JSON.stringify(merged));
  }

  getAccess(): {[key: string]: IResourceAccess} {
    return this.storage.has(storageKey)
           ? JSON.parse(this.storage.get(storageKey))
           : {};
  }

}
