import { Pipe, PipeTransform } from '@angular/core';

@Pipe({name: 'objectToArray'})
export class ObjectToArrayPipe implements PipeTransform {
  transform(value: any, propertyOrder: string[]): string[] {
    const arr = [];
    for (let i = 0; i < propertyOrder.length; i++) {
      arr.push(value[propertyOrder[i]]);
    }
    return arr;
  }
}
