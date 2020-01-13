import { Inject, Injectable } from '@angular/core';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';

import { dam } from './proto/dam-service';
import IResourceTokens = dam.v1.IResourceTokens;

const storageKey = `RESOURCE_TOKENS`;

@Injectable({
  providedIn: 'root',
})
export class ResourceAuthStateService {

  constructor(@Inject(LOCAL_STORAGE) private storage: StorageService) { }

  storeAccess(resourceTokens: IResourceTokens): void {
    this.storage.set(storageKey, JSON.stringify(resourceTokens));
  }

  getAccess(): IResourceTokens {
    return this.storage.has(storageKey)
           ? JSON.parse(this.storage.get(storageKey))
           : {};
  }

}
