import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';

import { ChangeDto, ItemDto } from '../../shared/change.dto';
import { JsonEditorDefaults } from '../../shared/jsonEditorDefaults';
import { ResourceService } from '../resources.service';

@Component({
  selector: 'ddap-resource-manage',
  templateUrl: './resource-manage.component.html',
  styleUrls: ['./resource-manage.component.scss'],
})
export class ResourceManageComponent implements OnInit {

  errorDto: any;
  entity: ChangeDto;

  errorEditorOptions: JsonEditorOptions;
  itemEditorOptions: JsonEditorOptions;

  @ViewChild('itemEditor')
  entityEditor: JsonEditorComponent;

  @ViewChild('errorEditor')
  errorEditor: JsonEditorComponent;

  constructor(public resourceService: ResourceService,
              private router: Router,
              private route: ActivatedRoute) {
    this.itemEditorOptions = new JsonEditorDefaults();
    this.errorEditorOptions = new JsonEditorDefaults();
    this.itemEditorOptions.mode = 'code';
    this.errorEditorOptions.mode = 'code';
    this.errorEditorOptions.onEditable = () => false;
    this.entity = new ChangeDto(new ItemDto(undefined), {});
  }

  ngOnInit() {
  }

  save() {
    this.resourceService.saveDto(this.entity).subscribe(path => {
        this.router.navigate(['../..'], { relativeTo: this.route });
      },
      (errorDto) => {
        this.errorDto = errorDto.error;
      });
  }

  updateItemDto(event: any) {
    this.entity = event;
  }
}
