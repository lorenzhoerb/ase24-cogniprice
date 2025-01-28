import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import {CommonModule} from '@angular/common';
import {AuthUserDto} from '../../dtos/authUserDto';
import {AuthService} from '../../services/auth/auth.service';
import {Router} from '@angular/router';
import {UserDetailsService} from '../../services/user-details/user-details.service';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.component.html',
  imports: [ReactiveFormsModule, CommonModule, MatFormFieldModule, MatInputModule],
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';

  constructor(private fb: FormBuilder,
              private authService: AuthService,
              private router: Router,
              private userDetailsService: UserDetailsService) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9_]{3,15}$/)]],
      password: ['', [Validators.required, Validators.minLength(7)]]
    });
  }
  onSubmit() {
    if (this.loginForm.valid) {
      const authRequest: AuthUserDto = {
        username:  this.loginForm.value.username,
        password: this.loginForm.value.password
      }
      this.authService.loginUser(authRequest).subscribe({
        next: (response) => {
          this.userDetailsService.getUserDetailsByUsername(authRequest.username).subscribe({
            next: userDetails => {
              localStorage.setItem('user', JSON.stringify(userDetails));
              this.router.navigate(['/dashboard']);
            }, error: error => {
              console.log("Error while fetching user by username");
            }
          })
      },
        error: (error) => {
          console.log("Error at login: ", error.error);
          // Handle error based on the response
          if (error.status === 401) {
            if (error.error.includes('Password or Username incorrect')) {
              this.errorMessage = 'Invalid username or password.';
            } else if (error.error.includes('User')) {
              this.errorMessage = 'Username not found.';
            }
          } else if (error.status === 423) {
            this.errorMessage = 'Your account is locked. Please contact support.';
          } else {
            this.errorMessage = 'An unexpected error occurred. Please try again later.';
          }
      }
    })
    } else {
      alert('Please fill in all fields correctly.');
    }
  }

  decodeJWTToken(token: string){
    return JSON.parse(atob(token.split(".")[1]))
  }

  goToSignup() {
    this.router.navigate(['/registration']);

  }

  goToForgotPassword() {
    this.router.navigate(['/forgot-password']);
  }
}
