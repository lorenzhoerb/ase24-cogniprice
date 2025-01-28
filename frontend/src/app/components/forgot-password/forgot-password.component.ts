import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {NgIf} from '@angular/common';
import {AuthService} from '../../services/auth/auth.service';
import {Router} from '@angular/router';
import {UserDetailsService} from '../../services/user-details/user-details.service';
import {SnackbarService} from '../../shared/utils/snackbar/snackbar.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormField,
    MatLabel,
    MatHint,
    MatInput,
    NgIf
  ],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;

  constructor(private fb: FormBuilder,
              private authService: AuthService,
              private router: Router,
              private snackBarService: SnackbarService) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  onSubmit() {
    if(this.forgotPasswordForm.valid) {
      const email = this.forgotPasswordForm.value.email;
      this.authService.resetPassword(email).subscribe(
        (response) => {
          this.snackBarService.success('Email to reset password successfully sent');
          this.goToLogin();
        },
        (error) => {
          if (error.status === 404) {
            this.snackBarService.error('Invalid input. User does not exist');
          } else {
            this.snackBarService.error('An unexpected error occurred. Please try again.');
          }
        });
    }
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  goToSignup() {
    this.router.navigate(['/registration']);
  }
}
