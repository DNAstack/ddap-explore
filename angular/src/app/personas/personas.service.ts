import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { ConfigEntityService } from '../shared/ConfigEntityService';

@Injectable({
  providedIn: 'root',
})
export class PersonaService extends ConfigEntityService {

  constructor(http: HttpClient) {
    super(http, 'testPersonas');
  }

}