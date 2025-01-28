import {Component, Input, OnInit} from '@angular/core';
import {UserDetailsService} from '../../services/user-details/user-details.service';
import {UserDetails} from '../../models/user-details';
import {MatDialog} from '@angular/material/dialog';
import {UserEditDialogueComponent} from './user-edit-modal/user-edit-modal.component';
import {MatGridList, MatGridTile} from "@angular/material/grid-list";
import {MatIcon} from "@angular/material/icon";
import {MatButton} from "@angular/material/button";
import {ConfirmationDialogComponent} from '../confirmation-dialog/confirmation-dialog.component';
import {Router} from '@angular/router';
import {SnackbarService} from '../../shared/utils/snackbar/snackbar.service';
import {AuthService} from '../../services/auth/auth.service';
import {WebhookService} from '../../services/webhook/webhook.service';
import {WebhookModalComponent} from './webhook-modal/webhook-modal.component';
import {ChangePasswordModalComponent} from './change-password-modal/change-password-modal.component';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [
    MatGridTile,
    MatIcon,
    MatGridList,
    MatButton
  ],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.scss'
})
export class ProfilePageComponent implements OnInit {

  @Input() id: number | undefined;
  userDetails: UserDetails | undefined;
  isWebhookTriggering = false;

  constructor(private userDetailsService: UserDetailsService,
              private dialog: MatDialog,
              private router: Router,
              private snackBarService: SnackbarService,
              private authService: AuthService,
              private webhookService: WebhookService) {
  }

  ngOnInit() {
    this.getUserFromLocalStorage();
  }


  getUserFromLocalStorage() {
    const userData = localStorage.getItem('user');
    if (userData) {
      this.userDetails = JSON.parse(userData);
    }
  }

  loadUserDetails() {
    this.userDetailsService.getUserDetails().subscribe({
      next: (userDetails) => {
        localStorage.setItem('user', JSON.stringify(userDetails));
        this.userDetails = userDetails;
      },
      error: error => {
        console.log("Error while fetching user by useranem");
      }
    })
  }


  openEditProfile() {
    // Blocking all page interactions until the dialogue is closed
    // document.body.classList.add('disable-interactions');

    const dialogRef = this.dialog.open(UserEditDialogueComponent, {
      width: '500px',
      data: this.userDetails, // Possibly change to dto or just pass the id and let it fetch.
      disableClose: true,
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadUserDetails();
      }
    })
  }

  deleteAccount() {
    const confirmDialog = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Delete Account',
        message: 'This action is irreversible. Are you sure you want to delete your account?'
      },
      width: '400px',
      height: 'auto',
      disableClose: true,
    });

    confirmDialog.afterClosed().subscribe((result) => {
      if (result === 'confirm') {
        this.userDetailsService.deleteCurrentUser().subscribe({
          next: () => {
            this.authService.logoutUser();
            this.router.navigate(['/login']);
            this.snackBarService.success('Account deleted successfully.');
          },
          error: error => {
            let errorMessage;
            if (typeof error.error === 'object') {
              errorMessage = error.error.error;
            } else {
              errorMessage = error.error;
            }
            console.log('Error deleting account: ', error);
            this.snackBarService.error('Could not delete account ' + errorMessage);
          }
        })
      }
    })
  }

  accessApi() {
    console.log("API");
    this.router.navigate(['/api-access']);
  }

  accessWebhook() {
    this.webhookService.getWebhook().subscribe({
      next: (webhook) => {
        this.openWebhookModal(webhook?.callbackUrl);
      },
      error: (err) => {
        if (err.status === 404) {
          console.log('No webhook registered for the user.');
          this.openWebhookModal(); // Open modal with empty URL for creation
        } else {
          console.error('Error fetching webhook:', err);
        }
      }
    });
  }

  openWebhookModal(url?: string): void {
    const dialogRef = this.dialog.open(WebhookModalComponent, {
      width: '400px',
      data: {url: url || ''} // Pass empty URL if creating a new webhook
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Determine the action performed and show appropriate message
        if (result.action === 'created') {
          this.snackBarService.success('Webhook created successfully!');
        } else if (result.action === 'updated') {
          this.snackBarService.success('Webhook updated successfully!');
        } else if (result.action === 'deleted') {
          this.snackBarService.success('Webhook deleted successfully!');
        }
      }
    });
  }

  triggerWebhook() {
    this.isWebhookTriggering = true;
    this.webhookService.triggerWebhookForUser().subscribe({
      next: (response) => {
        this.snackBarService.success(response); // Show success message
        this.isWebhookTriggering = false;
      },
      error: (error) => {
        this.snackBarService.error(error.error); // Show error message
        this.isWebhookTriggering = false;
      }
    });
  }

  openChangePasswordDialog() {
    const dialogRef = this.dialog.open(ChangePasswordModalComponent, {
      width: '500px',
      disableClose: true,
    })

    dialogRef.afterClosed().subscribe((result) => {
      console.log('The dialog was closed');
    })
  }
}

