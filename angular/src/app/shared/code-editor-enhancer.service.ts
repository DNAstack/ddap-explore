import { Injectable } from '@angular/core';
import LanguageConfiguration = monaco.languages.LanguageConfiguration;
import IMonarchLanguage = monaco.languages.IMonarchLanguage;

@Injectable({
  providedIn: 'root',
})
export class CodeEditorEnhancerService {
  constructor() {
  }

  configure(config: Configuration) {
    const monaco = (<any> window).monaco;
    // Based on https://github.com/microsoft/monaco-languages/tree/master/src

    if (config.language === 'wdl') {
      let languageConfig: LanguageConfiguration = null;
      let languageDefinition: any = null;

      languageConfig = {
        comments: {
          lineComment: '#',
        },
        wordPattern: /(-?\d*\.\d\w*)|([^\`\~\!\#\%\^\&\*\(\)\-\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g,
      };

      // NOTE When this feature is off, auto-indentation is also off for some reasons.
      if (config.autoClosingPairs) {
        languageConfig.brackets = [
          ['{', '}'],
          ['[', ']'],
          ['(', ')'],
        ];
      }

      // FIXME Properly implement WDL language spec.
      languageDefinition = {
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
            // [/>>>$/, 'string.escape', '@popall'],
            // [/<<</, 'string.escape', '@tripleStringBody'],
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

          // tripleStringBody: [
          //   [/.+/, 'string'],
          //   [/>>>$/, 'string.escape', '@popall'],
          // ],
        },
      };

      monaco.languages.register({id: 'wdl'});
      monaco.languages.setLanguageConfiguration('wdl', languageConfig);
      monaco.languages.setMonarchTokensProvider('wdl', languageDefinition);
    }
  }
}

export interface Configuration {
  language?: string;
  autoClosingPairs?: boolean;
  // FIXME ↑ On the UI, the user can choose to turn this feature on and stored the preference in the local storage.
  //  By default, it is off to accommodate Selenium WebDriver.
}
