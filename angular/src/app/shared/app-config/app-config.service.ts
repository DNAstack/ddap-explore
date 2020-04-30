import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { share } from 'rxjs/operators';

import { environment } from '../../../environments/environment';

import { AppConfigModel } from './app-config.model';

@Injectable({
  providedIn: 'root',
})
export class AppConfigService {

  constructor(private http: HttpClient) {
  }

  getConfig(): Observable<AppConfigModel> {
    return this.http.get<AppConfigModel>(`${environment.ddapAlphaApiUrl}/config`)
      .pipe(share());
  }

}
