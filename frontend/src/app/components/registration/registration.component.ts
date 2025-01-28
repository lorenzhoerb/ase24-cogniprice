import { Component } from '@angular/core';
import { Validators, FormBuilder, UntypedFormGroup, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth/auth.service';
import { CommonModule } from '@angular/common';
import { RegistrationUserDto } from '../../dtos/registrationUserDto';
import {Router} from '@angular/router';
import {UserDetailsService} from '../../services/user-details/user-details.service';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';

@Component({
  selector: 'app-registration',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule],
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.scss'
})
export class RegistrationComponent {

  registrationForm: UntypedFormGroup;
  passwordsMatch = false;
  errorMessage = '';
  error = false;
  isSubmitted = false;

  constructor(private formBuilder: FormBuilder,
              private authService: AuthService,
              private router: Router,
              private userDetailsService: UserDetailsService) {
      this.registrationForm = this.formBuilder.group({
        username: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9_]{3,15}$/)]],
        password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[!@#$%^&+=])[a-zA-Z0-9!@#$%^&+=]*$/)]],
        passwordConfirmation: ['', [Validators.required, Validators.minLength(7), Validators.pattern(/^(?=.*[!@#$%^&+=])[a-zA-Z0-9!@#$%^&+=]*$/)]], // Validators.pattern()
        firstName: ['', [Validators.required, Validators.pattern(/^[a-zA-Z]*$/)]],
        lastName: ['', [Validators.required, Validators.pattern(/^[a-zA-Z]*$/)]],
        email: ['', [Validators.required, Validators.email]],
      });
  }

  onSubmit(): void{
    this.isSubmitted = true;

    if (this.registrationForm.controls['password'].value != this.registrationForm.controls['passwordConfirmation'].value) {
      this.passwordsMatch = false;
      return;
    }

    if (this.registrationForm.valid) {
      const registrationUserDto: RegistrationUserDto = {
        email: this.registrationForm.controls['email'].value,
        password: this.registrationForm.controls['password'].value,
        username: this.registrationForm.controls['username'].value,
        firstName: this.registrationForm.controls['firstName'].value,
        lastName: this.registrationForm.controls['lastName'].value,
      }
      this.registerUser(registrationUserDto);
    } else {
      alert('Please fill in all fields correctly.');
    }
  }

  registerUser(registrationUserDto: RegistrationUserDto) {
      console.log("Try register user...");
      this.authService.registerUser(registrationUserDto).subscribe({
        next: () => {
          console.log("Successful user creation: ", registrationUserDto.email);
          this.userDetailsService.getUserDetailsByUsername(registrationUserDto.username).subscribe({
            next: userDetails => {
              localStorage.setItem('user', JSON.stringify(userDetails));
              this.router.navigate(['/dashboard']);
            },
            error: error => {
              console.log("Error while fetching user by username");
            }
          })

        }, error: error => {
          console.log("Error at registration: ", error.error);
          this.error = true;
          if (error.status == 409) {
            this.errorMessage = error.error;
          } else {
            this.errorMessage = 'An unexpected error has occured. Please try again later.';
          }
        }
      })
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
