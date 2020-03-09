import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ToolVersion } from '../../trs-v2/tool-version.model';
import { Tool } from '../../trs-v2/tool.model';
import { Client, TrsService } from '../../trs-v2/trs.service';

export interface Config {
  client: Client;
  acceptedToolClasses: string[];
  acceptedVersionDescriptorTypes: string[];
  pageSize: number;
}

@Component({
  selector: 'ddap-trs-browser',
  templateUrl: './trs-browser.component.html',
  styleUrls: ['./trs-browser.component.scss'],
})
export class TrsBrowserComponent implements OnInit {
  @Input()
  config: Config;

  pageIndex: number;

  updateInProgress = true;
  tools: Tool[];
  filteredTools: Tool[];
  filterTerm: string;

  constructor(private route: ActivatedRoute,
              private router: Router) {
    this.pageIndex = 0;
  }

  ngOnInit(): void {
    this.load();
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

  onVersionSelectionSelectClick(version: ToolVersion, type: string) {
    // const sourceUrl = `${version.url}/${type}/descriptor`;
    // this.router.navigate(
    //   ['..', 'run', btoa(sourceUrl)],
    //   {
    //     relativeTo: this.route,
    //   }
    // );

    // TODO Open a modal dialog for code editing
  }

  onNewVersionClick() {
    // TODO Pop up a modal dialog for code editing
  }

  getTotalPageNumber(): number {
    if (!this.filteredTools || this.filteredTools.length === 0) {
      return 0;
    }
    return Math.ceil(this.filteredTools.length / this.config.pageSize);
  }

  private load() {
    this.updateInProgress = true;
    this.config.client.getTools().subscribe(tools => {
      this.tools = tools.filter(t => {
        if (this.config.acceptedToolClasses.indexOf(t.toolclass.name) < 0) {
          return false;
        }

        for (const version of t.versions) {
          for (const descriptor_type of version.descriptor_type) {
            if (this.config.acceptedVersionDescriptorTypes.indexOf(descriptor_type) < 0) {
              return false;
            }
          }
        }

        return true;
      });

      this.filteredTools = this.tools;
      this.updateInProgress = false;

      this.reApplyFilter();
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
