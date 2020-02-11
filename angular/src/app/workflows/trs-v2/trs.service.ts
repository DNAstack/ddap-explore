import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ToolFile } from './tool-file.model';
import { Tool } from './tool.model';

/**
 * TRS Service
 *
 * Client for TRS API 2.0.0b3
 *
 * WARNING: This implementation is only tested to work with DDAP Explore in the standalone mode.
 */
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

  public getFileList(id: string, versionId: string, type: string): Observable<ToolFile[]> {
    return this.http.get<ToolFile[]>(this.buildToolVersionUrl(id, versionId, type, 'files'));
  }

  public getDescriptor(id: string, versionId: string, type: string): Observable<string> {
    return this.http.get<string>(this.buildToolVersionUrl(id, versionId, type, 'descriptor'));
  }

  public buildToolVersionUrl(id: string, version: string, type: string, subtype: string): string {
    const encodedToolId = encodeURIComponent(id);
    const encodedVersionId = encodeURIComponent(version);
    return `${this.baseUrl}/tools/${encodedToolId}/versions/${encodedVersionId}/${type}/${subtype}`;
  }
}
