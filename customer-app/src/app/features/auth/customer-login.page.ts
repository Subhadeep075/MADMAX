import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { AuthApiService } from '../../core/services/auth-api.service';
import { UiFeedbackService } from '../../core/services/ui-feedback.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { AppLanguage } from '../../core/i18n/i18n.types';

@Component({
  standalone: true,
  selector: 'app-customer-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './customer-login.page.html'
})
export class CustomerLoginPage implements OnInit {
  private readonly rememberedMobileKey = 'dcs_customer_remembered_mobile';

  showPin = false;

  readonly loginForm = this.fb.nonNullable.group({
    mobile: ['', [Validators.required, Validators.pattern(/^[0-9]{10,15}$/)]],
    pin: ['', [Validators.required, Validators.pattern(/^[0-9]{4}$/)]],
    rememberMobile: [true]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly authApiService: AuthApiService,
    private readonly authService: AuthService,
    private readonly ui: UiFeedbackService,
    readonly i18n: I18nService
  ) {}

  get selectedLanguage(): AppLanguage {
    return this.i18n.currentLanguage;
  }

  ngOnInit(): void {
    const rememberedMobile = localStorage.getItem(this.rememberedMobileKey);
    if (rememberedMobile) {
      this.loginForm.patchValue({
        mobile: rememberedMobile,
        rememberMobile: true
      });
      return;
    }

    this.loginForm.patchValue({ rememberMobile: false });
  }

  async submit(): Promise<void> {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      await this.ui.showToast(this.i18n.t('login.invalid'), 'warning');
      return;
    }

    const { mobile, pin, rememberMobile } = this.loginForm.getRawValue();
    const normalizedMobile = mobile.trim();
    const loading = await this.ui.presentLoading(this.i18n.t('common.loading'));

    try {
      const response = await firstValueFrom(
        this.authApiService.login({
          mobile: normalizedMobile,
          pin
        })
      );

      if (response.role !== 'CUSTOMER') {
        await this.ui.showToast(this.i18n.t('login.customer_only'), 'danger');
        return;
      }

      if (rememberMobile) {
        localStorage.setItem(this.rememberedMobileKey, normalizedMobile);
      } else {
        localStorage.removeItem(this.rememberedMobileKey);
      }

      this.authService.setSession(response, normalizedMobile);
      await this.ui.showToast(`Welcome ${response.name}`, 'success');
      await this.router.navigateByUrl('/app/home', { replaceUrl: true });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to login right now.';
      await this.ui.showToast(message, 'danger');
    } finally {
      await this.ui.dismissLoading(loading);
    }
  }

  changeLanguage(language: AppLanguage): void {
    this.i18n.setLanguage(language);
  }
}
