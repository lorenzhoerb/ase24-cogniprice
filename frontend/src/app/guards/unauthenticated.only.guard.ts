import {CanActivate, Router,} from '@angular/router';
import {Injectable} from '@angular/core';
import {AuthService} from '../services/auth/auth.service';

/**
 * UnauthenticatedOnlyGuard prevents access to specific routes (e.g., registration or login)
 * if the user is already logged in. If logged in, the user is redirected to the dashboard.
 */
@Injectable({
  providedIn: 'root'
})
export class UnauthenticatedOnlyGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {
  }

  /**
   * Determines if a route can be activated.
   * Redirects logged-in users to the dashboard if they attempt to access routes for unauthenticated users.
   * @returns `true` if the user is not logged in, allowing access to the route; otherwise `false`.
   */
  canActivate(): boolean {
    if (this.authService.isLoggedIn()) { // Replace `isLoggedIn` with your actual method
      this.router.navigate(['/dashboard']);
      return false;
    }
    return true;
  }
}
