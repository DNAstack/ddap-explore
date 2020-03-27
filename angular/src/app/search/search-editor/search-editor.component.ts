import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';

@Component({
  selector: 'ddap-search-editor',
  templateUrl: './search-editor.component.html',
  styleUrls: ['./search-editor.component.scss'],
})
export class SearchEditorComponent implements OnInit, AfterViewInit {
  @ViewChild('editor', {static: false}) editor;
  @Input()
  searchText: string;

  @Input()
  editorOptions: any;

  @Output()
  doSearch: EventEmitter<string> = new EventEmitter<string>();

  private QUERY_EDITOR_DELIMITER = ';';
  private QUERY_EDITOR_NEWLINE = '\n';

  constructor() { }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    this.editor.setTheme('eclipse');
    this.editor.setMode('sql');
    this.editor.getEditor().setOptions({
      enableBasicAutocompletion: true,
      /*enableLiveAutocompletion: true*/
      // this introduces weird behaviour where multiple characters get typed with every keypressResults
    });

    this.editor.getEditor().commands.addCommand({
      name: 'showOtherCompletions',
      bindKey: 'Ctrl-.',
      exec: function (editor) {
      },
    });

    this.editor.getEditor().commands.addCommand({
      name: 'run',
      exec: (e) => {
        this.doSearchFromEditor();
      },
      bindKey: {mac: 'cmd-return', win: 'ctrl-enter'},
    });
  }

  addAtCursor(text: string) {
    if (this.editor != null) {
      const cursorPosition = this.editor.getEditor().session.doc.positionToIndex(this.editor.getEditor().selection.getCursor());

      if (cursorPosition > 0 && this.editor.getEditor().getValue()[cursorPosition - 1] === this.QUERY_EDITOR_DELIMITER) {
        text = this.QUERY_EDITOR_NEWLINE + text;
      }

      this.editor.getEditor().session.replace(this.editor.getEditor().selection.getRange(), text);
      this.editor.getEditor().focus();
    }
  }

  doSearchFromEditor() {
    this.doSearch.emit(this.getQueryFromEditor());
  }

  getQueryFromEditor() {

    let query = this.editor.getEditor().getValue();

    query = query.trim();
    query = query.replace('/' + this.QUERY_EDITOR_DELIMITER + '+$/', '');

    let cursorPosition = this.editor.getEditor()
      .session.doc.positionToIndex(this.editor.getEditor().selection.getCursor());
    if (cursorPosition >= query.length) {
      cursorPosition = query.length - 1;
    }

    while (query[cursorPosition] === this.QUERY_EDITOR_NEWLINE || query[cursorPosition] === ' ') {
      cursorPosition = cursorPosition - 1;
      if (cursorPosition === 0) {
        break;
      }
    }

    if (cursorPosition > 0 && query[cursorPosition - 1] === this.QUERY_EDITOR_DELIMITER) {
      cursorPosition = cursorPosition - 1;
    }

    let leftPosition = cursorPosition;

    while (leftPosition > 0) {
      if (query[leftPosition] === this.QUERY_EDITOR_DELIMITER && cursorPosition !== leftPosition) {
        leftPosition = leftPosition + 1;
        break;
      }
      leftPosition = leftPosition - 1;
    }

    let rightPosition = cursorPosition;
    while (rightPosition <= query.length + 1) {
      if (query[rightPosition] === this.QUERY_EDITOR_DELIMITER) {
        break;
      }
      rightPosition = rightPosition + 1;
    }

    query = query.substring(leftPosition, rightPosition).trim();

    return query;
  }

  queryChanged($event) {
  }
}
