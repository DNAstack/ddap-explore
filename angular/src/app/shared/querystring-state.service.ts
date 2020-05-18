import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class QuerystringStateService {
  constructor(private activatedRoute: ActivatedRoute,
              private router: Router) {
  }

  save(name: string, options: SaveOptions) {
    const url = this.router.url.replace(/\?.*/, '');
    const params = {};

    params[name] = this.serialize(options.data);

    this.router.navigate(
      [url],
      {
        queryParams: params,
        queryParamsHandling: 'merge',
        skipLocationChange: false,
      }
    );
  }

  load(name): any {
    const params = this.activatedRoute.snapshot.queryParams;

    if (!params[name]) {
      return null;
    }

    return this.deserialize(params[name]);
  }

  private serialize(data: any): string {
    return btoa(JSON.stringify(data));
  }

  private deserialize(serializedData: string): any {
    return JSON.parse(atob(serializedData));
  }
}

export interface SaveOptions {
  data?: object;
}
