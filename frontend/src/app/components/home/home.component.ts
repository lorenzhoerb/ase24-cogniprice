import {Component, Renderer2} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {

  constructor(private renderer: Renderer2) {
  }

  ngOnInit(): void {
    this.renderer.removeClass(document.body, 'login-page'); // Ensure login class is removed
    this.renderer.removeClass(document.body, 'registration-page'); // Ensure registration class is removed
  }
}
