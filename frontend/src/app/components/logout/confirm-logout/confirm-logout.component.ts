import { Component } from '@angular/core';
import {MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';

@Component({
  selector: 'app-confirm-logout',
  templateUrl: './confirm-logout.component.html',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule], // Import Material modules
  styleUrls: ['confirm-logout.component.scss']
})
export class ConfirmLogoutComponent {
  constructor(public dialogRef: MatDialogRef<ConfirmLogoutComponent>) {}

  confirm(): void {
    this.dialogRef.close(true); // Close the dialog and return true
  }

  cancel(): void {
    this.dialogRef.close(false); // Close the dialog and return false
  }
}
