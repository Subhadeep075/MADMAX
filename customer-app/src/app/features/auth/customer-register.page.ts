import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';
import { firstValueFrom } from 'rxjs';
import { AuthApiService } from '../../core/services/auth-api.service';
import { UiFeedbackService } from '../../core/services/ui-feedback.service';
import { I18nService } from '../../core/i18n/i18n.service';

@Component({
  standalone: true,
  selector: 'app-customer-register',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './customer-register.page.html'
})
export class CustomerRegisterPage {
  showPin = false;

  readonly registerForm = this.fb.nonNullable.group(
    {
      name: ['', [Validators.required]],
      mobile: ['', [Validators.required, Validators.pattern(/^[0-9]{10,15}$/)]],
      email: ['', [Validators.email]],
      pin: ['', [Validators.required, Validators.pattern(/^[0-9]{4}$/)]],
      confirmPin: ['', [Validators.required]],
      address: ['']
    },
    { validators: [CustomerRegisterPage.pinMatchValidator] }
  );

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly authApiService: AuthApiService,
    private readonly ui: UiFeedbackService,
    readonly i18n: I18nService
  ) {}

  async submit(): Promise<void> {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      if (this.registerForm.hasError('pinMismatch')) {
        await this.ui.showToast(this.i18n.t('register.pin_mismatch'), 'warning');
        return;
      }
      await this.ui.showToast(this.i18n.t('register.invalid'), 'warning');
      return;
    }

    const payload = this.registerForm.getRawValue();
    const loading = await this.ui.presentLoading(this.i18n.t('common.loading'));

    try {
      await firstValueFrom(
        this.authApiService.register({
          name: payload.name.trim(),
          mobile: payload.mobile.trim(),
          email: payload.email.trim() || null,
          pin: payload.pin,
          address: payload.address.trim() || null
        })
      );

      await this.ui.showToast(this.i18n.t('register.success'), 'success');
      await this.router.navigateByUrl('/login', { replaceUrl: true });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to register right now.';
      await this.ui.showToast(message, 'danger');
    } finally {
      await this.ui.dismissLoading(loading);
    }
  }

  private static pinMatchValidator(form: AbstractControl): ValidationErrors | null {
    const pin = form.get('pin')?.value;
    const confirmPin = form.get('confirmPin')?.value;

    if (!pin || !confirmPin) {
      return null;
    }
    return pin === confirmPin ? null : { pinMismatch: true };
  }
}
