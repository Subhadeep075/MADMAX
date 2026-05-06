import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';
import { finalize } from 'rxjs';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ServiceResponse } from '../../shared/models/api.models';

@Component({
  standalone: true,
  selector: 'app-service-detail-page',
  imports: [CommonModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './service-detail.page.html'
})
export class ServiceDetailPage implements OnInit {
  service: ServiceResponse | null = null;
  requiredDocuments: string[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly api: CustomerApiService,
    readonly i18n: I18nService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const serviceId = idParam ? Number(idParam) : NaN;

    if (Number.isNaN(serviceId)) {
      this.errorMessage = this.i18n.t('error.invalid_service');
      return;
    }

    this.loadService(serviceId);
  }

  loadService(serviceId: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.api
      .getServiceById(serviceId)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (service) => {
          this.service = service;
          this.requiredDocuments = this.parseRequiredDocuments(service.requiredDocumentsJson);
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  canApply(): boolean {
    return this.service?.status === 'OPEN';
  }

  applyNow(): void {
    if (!this.service || !this.canApply()) {
      return;
    }

    this.router.navigate(['/request-form', this.service.id]);
  }

  private parseRequiredDocuments(raw: string): string[] {
    if (!raw?.trim()) {
      return [];
    }

    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        return parsed.map((item) => String(item)).filter((item) => !!item.trim());
      }

      return raw
        .split(',')
        .map((item) => item.trim())
        .filter((item) => !!item);
    } catch {
      return raw
        .split(',')
        .map((item) => item.trim())
        .filter((item) => !!item);
    }
  }
}
