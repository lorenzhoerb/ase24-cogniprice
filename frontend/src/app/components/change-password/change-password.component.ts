import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {AuthService} from '../../services/auth/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {SnackbarService} from '../../shared/utils/snackbar/snackbar.service';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatLabel,
    MatHint,
    MatInput,
    MatFormField,
    NgIf
  ],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.scss'
})
export class ChangePasswordComponent implements OnInit {
  changePasswordForm: FormGroup;
  token: string | null = '';
  passwordsMatch: boolean = true;

  constructor(private fb: FormBuilder,
              private authService: AuthService,
              private router: Router,
              private route: ActivatedRoute,
              private snackBarService: SnackbarService) {
    this.changePasswordForm = this.fb.group(
      {
        password: [
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
        validators: this.passwordMatchValidator // Custom validator for matching passwords
      }
    );
  }

  ngOnInit(): void {
    // Get the token from the URL
    this.token = this.route.snapshot.queryParamMap.get('token');
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password && confirmPassword && password === confirmPassword
      ? null
      : {mismatch: true};
  }

  onSubmit() {
    if (this.changePasswordForm.valid && this.token) {
      const {password, confirmPassword} = this.changePasswordForm.value;

      // Send the token and new password to the backend
      this.authService.changePassword(this.token, password).subscribe(
        (response) => {
          this.snackBarService.success('Password changed successfully');
          this.goToLogin();
        },
        (error) => {
          if (error.status === 404) {
            this.snackBarService.error('Invalid input. Token or user does not exist');
          } else if (error.status === 401) {
            this.snackBarService.error('Reset token is expired. Create a new one.');
          } else {
            this.snackBarService.error('An unexpected error occurred. Please try again.');
          }
        }
      );
    } else {
      console.log('Passwords do not match');
    }
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
