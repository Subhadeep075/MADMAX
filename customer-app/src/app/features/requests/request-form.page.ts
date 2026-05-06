import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, finalize, of, Subscription } from 'rxjs';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { PlatformService } from '../../core/services/platform.service';
import { RequestDraftService } from '../../core/services/request-draft.service';
import { UiFeedbackService } from '../../core/services/ui-feedback.service';
import {
  CenterInfoResponse,
  DraftApplicantFieldEntry,
  DraftDocumentFile,
  RequestDraft,
  ServiceResponse
} from '../../shared/models/api.models';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';

interface ApplicantFieldConfig {
  key: string;
  label: string;
  type: 'text' | 'email' | 'tel' | 'textarea';
  required: boolean;
}

@Component({
  standalone: true,
  selector: 'app-request-form-page',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './request-form.page.html'
})
export class RequestFormPage implements OnInit, OnDestroy {
  private static readonly DEFAULT_APPLICANT_FIELDS = ['Applicant Name', 'Applicant Mobile', 'Applicant Email', 'Applicant Address'];
  private static readonly ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'pdf'];

  readonly isNativeApp: boolean;
  readonly isBrowser: boolean;

  service: ServiceResponse | null = null;
  centerInfo: CenterInfoResponse | null = null;
  requiredDocuments: string[] = [];
  applicantFields: ApplicantFieldConfig[] = [];
  draftDocuments: DraftDocumentFile[] = [];
  optionalDocumentType = '';
  loading = false;
  errorMessage = '';

  private serviceId = 0;
  private existingDraft: RequestDraft | null = null;
  private formValueSub: Subscription | null = null;

  applicantForm = this.fb.group({});

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly api: CustomerApiService,
    private readonly platformService: PlatformService,
    private readonly draftService: RequestDraftService,
    private readonly ui: UiFeedbackService,
    readonly i18n: I18nService
  ) {
    this.isNativeApp = this.platformService.isNativeApp();
    this.isBrowser = this.platformService.isBrowser();
  }

  ngOnInit(): void {
    const serviceIdParam = this.route.snapshot.paramMap.get('serviceId');
    this.serviceId = serviceIdParam ? Number(serviceIdParam) : NaN;

    if (!this.serviceId || Number.isNaN(this.serviceId)) {
      this.errorMessage = this.i18n.t('request_form.invalid_service');
      return;
    }

    this.existingDraft = this.draftService.getDraft(this.serviceId);
    if (this.existingDraft) {
      this.draftDocuments = [...this.existingDraft.documents];
    }

    this.loadService();
  }

  ngOnDestroy(): void {
    this.formValueSub?.unsubscribe();
  }

  get primaryApplicantFields(): ApplicantFieldConfig[] {
    return this.applicantFields.filter((field) => field.required);
  }

  get optionalApplicantFields(): ApplicantFieldConfig[] {
    return this.applicantFields.filter((field) => !field.required);
  }

  get hasMissingRequiredDocuments(): boolean {
    if (this.requiredDocuments.length === 0) {
      return false;
    }
    return this.requiredDocuments.some((documentType) => !this.getDocumentForType(documentType));
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

  loadService(): void {
    this.loading = true;
    this.errorMessage = '';

    this.api
      .getServiceById(this.serviceId)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (service) => {
          this.service = service;
          this.requiredDocuments = this.parseRequiredDocuments(service.requiredDocumentsJson);
          this.setupApplicantFields(service.applicantFieldsJson, this.existingDraft);
          this.loadCenterInfo();
          this.saveCurrentDraft();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  loadCenterInfo(): void {
    this.api
      .getCenterInfo()
      .pipe(catchError(() => of(null)))
      .subscribe((response) => {
        this.centerInfo = response;
      });
  }

  async onRequiredDocumentSelected(documentType: string, event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);
    input.value = '';
    if (!file) {
      return;
    }

    await this.addOrReplaceDocument(documentType, file);
  }

  async onOptionalDocumentSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);
    input.value = '';
    if (!file) {
      return;
    }

    if (!this.optionalDocumentType.trim()) {
      await this.ui.showToast(this.i18n.t('request_form.please_complete'), 'warning');
      return;
    }

    await this.addOrReplaceDocument(this.optionalDocumentType, file);
    this.optionalDocumentType = '';
  }

  async removeDraftDocument(document: DraftDocumentFile): Promise<void> {
    const confirmed = await this.ui.confirm(
      this.i18n.t('request_form.delete_draft_title'),
      this.i18n.t('request_form.delete_draft_message'),
      this.i18n.t('common.delete')
    );
    if (!confirmed) {
      return;
    }

    this.draftDocuments = this.draftDocuments.filter((item) => item.id !== document.id);
    this.saveCurrentDraft();
  }

  openDocument(document: DraftDocumentFile): void {
    const url = document.dataUrl;
    if (!url) {
      return;
    }
    window.open(url, '_blank');
  }

  documentStatus(documentType: string): 'MISSING' | 'UPLOADED' {
    return this.getDocumentForType(documentType) ? 'UPLOADED' : 'MISSING';
  }

  getDocumentForType(documentType: string): DraftDocumentFile | null {
    const normalized = this.normalizeDocumentType(documentType);
    return this.draftDocuments.find((item) => this.normalizeDocumentType(item.documentType) === normalized) ?? null;
  }

  async continueToReview(): Promise<void> {
    if (!this.service) {
      await this.ui.showToast(this.i18n.t('request_form.invalid_service'), 'warning');
      return;
    }

    if (this.applicantForm.invalid) {
      this.applicantForm.markAllAsTouched();
      await this.ui.showToast(this.i18n.t('request_form.please_complete'), 'warning');
      return;
    }

    if (this.hasMissingRequiredDocuments) {
      await this.ui.showToast(this.i18n.t('request_form.please_upload_all'), 'warning');
      return;
    }

    this.saveCurrentDraft();
    await this.router.navigate(['/review-submit', this.service.id]);
  }

  private async addOrReplaceDocument(documentType: string, file: File): Promise<void> {
    const extension = file.name.split('.').pop()?.toLowerCase();
    if (!extension || !RequestFormPage.ALLOWED_EXTENSIONS.includes(extension)) {
      await this.ui.showToast(this.i18n.t('request_form.file_type_invalid'), 'warning');
      return;
    }

    try {
      const dataUrl = await this.readFileAsDataUrl(file);
      const previewUrl = file.type.startsWith('image/') ? dataUrl : null;
      const normalizedType = this.normalizeDocumentType(documentType);

      const nextDocument: DraftDocumentFile = {
        id: `${Date.now()}-${Math.random()}`,
        documentType: documentType.trim(),
        fileName: file.name,
        mimeType: file.type || 'application/octet-stream',
        file,
        dataUrl,
        previewUrl
      };

      const existingIndex = this.draftDocuments.findIndex(
        (item) => this.normalizeDocumentType(item.documentType) === normalizedType
      );
      if (existingIndex >= 0) {
        this.draftDocuments.splice(existingIndex, 1, nextDocument);
      } else {
        this.draftDocuments.push(nextDocument);
      }
      this.draftDocuments = [...this.draftDocuments];
      this.saveCurrentDraft();
    } catch {
      await this.ui.showToast(this.i18n.t('request_form.upload_failed'), 'danger');
    }
  }

  private saveCurrentDraft(): void {
    if (!this.service) {
      return;
    }

    this.draftService.saveDraft({
      serviceId: this.service.id,
      serviceTitle: this.service.title,
      totalAmount: this.service.totalFee,
      govtFee: this.service.govtFee,
      serviceFee: this.service.serviceFee,
      applicantDetails: {
        entries: this.collectApplicantEntries()
      },
      documents: [...this.draftDocuments],
      createdRequestId: this.existingDraft?.createdRequestId ?? null,
      trackingId: this.existingDraft?.trackingId ?? null,
      failedDocumentIds: this.existingDraft?.failedDocumentIds ?? []
    });
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
      return [];
    } catch {
      return raw
        .split(',')
        .map((item) => item.trim())
        .filter((item) => !!item);
    }
  }

  private setupApplicantFields(raw: string, existingDraft: RequestDraft | null): void {
    const labels = this.parseApplicantFieldLabels(raw);
    const usedKeys = new Set<string>();

    this.applicantFields = labels.map((label) => this.toApplicantFieldConfig(label, usedKeys));

    const controls: Record<string, unknown> = {};
    this.applicantFields.forEach((field) => {
      const validators = [];
      if (field.required) {
        validators.push(Validators.required);
      }
      if (field.type === 'email') {
        validators.push(Validators.email);
      }
      if (field.type === 'tel') {
        validators.push(Validators.pattern(/^[0-9]{10,15}$/));
      }
      controls[field.key] = ['', validators];
    });

    this.applicantForm = this.fb.group(controls);
    this.patchDraftValues(existingDraft);
    this.formValueSub?.unsubscribe();
    this.formValueSub = this.applicantForm.valueChanges.subscribe(() => this.saveCurrentDraft());
  }

  private parseApplicantFieldLabels(raw: string): string[] {
    const fallback = [...RequestFormPage.DEFAULT_APPLICANT_FIELDS];
    if (!raw?.trim()) {
      return fallback;
    }

    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        const labels = parsed.map((item) => String(item).trim()).filter((item) => item.length > 0);
        return labels.length > 0 ? labels : fallback;
      }
    } catch {
      const labels = raw
        .split(',')
        .map((item) => item.trim())
        .filter((item) => item.length > 0);
      return labels.length > 0 ? labels : fallback;
    }

    return fallback;
  }

  private toApplicantFieldConfig(label: string, usedKeys: Set<string>): ApplicantFieldConfig {
    const normalizedLabel = label.trim() || 'Applicant Detail';
    const lower = normalizedLabel.toLowerCase();
    let baseKey = lower.replace(/[^a-z0-9]+/g, '_').replace(/^_+|_+$/g, '');
    if (!baseKey) {
      baseKey = 'field';
    }

    let key = baseKey;
    let count = 2;
    while (usedKeys.has(key)) {
      key = `${baseKey}_${count}`;
      count += 1;
    }
    usedKeys.add(key);

    const isEmail = lower.includes('email');
    const isMobile = lower.includes('mobile') || lower.includes('phone');
    const isAddress = lower.includes('address');
    const isName = lower.includes('name');
    const required = isName || isMobile;

    return {
      key,
      label: normalizedLabel,
      type: isAddress ? 'textarea' : isEmail ? 'email' : isMobile ? 'tel' : 'text',
      required
    };
  }

  private patchDraftValues(existingDraft: RequestDraft | null): void {
    if (!existingDraft?.applicantDetails?.entries?.length) {
      return;
    }

    const draftValuesByLabel = new Map(
      existingDraft.applicantDetails.entries.map((entry) => [this.normalizeLabel(entry.label), entry.value])
    );

    const valuesToPatch: Record<string, string> = {};
    this.applicantFields.forEach((field) => {
      const value = draftValuesByLabel.get(this.normalizeLabel(field.label));
      if (typeof value === 'string') {
        valuesToPatch[field.key] = value;
      }
    });

    this.applicantForm.patchValue(valuesToPatch);
  }

  private collectApplicantEntries(): DraftApplicantFieldEntry[] {
    return this.applicantFields.map((field) => {
      const raw = this.applicantForm.get(field.key)?.value;
      return {
        label: field.label,
        value: typeof raw === 'string' ? raw.trim() : ''
      };
    });
  }

  private normalizeLabel(label: string): string {
    return label.trim().toLowerCase();
  }

  private normalizeDocumentType(value: string): string {
    return value.trim().toLowerCase();
  }

  private normalizePhone(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    const digits = value.replace(/\D/g, '');
    return digits.length >= 10 ? digits : '';
  }

  private readFileAsDataUrl(file: File): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        if (typeof reader.result === 'string') {
          resolve(reader.result);
          return;
        }
        reject(new Error('Unable to read file.'));
      };
      reader.onerror = () => reject(new Error('Unable to read file.'));
      reader.readAsDataURL(file);
    });
  }
}
