import { Inject, Injectable } from '@angular/core';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';

import { ResourceAccessFormModel } from './resource-access-form.model';

const keyPrefix = 'resource_access_form_state__';

@Injectable({
  providedIn: 'root',
})
export class ResourceAccessFormStateService {

  constructor(@Inject(LOCAL_STORAGE) private storage: StorageService) { }

  storeFormState(resourceId: string, accessForm: ResourceAccessFormModel): void {
    const storageKey = `${keyPrefix}${resourceId}`;
    this.storage.set(storageKey, JSON.stringify(accessForm));
  }

  getFormState(resourceId: string): ResourceAccessFormModel | undefined {
    const storageKey = `${keyPrefix}${resourceId}`;
    return this.storage.has(storageKey)
           ? JSON.parse(this.storage.get(storageKey))
           : undefined;
  }

  removeFormState(resourceId: string): void {
    const storageKey = `${keyPrefix}${resourceId}`;
    if (this.storage.has(storageKey)) {
      this.storage.remove(storageKey);
    }
  }

}
