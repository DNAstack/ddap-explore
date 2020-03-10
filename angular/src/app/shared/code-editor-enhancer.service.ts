import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CodeEditorEnhancerService {
  configure(language: string) {
    const monaco = (<any> window).monaco;

    // Based on https://github.com/microsoft/monaco-languages/tree/master/src

    if (language === 'wdl') {
      monaco.languages.register({id: 'wdl'});
      monaco.languages.setLanguageConfiguration('wdl', {
        brackets: [
          ['{', '}'],
          ['[', ']'],
          ['(', ')'],
        ],
        comments: {
          lineComment: '#',
        },
        wordPattern: /(-?\d*\.\d\w*)|([^\`\~\!\#\%\^\&\*\(\)\-\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g,
      });
      monaco.languages.setMonarchTokensProvider('wdl', {
        defaultToken: '',
        tokenPostfix: '.wdl',

        keywords: [
          'version',
          'task',
          'input',
          'command',
          'runtime',
          'output',
          'workflow',
          'call',
        ],

        symbols: /[=><!~?:&|+\-*\/\^%]+/,
        digits: /\d+(_+\d+)*/,
        escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,

        brackets: [
          {open: '{', close: '}', token: 'delimiter.curly'},
          {open: '[', close: ']', token: 'delimiter.bracket'},
          {open: '(', close: ')', token: 'delimiter.parenthesis'},
        ],

        tokenizer: {
          root: [
            {include: '@whitespace'},
            {include: '@strings'},
            {include: '@numbers'},
            [/[,:;]/, 'delimiter'],
            [/[{}\[\]()]/, '@brackets'],
            [/@symbols/, 'delimiter'],
            [/[a-zA-Z_]+/, {
              cases: {
                '@keywords': 'keyword',
                '@default': 'identifier',
              },
            }],
          ],

          numbers: [
            [/-?0x([abcdef]|[ABCDEF]|\d)+[lL]?/, 'number.hex'],
            [/-?(\d*\.)?\d+([eE][+\-]?\d+)?[jJ]?[lL]?/, 'number'],
          ],

          strings: [
            [/'$/, 'string.escape', '@popall'],
            [/'/, 'string.escape', '@stringBody'],
            [/"$/, 'string.escape', '@popall'],
            [/"/, 'string.escape', '@dblStringBody'],
          ],

          whitespace: [
            [/\s+/, 'white'],
            [/(^#.*$)/, 'comment'],
          ],

          stringBody: [
            [/[^\\']+$/, 'string', '@popall'],
            [/[^\\']+/, 'string'],
            [/\\./, 'string'],
            [/'/, 'string.escape', '@popall'],
            [/\\$/, 'string'],
          ],

          dblStringBody: [
            [/[^\\"]+$/, 'string', '@popall'],
            [/[^\\"]+/, 'string'],
            [/\\./, 'string'],
            [/"/, 'string.escape', '@popall'],
            [/\\$/, 'string'],
          ],
        },
      });
    }
  }
}
