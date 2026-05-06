import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { AuthApiService } from '../../core/services/auth-api.service';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-login.component.html'
})
export class AdminLoginComponent implements OnInit {
  private readonly rememberedUserIdKey = 'dcs_admin_remembered_user_id';

  submitted = false;
  loading = false;
  errorMessage = '';
  showPassword = false;

  readonly loginForm = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
    rememberUserId: [true]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authApiService: AuthApiService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    const rememberedUserId = localStorage.getItem(this.rememberedUserIdKey);
    if (rememberedUserId) {
      this.loginForm.patchValue({
        username: rememberedUserId,
        rememberUserId: true
      });
    } else {
      this.loginForm.patchValue({
        rememberUserId: false
      });
    }
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';

    if (this.loginForm.invalid) {
      return;
    }

    const { username, password, rememberUserId } = this.loginForm.getRawValue();
    const normalizedUserId = username.trim();

    this.loading = true;
    this.authApiService
      .login({ username: normalizedUserId, password })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          if (response.role !== 'ADMIN') {
            this.errorMessage = 'Only admin users can access this panel.';
            return;
          }
          if (rememberUserId) {
            localStorage.setItem(this.rememberedUserIdKey, normalizedUserId);
          } else {
            localStorage.removeItem(this.rememberedUserIdKey);
          }
          this.authService.setSession(response.token, response.role, response.name);
          this.router.navigateByUrl('/dashboard');
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }
}
