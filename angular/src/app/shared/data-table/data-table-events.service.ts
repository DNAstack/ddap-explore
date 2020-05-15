import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable()
export class DataTableEventsService {
  deselectRowsEvents: Subject<void> = new Subject<void>();

  deselectRows(): void {
    this.deselectRowsEvents.next();
  }
}
