import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import Tool from './tool.model';

@Injectable({
  providedIn: 'root',
})
export class TrsService {
  constructor(private http: HttpClient) {
  }

  public getTools(): Observable<Tool[]> {
    return this.http.get<Tool[]>('https://dockstore.org/api/api/ga4gh/v2/tools?limit=1000');
  }
}
