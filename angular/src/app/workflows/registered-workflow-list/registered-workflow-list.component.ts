import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { TrsService } from '../trs-v2/trs.service';

import { Config } from './trs-browser/trs-browser.component';

@Component({
  selector: 'ddap-registered-workflow-list',
  templateUrl: 'registered-workflow-list.component.html',
  styleUrls: ['registered-workflow-list.component.scss'],
})
export class RegisteredWorkflowListComponent implements OnInit {
  appConfig: AppConfigModel;
  publicBrowserConfig: Config;

  constructor(private route: ActivatedRoute,
              private appConfigService: AppConfigService,
              private router: Router,
              private trs: TrsService) {
  }

  ngOnInit(): void {
    // Ensure that the user can only access this component when it is enabled.
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      this.appConfig = data;

      if (!this.appConfig.trsBaseUrl) {
        return;
      }

      if (this.appConfig.featureWorkflowsEnabled) {
        this.publicBrowserConfig = {
          client: this.trs.from(this.appConfig.trsBaseUrl),
          acceptedToolClasses: this.appConfig.trsAcceptedToolClasses,
          acceptedVersionDescriptorTypes: this.appConfig.trsAcceptedVersionDescriptorTypes,
          pageSize: this.appConfig.listPageSize,
        };
      } else {
        this.router.navigate(['/']);
      }
    });
  }
}
