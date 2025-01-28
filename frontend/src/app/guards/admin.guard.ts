import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';

import {AuthService} from '../services/auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(private authService: AuthService,
              private router: Router) {
  }

  canActivate(): boolean {
    if (this.authService.isLoggedIn()) {
      if (this.authService.getUserRole() === 'ADMIN') {
        return true;
      } else {
        this.router.navigate(['/accessDenied']);
        return false;
      }
    } else {
      this.router.navigate(['/login']);
      return false;
    }
  }
}
