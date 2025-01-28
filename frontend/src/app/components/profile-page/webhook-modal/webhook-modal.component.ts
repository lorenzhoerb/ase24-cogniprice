import {Component, Inject} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatError, MatFormField, MatFormFieldModule} from '@angular/material/form-field';
import {WebhookService} from '../../../services/webhook/webhook.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatInputModule} from '@angular/material/input';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-webhook-modal',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    ReactiveFormsModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormField,
    MatError,
    NgIf
  ],
  templateUrl: './webhook-modal.component.html',
  styleUrl: './webhook-modal.component.scss'
})
export class WebhookModalComponent {
  webhookForm: FormGroup;
  isCreating: boolean = false; // To distinguish between creation and update
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private webhookService: WebhookService,
    private dialogRef: MatDialogRef<WebhookModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { url?: string } // Inject existing webhook data
  ) {
    this.isCreating = !data?.url; // If no URL is provided, we are in creation mode

    this.webhookForm = this.fb.group({
      url: [data?.url || '', [Validators.required, Validators.pattern('(https?:\/\/.*)')]],
      secret: [''] // Optional field for webhook creation
    });
  }

  createWebhook(): void {
    this.errorMessage = null;
    if (this.webhookForm.valid) {
      const { url } = this.webhookForm.value;
      this.webhookService.createWebhook({ url }).subscribe({
        next: () => {
          console.log('Webhook created successfully');
          this.dialogRef.close({ action: 'created' });
        },
        error: (err) => {
          console.error('Error creating webhook:', err);
          this.errorMessage = err.error;
        }
      });
    }
  }

  updateWebhook(): void {
    this.errorMessage = null;
    if (this.webhookForm.valid) {
      const { url } = this.webhookForm.value;
      this.webhookService.updateWebhook(url).subscribe({
        next: () => {
          console.log('Webhook updated successfully');
          this.dialogRef.close({ action: 'updated' });
        },
        error: (err) => {
          console.error('Error updating webhook:', err);
          this.errorMessage = err.error;
        }
      });
    }
  }

  deleteWebhook(): void {
    this.errorMessage = null; // Reset error message
    this.webhookService.deleteWebhook().subscribe({
      next: () => {
        console.log('Webhook deleted successfully');
        this.dialogRef.close({ action: 'deleted' });
      },
      error: (err) => {
        console.error('Error deleting webhook:', err);
        this.errorMessage = err.error;
      }
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
