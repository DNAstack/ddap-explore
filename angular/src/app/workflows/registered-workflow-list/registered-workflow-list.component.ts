import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AppConfigModel, FrontendAppWorkflowsConfig } from '../../shared/app-config/app-config.model';
import { AppConfigStore } from '../../shared/app-config/app-config.store';
import { TrsService } from '../trs-v2/trs.service';

import { Config } from './trs-browser/trs-browser.component';

@Component({
  selector: 'ddap-registered-workflow-list',
  templateUrl: 'registered-workflow-list.component.html',
  styleUrls: ['registered-workflow-list.component.scss'],
})
export class RegisteredWorkflowListComponent implements OnInit {

  workflowsConfig: FrontendAppWorkflowsConfig;
  publicBrowserConfig: Config;

  constructor(private route: ActivatedRoute,
              private appConfigStore: AppConfigStore,
              private router: Router,
              private trs: TrsService) {
  }

  ngOnInit(): void {
    // Ensure that the user can only access this component when it is enabled.
    this.appConfigStore.state$.subscribe((appConfig: AppConfigModel) => {
      this.workflowsConfig = appConfig.apps.workflows;

      if (!this.workflowsConfig.trsBaseUrl) {
        return;
      }

      this.publicBrowserConfig = {
        client: this.trs.endpoint(this.workflowsConfig.trsBaseUrl),
        acceptedToolClasses: this.workflowsConfig.trsAcceptedToolClasses,
        acceptedVersionDescriptorTypes: this.workflowsConfig.trsAcceptedVersionDescriptorTypes,
        pageSize: appConfig.listPageSize,
        editable: false,
      };
    });
  }
}
