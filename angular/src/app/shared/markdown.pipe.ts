import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import showdown from 'showdown';

@Pipe({name: 'markdown'})
export class MarkdownPipe implements PipeTransform {
  constructor(protected sanitizer: DomSanitizer) {}

  transform(value: string): SafeHtml {
    const converter = new showdown.Converter();
    const compiled = converter.makeHtml(value).replace(/<a /g, '<a target="_blank" ');
    return this.sanitizer.bypassSecurityTrustHtml(compiled);
  }
}
