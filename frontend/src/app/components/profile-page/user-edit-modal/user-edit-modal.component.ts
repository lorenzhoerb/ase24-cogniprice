import {Component, Inject, ViewContainerRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ConfirmationDialogComponent} from '../../confirmation-dialog/confirmation-dialog.component';
import {EditUserDto} from '../../../dtos/editUserDto';
import {UserDetailsService} from '../../../services/user-details/user-details.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AuthService} from '../../../services/auth/auth.service';
import {Router} from '@angular/router';
import {SnackbarService} from '../../../shared/utils/snackbar/snackbar.service';


@Component({
  selector: 'app-user-edit-dialogue',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, ReactiveFormsModule],
  templateUrl: './user-edit-modal.component.html',
  styleUrl: './user-edit-modal.component.scss'
})
export class UserEditDialogueComponent {
  editUserForm: FormGroup;
  errorMessage = '';
  error = false;
  isSubmitted = false;

  constructor(private dialog: MatDialog,
              public dialogRef: MatDialogRef<UserEditDialogueComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private fb: FormBuilder,
              private userService: UserDetailsService,
              private authService: AuthService,
              private router: Router,
              private snackBarService: SnackbarService
  ) {
    this.editUserForm = this.fb.group({
      username: [data.username, [Validators.required, Validators.pattern(/^[a-zA-Z0-9_]{3,15}$/)]],
      email: [data.email, [Validators.required, Validators.email]],
      firstName: [data.firstName, [Validators.required, Validators.pattern(/^[a-zA-Z]*$/)]],
      lastName: [data.lastName, [Validators.required, Validators.pattern(/^[a-zA-Z]*$/)]]
    });
  }

  saveChanges() {
    if (this.editUserForm.valid) {
      const editUserDto: EditUserDto = {
        email: this.editUserForm.controls['email'].value,
        username: this.editUserForm.controls['username'].value,
        firstName: this.editUserForm.controls['firstName'].value,
        lastName: this.editUserForm.controls['lastName'].value,
      }
      this.updateUser(editUserDto);
    } else {
      alert('Please fill in all fields correctly.');
    }
  }

  updateUser(editUserDto: EditUserDto) {
    this.userService.editUser(editUserDto).subscribe({
      next: (response : string | void)=> {
        this.snackBarService.success('User successfully updated!')

        if (typeof response === 'string' && response.trim() !== '') {
          this.authService.updateToken(response);
        }
        this.dialogRef.close("confirm");
      },
      error: error => {
        console.log(error);
        if (error.status == 422) {
          this.snackBarService.error('Failed to update user. Check the form for errors.');
        } else {
          this.snackBarService.error('Could not edit profile: ' + error.error);
        }
      }
    })
  }

  onCancel() {
    if (this.editUserForm.dirty) {
      const confirmDialog = this.dialog.open(ConfirmationDialogComponent, {
        data: {
          title: 'Abort Confirmation',
          message: 'Are you sure you want to abort?'
        },
        width: '400px',
        height: 'auto',
        disableClose: true,
      });

      confirmDialog.afterClosed().subscribe((result) => {
        if (result === 'confirm') {
          this.dialogRef.close();
        }
      });
    } else {
      this.dialogRef.close();
    }
  }
}
