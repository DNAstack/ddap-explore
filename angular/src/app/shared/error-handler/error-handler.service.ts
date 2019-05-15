import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material';
import { OperatorFunction } from 'rxjs/interfaces';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ErrorHandlerService {

  constructor(private snackBar: MatSnackBar) {

  }

  notifyOnError<T>(message?: string): OperatorFunction<T, T> {
    return catchError<T, T>((error) => {
      this.openSnackBar(error, message);
      throw error;
    });
  }

  private openSnackBar(error, message) {
    this.snackBar.open(message ? message : error.message, null, {
      duration: 3000,
      panelClass: 'ddap-error',
    });
  }
}