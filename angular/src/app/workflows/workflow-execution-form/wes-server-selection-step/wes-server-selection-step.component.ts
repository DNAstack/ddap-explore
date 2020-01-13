import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { ResourceService } from '../../../shared/resource/resource.service';
import { SimplifiedWesResourceViews } from '../../workflow.model';
import { WorkflowService } from '../../workflows.service';


@Component({
  selector: 'ddap-wes-server-selection-step',
  templateUrl: './wes-server-selection-step.component.html',
  styleUrls: ['./wes-server-selection-step.component.scss'],
})
export class WesServerSelectionStepComponent implements OnInit {

  @Input()
  form: FormGroup;

  wesResourceViews: SimplifiedWesResourceViews[];

  constructor(private route: ActivatedRoute,
              private resourceService: ResourceService,
              private workflowService: WorkflowService) {
  }

  ngOnInit() {
    this.workflowService.getAllWesViews()
      .subscribe((sanitizedWesResourceViews: SimplifiedWesResourceViews[]) => {
        this.wesResourceViews = sanitizedWesResourceViews;
        const { viewId } = this.route.snapshot.params;
        if (viewId) {
          this.form.get('wesView').patchValue(viewId);
        }
      });
    this.form.get('wesView').valueChanges
      .subscribe(() => {
        this.form.get('wesViewResourcePath').patchValue(`${this.getDamId()};${this.getResourcePathForSelectedWesServer()}`);
      });
  }

  getDamId(): string {
    return this.wesResourceViews.find((wesResourceViews: SimplifiedWesResourceViews) => {
      return wesResourceViews.views.some((view) => view.name === this.form.get('wesView').value);
    }).damId;
  }

  private getResourcePathForSelectedWesServer() {
    return this.workflowService.getResourcePathForView(this.getDamId(), this.form.get('wesView').value, this.wesResourceViews);
  }

}
