import {Component} from '@angular/core';
import {
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatInput} from '@angular/material/input';
import {NgIf} from '@angular/common';
import {MatButton} from '@angular/material/button';
import {AuthService} from '../../../services/auth/auth.service';
import {UserDetailsService} from '../../../services/user-details/user-details.service';
import {SnackbarService} from '../../../shared/utils/snackbar/snackbar.service';

@Component({
  selector: 'app-change-password-dialog',
  standalone: true,
  imports: [
    MatDialogActions,
    MatError,
    MatLabel,
    MatFormField,
    MatDialogContent,
    ReactiveFormsModule,
    MatInput,
    NgIf,
    MatDialogTitle,
    MatButton,
    MatDialogClose
  ],
  templateUrl: './change-password-modal.component.html',
  styleUrl: './change-password-modal.component.scss'
})
export class ChangePasswordModalComponent {
  changePasswordForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private userService: UserDetailsService,
    private snackbarService: SnackbarService,
    private dialogRef: MatDialogRef<ChangePasswordModalComponent>

  ) {
    this.changePasswordForm = this.fb.group(
      {
        oldPassword: [
          '',
          [
            Validators.required,
          ]
        ],
        newPassword: [
          '',
          [
            Validators.required,
            Validators.minLength(8),
            Validators.pattern(/^(?=.*[!@#$%^&+=])[a-zA-Z0-9!@#$%^&+=]*$/)
          ]
        ],
        confirmPassword: ['', [Validators.required]]
      },
      {
        validators: this.passwordMatchValidator
      }
    );
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const password = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password && confirmPassword && password === confirmPassword
      ? null
      : {mismatch: true};
  }

  onSubmit() {
    if (this.changePasswordForm.valid) {
      const {oldPassword, newPassword} = this.changePasswordForm.value;
      this.userService.changePassword(oldPassword, newPassword).subscribe({
        next: () => {
          // Handle success: show a confirmation message to the user
          this.snackbarService.success('Password changed successfully!');
          this.dialogRef.close();
        },
        error: (err) => {
          // Handle error: display an error message based on the response
          if (err.status === 400) {
            this.snackbarService.error('Invalid request. Please check your input.');
          } else if (err.status === 401) {
            this.snackbarService.error('Incorrect old password.');
          } else if (err.status === 404) {
            this.snackbarService.error('User does not exist. How did you get here?');}
          else {
            this.snackbarService.error('An unexpected error occurred. Please try again.');
          }
          console.error('Change password failed:', err);
        }
      });
    }
  }
}
