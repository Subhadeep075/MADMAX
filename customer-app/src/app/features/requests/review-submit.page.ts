import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { RequestDraftService } from '../../core/services/request-draft.service';
import { UiFeedbackService } from '../../core/services/ui-feedback.service';
import { DraftDocumentFile, RequestDraft } from '../../shared/models/api.models';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';

@Component({
  standalone: true,
  selector: 'app-review-submit-page',
  imports: [CommonModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './review-submit.page.html'
})
export class ReviewSubmitPage implements OnInit {
  draft: RequestDraft | null = null;
  errorMessage = '';
  submitting = false;

  serviceId = 0;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly api: CustomerApiService,
    private readonly draftService: RequestDraftService,
    private readonly ui: UiFeedbackService,
    readonly i18n: I18nService
  ) {}

  ngOnInit(): void {
    const serviceIdParam = this.route.snapshot.paramMap.get('serviceId');
    this.serviceId = serviceIdParam ? Number(serviceIdParam) : NaN;

    if (!this.serviceId || Number.isNaN(this.serviceId)) {
      this.errorMessage = this.i18n.t('error.invalid_service');
      return;
    }

    this.draft = this.draftService.getDraft(this.serviceId);
    if (!this.draft) {
      this.errorMessage = this.i18n.t('review.no_draft_found');
    }
  }

  get failedDocuments(): DraftDocumentFile[] {
    if (!this.draft?.failedDocumentIds?.length) {
      return [];
    }
    const failedIds = new Set(this.draft.failedDocumentIds);
    return this.draft.documents.filter((document) => failedIds.has(document.id));
  }

  async submitRequest(): Promise<void> {
    if (!this.draft || this.submitting) {
      return;
    }

    this.submitting = true;
    const loading = await this.ui.presentLoading(this.i18n.t('review.submitting'));

    try {
      if (!this.draft.createdRequestId) {
        const createdRequest = await firstValueFrom(
          this.api.createRequest({
            serviceId: this.draft.serviceId,
            remarks: this.buildRemarks()
          })
        );

        this.draft.createdRequestId = createdRequest.id;
        this.draft.trackingId = createdRequest.trackingId;
        this.draft.failedDocumentIds = [];
        this.draftService.saveDraft(this.draft);
      }

      const documentsToUpload = this.resolveDocumentsToUpload(this.draft);
      const failedDocumentIds: string[] = [];

      for (const document of documentsToUpload) {
        try {
          await firstValueFrom(this.api.uploadRequestDocument(this.draft.createdRequestId, document.documentType, document.file));
        } catch {
          failedDocumentIds.push(document.id);
        }
      }

      if (failedDocumentIds.length > 0) {
        this.draft.failedDocumentIds = failedDocumentIds;
        this.draftService.saveDraft(this.draft);
        await this.ui.showToast(this.i18n.t('review.upload_retry_failed'), 'warning');
        return;
      }

      const trackingId = this.draft.trackingId || `REQ-${this.draft.createdRequestId}`;
      const requestId = this.draft.createdRequestId;
      this.draftService.clearDraft(this.draft.serviceId);
      this.draft = null;

      await this.ui.showToast(this.i18n.t('review.created', { trackingId }), 'success');
      await this.router.navigate(['/payment', requestId], {
        replaceUrl: true,
        queryParams: {
          trackingId
        }
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : this.i18n.t('payment.submit_failed');
      await this.ui.showToast(message, 'danger');
    } finally {
      this.submitting = false;
      await this.ui.dismissLoading(loading);
    }
  }

  editRequestForm(): void {
    this.router.navigate(['/request-form', this.serviceId]);
  }

  private resolveDocumentsToUpload(draft: RequestDraft): DraftDocumentFile[] {
    const failedIds = new Set(draft.failedDocumentIds ?? []);
    if (failedIds.size === 0) {
      return draft.documents;
    }
    return draft.documents.filter((document) => failedIds.has(document.id));
  }

  private buildRemarks(): string {
    if (!this.draft) {
      return '';
    }

    const details = this.draft.applicantDetails.entries;
    const lines = details.map((entry) => `${entry.label}: ${entry.value || '-'}`);
    return lines.join(' | ');
  }
}
