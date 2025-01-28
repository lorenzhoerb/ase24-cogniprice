import {Component, Inject} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
} from "@angular/material/dialog";

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
    imports: [
      MatDialogModule, MatButton
    ],
  templateUrl: './confirmation-dialog.component.html',
  styleUrl: './confirmation-dialog.component.scss'
})
export class ConfirmationDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data : {
    title: string,
    message: string
  }) {
  }
}
