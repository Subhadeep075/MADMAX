import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AdminApiService } from '../../core/services/admin-api.service';
import { PaymentSettingsResponse } from '../../shared/models/api.models';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment-settings.component.html'
})
export class PaymentSettingsComponent implements OnInit {
  settings: PaymentSettingsResponse | null = null;
  qrFile: File | null = null;
  shopUpiName = '';
  shopUpiId = '';
  centerName = '';
  officialUpiId = '';
  centerMobile = '';
  centerWhatsappNumber = '';
  centerAddress = '';
  centerWorkingHours = '';
  saving = false;
  errorMessage = '';
  successMessage = '';

  constructor(private readonly adminApiService: AdminApiService) {}

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings(): void {
    this.errorMessage = '';
    this.adminApiService.getPaymentSettings().subscribe({
      next: (response) => {
        this.settings = response;
        this.shopUpiName = response.shopUpiName || '';
        this.shopUpiId = response.shopUpiId || '';
        this.centerName = response.centerName || '';
        this.officialUpiId = response.officialUpiId || '';
        this.centerMobile = response.centerMobile || '';
        this.centerWhatsappNumber = response.centerWhatsappNumber || '';
        this.centerAddress = response.centerAddress || '';
        this.centerWorkingHours = response.centerWorkingHours || '';
      },
      error: (error: Error) => {
        this.errorMessage = error.message;
      }
    });
  }

  onQrFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.qrFile = input.files?.item(0) ?? null;
  }

  uploadQr(): void {
    if (
      !this.qrFile &&
      !this.shopUpiName.trim() &&
      !this.shopUpiId.trim() &&
      !this.centerName.trim() &&
      !this.officialUpiId.trim() &&
      !this.centerMobile.trim() &&
      !this.centerWhatsappNumber.trim() &&
      !this.centerAddress.trim() &&
      !this.centerWorkingHours.trim()
    ) {
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminApiService
      .updateUpiQr(
        this.qrFile,
        this.shopUpiName,
        this.shopUpiId,
        this.centerName,
        this.officialUpiId,
        this.centerMobile,
        this.centerWhatsappNumber,
        this.centerAddress,
        this.centerWorkingHours
      )
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: (response) => {
          this.settings = response;
          this.successMessage = 'UPI QR updated successfully.';
          this.qrFile = null;
          this.shopUpiName = response.shopUpiName || '';
          this.shopUpiId = response.shopUpiId || '';
          this.centerName = response.centerName || '';
          this.officialUpiId = response.officialUpiId || '';
          this.centerMobile = response.centerMobile || '';
          this.centerWhatsappNumber = response.centerWhatsappNumber || '';
          this.centerAddress = response.centerAddress || '';
          this.centerWorkingHours = response.centerWorkingHours || '';
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }
}
