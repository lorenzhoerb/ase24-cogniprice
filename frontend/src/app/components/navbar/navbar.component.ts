import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {AuthService} from '../../services/auth/auth.service';
import {NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatMenuModule} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatDividerModule} from '@angular/material/divider';
import {MatTooltip} from '@angular/material/tooltip';
import {NgClass} from '@angular/common';
import {Subscription} from 'rxjs';
import {ConfirmationDialogComponent} from '../confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatToolbarModule, MatMenuModule, MatIconModule, MatButtonModule, MatDividerModule, RouterLink, RouterOutlet, MatTooltip, RouterLinkActive, NgClass],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements AfterViewInit, OnInit, OnDestroy  {

  private isDialogOpen = false;
  username: String = '';
  id: number | undefined;
  isActive: boolean = false;
  private routerSubscription?: Subscription;

  constructor(private dialog: MatDialog,
              private authService: AuthService,
              private router: Router) {
  }

  ngOnInit() {
    const userData = localStorage.getItem('user');
    if (userData) {
      const userDetails = JSON.parse(userData);
      this.username = userDetails.username;
      this.id = userDetails.id;
    } else {
      console.log('No user item stored in localStorage');
    }

    this.routerSubscription = this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.checkIfActive();
      }
    });

    // Check if the menu button should be highlighted when the component initializes
    this.checkIfActive();
  }

  checkIfActive() {
    const currentUrl = this.router.url;
    const menuRoutes = ['profile/details']; // List of routes related to the profile menu

    // If the current URL matches one of the menu routes, highlight the button
    this.isActive = menuRoutes.some(route => currentUrl.includes(route));
  }

  ngAfterViewInit() {
  }

  setActive(isActive: boolean) {
    this.isActive = isActive;
  }


  openLogoutDialog() {
    if (this.isDialogOpen) {
      return;
    }

    this.isDialogOpen = true;

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Logout Confirmation',
        message: 'Are you sure you want to logout?'
      },
      width: '400px',
      height: 'auto',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe(result => {
      this.isDialogOpen = false;
      if (result === 'confirm') {
        this.authService.logoutUser();
        this.router.navigate(['/login']);
      }
    })
  }

  ngOnDestroy() {
    // Clean up the subscription when the component is destroyed
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }
}
