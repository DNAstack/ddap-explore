import { Inject, Injectable } from '@angular/core';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';

import { dam } from './proto/dam-service';
import IResourceTokens = dam.v1.IResourceTokens;
import IResourceToken = dam.v1.ResourceTokens.IResourceToken;

const storageKey = `RESOURCE_TOKENS`;

@Injectable({
  providedIn: 'root',
})
export class ResourceAuthStateService {

  constructor(@Inject(LOCAL_STORAGE) private storage: StorageService) { }

  storeAccess(resourceTokens: {[key: string]: IResourceToken}): void {
    const existing: IResourceTokens = this.getAccess();
    const merged = { ...existing, ...resourceTokens };
    this.storage.set(storageKey, JSON.stringify(merged));
  }

  getAccess(): {[key: string]: IResourceToken} {
    return this.storage.has(storageKey)
           ? JSON.parse(this.storage.get(storageKey))
           : {};
  }

}
