import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { DefinitionService } from '../definitions.service';

@Component({
  selector: 'ddap-definition-manage',
  templateUrl: './definition-manage.component.html',
  styleUrls: ['./definition-manage.component.scss'],
})
export class DefinitionManageComponent {

  constructor(private definitionService: DefinitionService,
              private router: Router) { }

  onSubmit(value: any) {
    this.definitionService.save(JSON.parse(value.body))
      .subscribe(path => this.router.navigate([path]));
  }
}
