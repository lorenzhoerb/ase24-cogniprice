import { Injectable } from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class SnackbarService {

  constructor(private snackBar: MatSnackBar) { }

  error(message: string) {
    this.openSnackbar(message, false);
  }

  success(message: string) {
    this.openSnackbar(message, true);
  }

  private openSnackbar(message: string, success: boolean) {
    let panelClassText: string = 'snackbar-success'
    const displayDuration = 4000; // Duration for which the snackbar is displayed
    const fadeOutDuration = 500; // Fade-out animation duration
    const totalDuration = displayDuration + fadeOutDuration;

    if (!success) {
      panelClassText = 'snackbar-error'
    }
    this.snackBar.open(message, 'Close', {
      duration: totalDuration,
      panelClass: [panelClassText],
    })

    setTimeout(() => {
      const snackBarElement = document.querySelector('.mat-mdc-snack-bar-container');
      if (snackBarElement) {
        snackBarElement.classList.add('fade-out');
      }
    }, displayDuration);

  }
}
