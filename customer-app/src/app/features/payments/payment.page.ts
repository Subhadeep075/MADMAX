import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';
import { catchError, finalize, firstValueFrom, forkJoin, of } from 'rxjs';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { PlatformService } from '../../core/services/platform.service';
import { UiFeedbackService } from '../../core/services/ui-feedback.service';
import {
  ApplicationRequestResponse,
  CenterInfoResponse,
  PaymentMethod,
  PaymentSettingsResponse
} from '../../shared/models/api.models';

@Component({
  standalone: true,
  selector: 'app-payment-page',
  imports: [CommonModule, FormsModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './payment.page.html'
})
export class PaymentPage implements OnInit {
  readonly isNativeApp: boolean;
  readonly isBrowser: boolean;

  request: ApplicationRequestResponse | null = null;
  paymentSettings: PaymentSettingsResponse | null = null;
  centerInfo: CenterInfoResponse | null = null;
  requestTrackingId = '';

  selectedMethod: PaymentMethod = 'UPI_QR';
  upiTransactionId = '';
  screenshotFile: File | null = null;
  screenshotPreviewUrl: string | null = null;

  loading = false;
  submitting = false;
  lastSubmitFailed = false;
  errorMessage = '';

  private requestId = 0;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly api: CustomerApiService,
    private readonly platformService: PlatformService,
    private readonly ui: UiFeedbackService,
    readonly i18n: I18nService
  ) {
    this.isNativeApp = this.platformService.isNativeApp();
    this.isBrowser = this.platformService.isBrowser();
  }

  ngOnInit(): void {
    const requestIdParam = this.route.snapshot.paramMap.get('requestId');
    this.requestId = requestIdParam ? Number(requestIdParam) : NaN;
    this.requestTrackingId = this.route.snapshot.queryParamMap.get('trackingId') ?? '';

    if (!this.requestId || Number.isNaN(this.requestId)) {
      this.errorMessage = this.i18n.t('error.invalid_service');
      return;
    }

    this.loadData();
  }

  loadData(event?: CustomEvent): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      request: this.api.getRequestById(this.requestId),
      paymentSettings: this.api.getPaymentSettings(),
      centerInfo: this.api.getCenterInfo().pipe(catchError(() => of(null)))
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          event?.detail?.complete();
        })
      )
      .subscribe({
        next: (result) => {
          this.request = result.request;
          this.paymentSettings = result.paymentSettings;
          this.centerInfo = result.centerInfo;
          if (!this.requestTrackingId) {
            this.requestTrackingId = result.request.trackingId;
          }
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  onScreenshotSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);

    if (!file) {
      return;
    }

    const extension = file.name.split('.').pop()?.toLowerCase();
    const allowed = ['jpg', 'jpeg', 'png', 'pdf'];
    if (!extension || !allowed.includes(extension)) {
      this.ui.showToast(this.i18n.t('request_form.file_type_invalid'), 'warning');
      input.value = '';
      return;
    }

    if (this.screenshotPreviewUrl) {
      URL.revokeObjectURL(this.screenshotPreviewUrl);
    }

    this.screenshotFile = file;
    this.screenshotPreviewUrl = file.type.startsWith('image/') ? URL.createObjectURL(file) : null;
    input.value = '';
  }

  async submitPaymentProof(): Promise<void> {
    if (!this.request || this.submitting) {
      await this.ui.showToast(this.i18n.t('payment.request_not_loaded'), 'warning');
      return;
    }

    if (this.selectedMethod === 'UPI_QR' && !this.upiTransactionId.trim() && !this.screenshotFile) {
      await this.ui.showToast(this.i18n.t('payment.need_proof'), 'warning');
      return;
    }

    this.submitting = true;
    this.lastSubmitFailed = false;
    const loading = await this.ui.presentLoading(this.i18n.t('common.loading'));

    try {
      await firstValueFrom(
        this.api.submitPaymentProof(this.request.id, {
          method: this.selectedMethod,
          upiTransactionId: this.upiTransactionId.trim() || undefined,
          screenshot: this.screenshotFile || undefined
        })
      );

      await this.ui.showToast(this.i18n.t('payment.submit_success'), 'success');
      await this.router.navigate(['/request-detail', this.request.id], { replaceUrl: true });
    } catch (error) {
      const message = error instanceof Error ? error.message : this.i18n.t('payment.submit_failed');
      this.lastSubmitFailed = true;
      await this.ui.showToast(message, 'danger');
    } finally {
      this.submitting = false;
      await this.ui.dismissLoading(loading);
    }
  }

  async payAtShop(): Promise<void> {
    this.selectedMethod = 'CASH';
    await this.submitPaymentProof();
  }

  get callHref(): string {
    const digits = this.normalizePhone(this.centerInfo?.mobile);
    return digits ? `tel:+91${digits}` : '';
  }

  get whatsappHref(): string {
    const digits = this.normalizePhone(this.centerInfo?.whatsappNumber);
    if (!digits) {
      return '';
    }
    return `https://wa.me/91${digits}?text=${encodeURIComponent('I need help with Digital Cyber Seva')}`;
  }

  get hasCallContact(): boolean {
    return !!this.normalizePhone(this.centerInfo?.mobile);
  }

  get hasWhatsappContact(): boolean {
    return !!this.normalizePhone(this.centerInfo?.whatsappNumber);
  }

  private normalizePhone(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    const digits = value.replace(/\D/g, '');
    return digits.length >= 10 ? digits : '';
  }
}
