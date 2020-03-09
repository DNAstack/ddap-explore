import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { AppConfigService } from '../../shared/app-config/app-config.service';

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
  private clientMap: Map<string, TrsClientContext>;

  constructor(private http: HttpClient) {
    this.clientMap = new Map<string, TrsClientContext>();
  }

  public from(url: string): Client {
    if (!url) {
      return null;
    }

    if (!this.clientMap.has(url)) {
      this.clientMap.set(
        url,
        {
          client: new Client(this.http, url, 1000),
        }
      );
    }

    return this.clientMap.get(url).client;
  }

  public reverseLookup(destinationUrl: string): Client {
    for (const baseUrl in this.clientMap) {
      if (destinationUrl.startsWith(baseUrl)) {
        return this.clientMap[baseUrl];
      }
    }

    throw new Error('Unable to find a client capable to handle the destination URL');
  }
}

interface TrsClientContext {
  client: Client;
}

export class Client {
  private http: HttpClient;
  private baseUrl: string;
  private maxResultCount: number;

  constructor(http: HttpClient, baseUrl: string, maxResultCount: number) {
    this.http = http;
    this.baseUrl = baseUrl;
    this.maxResultCount = maxResultCount;
  }

  public getBaseUrl(): string {
    return this.baseUrl;
  }

  public getTools(): Observable<Tool[]> {
    return this.http.get<Tool[]>(`${this.baseUrl}/tools?limit=${this.maxResultCount}`);
  }

  public getDescriptorFrom(url: string): Observable<any> {
    return this.http.get(url, {responseType: 'text'});
  }
}
