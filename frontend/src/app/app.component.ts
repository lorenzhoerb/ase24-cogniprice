import {Component, Renderer2} from '@angular/core';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {NavbarComponent} from './components/navbar/navbar.component';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [NgIf, RouterOutlet, NavbarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'CogniPrice';
  showNavbar = true;
  constructor(private router: Router, private renderer: Renderer2) {}

  ngOnInit(): void {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {

        // Check if the current route is login or registration and set a background class
        if (event.urlAfterRedirects.includes('login')) {
          this.renderer.addClass(document.body, 'login-page');
          this.showNavbar = false;
        } else if (event.urlAfterRedirects.includes('registration')) {
          this.renderer.addClass(document.body, 'registration-page');
          this.showNavbar = false;
        } else if (event.urlAfterRedirects.includes('forgot-password')) {
          this.showNavbar = false;
        } else if (event.urlAfterRedirects.includes('change-password')) {
          this.showNavbar = false;
        }
        else {
          this.showNavbar = true;
        }
      }
    });
  }
}
