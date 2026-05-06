import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  standalone: true,
  selector: 'app-startup-redirect',
  template: ''
})
export class StartupRedirectPage implements OnInit {
  constructor(
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    if (this.authService.isLoggedIn() && this.authService.hasRole('CUSTOMER')) {
      this.router.navigateByUrl('/app/home', { replaceUrl: true });
      return;
    }

    this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}
