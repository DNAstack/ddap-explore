import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { AppConfigService } from '../../shared/app-config/app-config.service';

import { ToolFile } from './tool-file.model';
import { Tool } from './tool.model';

/**
 * TRS Service
 *
 * Client for TRS API 2.0.0b3
 *
 * WARNING: This implementation is only tested to work with DDAP Explore in the standalone mode.
 *
 * NOTE: The reason that some endpoints are promises of an observable is because the service need to wait until the base
 *       URL has been set properly. This is to avoid the client to unintentionally send a request to an unintended server.
 */
@Injectable({
  providedIn: 'root',
})
export class TrsService {
  private baseUrl: string;
  constructor(private appConfigService: AppConfigService,
              private http: HttpClient) {
    this.appConfigService.get().subscribe(appConfig => {
      this.setBaseUrl(appConfig.trsBaseUrl);
    });
  }

  public setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  public async getTools(): Promise<Observable<Tool[]>> {
    return this.http.get<Tool[]>(`${await this.getBaseUrl()}/tools?limit=1000`);
  }

  public getDescriptorFrom(url: string): Observable<any> {
    return this.http.get(url, {responseType: 'text'});
  }

  private async getBaseUrl(): Promise<string> {
    return new Promise<string>(resolve => {
      this.resolveGetBaseUrl(resolve, 1, 32);
    });
  }

  private resolveGetBaseUrl(resolve, proposedBackOffTime, maxBackOffTime) {
    if (maxBackOffTime < proposedBackOffTime) {
      throw new Error(`Failed to get the base URL for TRS service within ${maxBackOffTime}`);
    }

    if (!this.baseUrl) {
      setTimeout(() => {
        this.resolveGetBaseUrl(resolve, proposedBackOffTime * 2, maxBackOffTime);
      }, proposedBackOffTime);
    } else {
      resolve(this.baseUrl);
    }
  }
}
