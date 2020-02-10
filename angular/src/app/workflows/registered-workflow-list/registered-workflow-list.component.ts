import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import Tool from '../trs-v2/tool.model';
import { TrsService } from '../trs-v2/trs.service';

@Component({
  selector: 'ddap-registered-workflow-list',
  templateUrl: 'registered-workflow-list.component.html',
  styleUrls: ['registered-workflow-list.component.scss'],
})
export class RegisteredWorkflowListComponent implements OnInit {
  appConfig: AppConfigModel;
  acceptedToolClasses: string[];
  acceptedVersionDescriptorTypes: string[];
  tools: Tool[];
  filteredTools: Tool[];
  filterTerm: string;
  pageSize: number;
  pageIndex: number;

  constructor(private appConfigService: AppConfigService,
              private router: Router,
              private dialog: MatDialog,
              private trs: TrsService) {
    // FIXME Some of these default values must be configurable via the BFF.
    this.acceptedToolClasses = ['Workflow'];  // TODO ui.trs-accepted.tool-classes
    this.acceptedVersionDescriptorTypes = ['WDL'];  // TODO ui.trs-accepted-version-descriptor-types
    this.pageSize = 14;  // TODO ui.list.page-size
    this.pageIndex = 0;  // NOTE Not configurable
  }

  ngOnInit(): void {
    // Ensure that the user can only access this component when it is enabled.
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      this.appConfig = data;
      if (data.featureWorkflowsEnabled) {
        this.initialize();
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  onFilterUpdate(filterTerm: string) {
    this.filterTerm = filterTerm;
    this.reApplyFilter();
  }

  onFilterResetClick() {
    this.filterTerm = null;
    this.reApplyFilter();
  }

  onFilterRefreshClick() {
    this.load();
  }

  onPagerClick(by: number) {
    this.pageIndex += by;
    if (this.pageIndex < 0) {
      this.pageIndex = 0;
    } else if (this.pageIndex >= this.getTotalPageNumber()) {
      this.pageIndex = this.getTotalPageNumber() - 1;
    }
  }

  getTotalPageNumber(): number {
    if (!this.filteredTools || this.filteredTools.length === 0) {
      return 0;
    }
    return Math.ceil(this.filteredTools.length / this.pageSize);
  }

  private initialize() {
    this.load();
  }

  private load() {
    this.trs.getTools().subscribe(tools => {
      this.tools = tools.filter(t => {
        if (this.acceptedToolClasses.indexOf(t.toolclass.name) < 0) {
          return false;
        }

        for (const version of t.versions) {
          for (const descriptor_type of version.descriptor_type) {
            if (this.acceptedVersionDescriptorTypes.indexOf(descriptor_type) < 0) {
              return false;
            }
          }
        }

        return true;
      });

      this.filteredTools = this.tools;
    });
  }

  private reApplyFilter() {
    this.pageIndex = 0;

    // Reset the filtered list.
    if (!this.filterTerm || this.filterTerm.length === 0) {
      this.filteredTools = this.tools;
      return;
    }

    const filteringPattern = new RegExp(this.filterTerm, 'i');

    this.filteredTools = this.tools.filter(t => {
      return (
        t.toolname.match(filteringPattern)
        || t.author.match(filteringPattern)
      );
    });
  }
}
