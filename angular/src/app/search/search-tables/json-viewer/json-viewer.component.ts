import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
  })
export class JsonViewerService {

    constructor(
    ) {
    }

    public viewJSON(obj: any) {
        const blob = new Blob([JSON.stringify(obj, null, 2)], { type: 'text/json' });
        const url = window.URL.createObjectURL(blob);
        window.open(url);
    }

}
