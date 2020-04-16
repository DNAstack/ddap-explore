import { Pipe, PipeTransform } from '@angular/core';
import linkifyStr from 'linkifyjs/string';

@Pipe({name: 'linkify'})
export class LinkifyPipe implements PipeTransform {
  transform(str: string): string {
    if (str === 'null' || str === null || str === undefined) {
      return '<span class="null-value">null</span>';
    }
    return str ? linkifyStr(str, {target: '_blank'}) : str;
  }
}
