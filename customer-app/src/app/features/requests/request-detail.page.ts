import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';
import { finalize, firstValueFrom } from 'rxjs';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { UiFeedbackService } from '../../core/services/ui-feedback.service';
import { ApplicationRequestResponse, RequestDocumentResponse, RequestStatus } from '../../shared/models/api.models';
import { REQUEST_TIMELINE } from '../../shared/models/status.constants';

@Component({
  standalone: true,
  selector: 'app-request-detail-page',
  imports: [CommonModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './request-detail.page.html'
})
export class RequestDetailPage implements OnInit {
  request: ApplicationRequestResponse | null = null;
  loading = false;
  deletingDocumentId: number | null = null;
  errorMessage = '';

  readonly timelineSteps = REQUEST_TIMELINE;

  private requestId = 0;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly api: CustomerApiService,
    private readonly ui: UiFeedbackService,
    readonly i18n: I18nService
  ) {}

  ngOnInit(): void {
    const requestIdParam = this.route.snapshot.paramMap.get('id');
    this.requestId = requestIdParam ? Number(requestIdParam) : NaN;

    if (!this.requestId || Number.isNaN(this.requestId)) {
      this.errorMessage = this.i18n.t('error.invalid_service');
      return;
    }

    this.loadRequest();
  }

  loadRequest(event?: CustomEvent): void {
    this.loading = true;
    this.errorMessage = '';

    this.api
      .getRequestById(this.requestId)
      .pipe(
        finalize(() => {
          this.loading = false;
          event?.detail?.complete();
        })
      )
      .subscribe({
        next: (response) => {
          this.request = response;
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  isStepActive(step: RequestStatus): boolean {
    if (!this.request) {
      return false;
    }

    const currentIndex = this.timelineSteps.indexOf(this.request.status);
    const stepIndex = this.timelineSteps.indexOf(step);

    if (this.request.status === 'REJECTED') {
      return step === 'REJECTED';
    }

    return stepIndex <= currentIndex && step !== 'REJECTED';
  }

  isFinalDocument(document: RequestDocumentResponse): boolean {
    return document.documentType === 'FINAL_DOCUMENT';
  }

  async deleteDocument(document: RequestDocumentResponse): Promise<void> {
    if (!this.request || this.isFinalDocument(document)) {
      return;
    }

    const confirmed = await this.ui.confirm(
      this.i18n.t('request_detail.delete_confirm_title'),
      this.i18n.t('request_detail.delete_confirm_message'),
      this.i18n.t('common.delete')
    );

    if (!confirmed) {
      return;
    }

    this.deletingDocumentId = document.id;
    try {
      await firstValueFrom(this.api.deleteRequestDocument(this.request.id, document.id));
      await this.ui.showToast(this.i18n.t('request_detail.delete_success'), 'success');
      this.loadRequest();
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to delete document.';
      await this.ui.showToast(message, 'danger');
    } finally {
      this.deletingDocumentId = null;
    }
  }

  goToPayment(): void {
    if (!this.request) {
      return;
    }
    this.router.navigate(['/payment', this.request.id]);
  }

  get finalDocuments(): RequestDocumentResponse[] {
    return this.request?.documents.filter((document) => this.isFinalDocument(document)) ?? [];
  }
}
