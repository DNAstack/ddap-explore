import { Inject, Injectable } from '@angular/core';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';

@Injectable({
  providedIn: 'root',
})
export class WorkflowsStateService {

  constructor(@Inject(LOCAL_STORAGE) private storage: StorageService) { }

  storeMetaInfoForWorkflow(workflowId: string, meta: object) {
    const storageKey = `wf_meta_${workflowId}`;
    this.storage.set(storageKey, JSON.stringify(meta));
  }

  storeWorkflowForm(workflowId: string, workflow: any): void {
    const storageKey = `wf_${workflowId}`;
    this.storage.set(storageKey, JSON.stringify(workflow));
  }

  getWorkflowForm(workflowId: string): any {
    const storageKey = `wf_${workflowId}`;
    return this.storage.has(storageKey)
           ? JSON.parse(this.storage.get(storageKey))
           : {};
  }

  getMetaInfoForWorkflow(workflowId: string): any {
    const storageKey = `wf_meta_${workflowId}`;
    return this.storage.has(storageKey)
           ? JSON.parse(this.storage.get(storageKey))
           : {};
  }

  removeWorkflowData(workflowId: string): any {
    const storageKeys = [`wf_meta_${workflowId}`, `wf_${workflowId}`];
    storageKeys.forEach((storageKey) => {
      if (this.storage.has(storageKey)) {
        this.storage.remove(storageKey);
      }
    });
  }

}
