import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ConfirmLogoutComponent } from '../confirm-logout/confirm-logout.component';

@Component({
  selector: 'app-logout-button',
  templateUrl: './logout-button.component.html',
  standalone: true,
  styleUrls: ['logout-button.component.scss']
})
export class LogoutButtonComponent {
  constructor(private dialog: MatDialog, private router: Router) {}

  logout() {
    const dialogRef = this.dialog.open(ConfirmLogoutComponent, {
      width: '300px',
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // User confirmed logout
        localStorage.removeItem('authToken');
        this.router.navigate(['/login']);
      }
    });
  }
}
