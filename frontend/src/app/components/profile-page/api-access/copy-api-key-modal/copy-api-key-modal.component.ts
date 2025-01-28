import {Component, Inject} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import {MatFormField} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-copy-api-key-modal',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatFormField,
    MatIcon,
    MatDialogActions,
    MatButton,
    MatIconButton,
    MatInput,
    NgIf
  ],
  templateUrl: './copy-api-key-modal.component.html',
  styleUrl: './copy-api-key-modal.component.scss'
})
export class CopyApiKeyModalComponent {
  copied = false;

  constructor(
    public dialogRef: MatDialogRef<CopyApiKeyModalComponent>,
    @Inject(MAT_DIALOG_DATA) public apiKey: string
  ) {
  }

  copyToClipboard(): void {
    navigator.clipboard.writeText(this.apiKey).then(() => {
      this.copied = true; // Show the copied message
      setTimeout(() => {
        this.copied = false; // Hide the copied message after 2 seconds
      }, 2000);
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
