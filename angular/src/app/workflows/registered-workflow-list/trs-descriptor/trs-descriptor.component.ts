import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { editor } from 'monaco-editor';

import { ToolVersion } from '../../trs-v2/tool-version.model';
import { Client } from '../../trs-v2/trs.service';
import ICodeEditor = editor.ICodeEditor;

@Component({
  selector: 'ddap-trs-descriptor',
  templateUrl: './trs-descriptor.component.html',
  styleUrls: ['./trs-descriptor.component.scss'],
})
export class TrsDescriptorComponent implements OnInit {
  sample = 'task md5 {\n' +
    '  File inputFile\n' +
    '\n' +
    '  command {\n' +
    '    /bin/my_md5sum ${inputFile}\n' +
    '  }\n' +
    '\n' +
    ' output {\n' +
    '    File value = "md5sum.txt"\n' +
    ' }\n' +
    '\n' +
    ' runtime {\n' +
    '   docker: "quay.io/briandoconnor/dockstore-tool-md5sum:1.0.2"\n' +
    '   cpu: 1\n' +
    '   memory: "512 MB"\n' +
    ' }\n' +
    '}\n' +
    '\n' +
    'workflow ga4ghMd5 {\n' +
    ' File inputFile\n' +
    ' call md5 { input: inputFile=inputFile }\n' +
    '}\n';
  editor: ICodeEditor;
  descriptorContent: string;
  editorConfig = {
    language: 'wdl',
    theme: 'vs-dark',
    minimap: {enabled: false},
  };

  constructor(public dialogRef: MatDialogRef<TrsDescriptorComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData) {
  }

  ngOnInit() {
    this.descriptorContent = this.sample;
    // const sourceUrl = `${this.data.version.url}/${this.data.type}/descriptor`;
    // this.data.client.getDescriptorFrom(sourceUrl).subscribe(content => {
    //   this.descriptorContent = content;
    // });
  }

  onEditorInit(codeEditor: ICodeEditor) {
    this.editor = codeEditor;

    const monaco = (<any> window).monaco;
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
      keywords: [
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
          [/[a-zA-Z_]\w*/, {
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

interface DialogData {
  client: Client;
  version: ToolVersion;
  type: string;
}
