import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import Tool from './tool.model';

@Injectable({
  providedIn: 'root',
})
export class TrsService {
  private baseUrl: string;
  constructor(private http: HttpClient) {
  }

  public setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  public getTools(): Observable<Tool[]> {
    return this.http.get<Tool[]>(`${this.baseUrl}/tools?limit=1000`);
  }
}
