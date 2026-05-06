import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { AdminApiService } from '../../core/services/admin-api.service';
import {
  CategoryResponse,
  ServiceResponse,
  ServiceStatus,
  ServiceUpsertRequest
} from '../../shared/models/api.models';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './service-management.component.html'
})
export class ServiceManagementComponent implements OnInit {
  categories: CategoryResponse[] = [];
  services: ServiceResponse[] = [];
  filteredServices: ServiceResponse[] = [];
  loading = false;
  saving = false;
  deleting = false;
  editingServiceId: number | null = null;
  errorMessage = '';
  successMessage = '';

  searchTerm = '';
  statusFilter = '';
  readonly serviceStatuses: ServiceStatus[] = ['OPEN', 'CLOSED', 'COMING_SOON'];

  readonly serviceForm = this.fb.group({
    categoryId: [null as number | null, [Validators.required]],
    title: ['', [Validators.required]],
    description: ['', [Validators.required]],
    requiredDocumentsList: ['', [Validators.required]],
    applicantFieldsList: ['Applicant Name, Applicant Mobile, Applicant Email, Applicant Address', [Validators.required]],
    govtFee: [0, [Validators.required, Validators.min(0)]],
    serviceFee: [0, [Validators.required, Validators.min(0)]],
    status: ['OPEN' as ServiceStatus, [Validators.required]],
    openDate: [''],
    closeDate: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly adminApiService: AdminApiService
  ) {}

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.loading = true;
    this.errorMessage = '';
    forkJoin({
      categories: this.adminApiService.getAdminCategories(),
      services: this.adminApiService.getServices()
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ categories, services }) => {
          this.categories = categories.filter((item) => item.active);
          this.services = services;
          this.applyFilters();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  saveService(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (this.serviceForm.invalid) {
      this.serviceForm.markAllAsTouched();
      return;
    }

    const payload = this.toPayload();
    this.saving = true;
    const operation = this.editingServiceId
      ? this.adminApiService.updateService(this.editingServiceId, payload)
      : this.adminApiService.createService(payload);

    operation.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        this.successMessage = this.editingServiceId
          ? 'Service updated successfully.'
          : 'Service created successfully.';
        this.resetForm();
        this.loadInitialData();
      },
      error: (error: Error) => {
        this.errorMessage = error.message;
      }
    });
  }

  deleteEditingService(): void {
    if (!this.editingServiceId || this.deleting) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    const confirmed = window.confirm(
      'Delete this service permanently? This cannot be undone.'
    );
    if (!confirmed) {
      return;
    }

    const serviceId = this.editingServiceId;
    this.deleting = true;
    this.adminApiService
      .deleteService(serviceId)
      .pipe(finalize(() => (this.deleting = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Service deleted successfully.';
          this.resetForm();
          this.loadInitialData();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  startEdit(service: ServiceResponse): void {
    this.editingServiceId = service.id;
    this.serviceForm.patchValue({
      categoryId: service.categoryId,
      title: service.title,
      description: service.description,
      requiredDocumentsList: this.parseCommaSeparatedValues(service.requiredDocumentsJson),
      applicantFieldsList: this.parseCommaSeparatedValues(service.applicantFieldsJson),
      govtFee: Number(service.govtFee),
      serviceFee: Number(service.serviceFee),
      status: service.status,
      openDate: this.toDateInputValue(service.openDate),
      closeDate: this.toDateInputValue(service.closeDate)
    });
  }

  resetForm(): void {
    this.editingServiceId = null;
    this.serviceForm.reset({
      categoryId: null,
      title: '',
      description: '',
      requiredDocumentsList: '',
      applicantFieldsList: 'Applicant Name, Applicant Mobile, Applicant Email, Applicant Address',
      govtFee: 0,
      serviceFee: 0,
      status: 'OPEN',
      openDate: '',
      closeDate: ''
    });
  }

  applyFilters(): void {
    const query = this.searchTerm.trim().toLowerCase();
    this.filteredServices = this.services.filter((service) => {
      const matchesQuery =
        query.length === 0 ||
        service.title.toLowerCase().includes(query) ||
        service.categoryName.toLowerCase().includes(query);

      const matchesStatus = !this.statusFilter || service.status === this.statusFilter;
      return matchesQuery && matchesStatus;
    });
  }

  statusClass(status: ServiceStatus): string {
    if (status === 'OPEN') {
      return 'text-bg-success';
    }
    if (status === 'CLOSED') {
      return 'text-bg-danger';
    }
    return 'text-bg-warning';
  }

  private parseCommaSeparatedValues(value: string): string {
    try {
      const parsed = JSON.parse(value) as unknown;
      if (Array.isArray(parsed)) {
        return parsed.filter((item): item is string => typeof item === 'string').join(', ');
      }
      return value;
    } catch {
      return value;
    }
  }

  private toDateInputValue(value: string | null): string {
    if (!value) {
      return '';
    }
    return value.slice(0, 10);
  }

  private toPayload(): ServiceUpsertRequest {
    const raw = this.serviceForm.getRawValue();
    const docs = (raw.requiredDocumentsList ?? '')
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item.length > 0);
    const applicantFields = (raw.applicantFieldsList ?? '')
      .split(',')
      .map((item) => item.trim())
      .filter((item) => item.length > 0);

    return {
      categoryId: Number(raw.categoryId),
      title: (raw.title ?? '').trim(),
      description: (raw.description ?? '').trim(),
      requiredDocumentsJson: JSON.stringify(docs),
      applicantFieldsJson: JSON.stringify(applicantFields.length > 0 ? applicantFields : [
        'Applicant Name',
        'Applicant Mobile',
        'Applicant Email',
        'Applicant Address'
      ]),
      govtFee: Number(raw.govtFee ?? 0),
      serviceFee: Number(raw.serviceFee ?? 0),
      status: (raw.status ?? 'OPEN') as ServiceStatus,
      openDate: raw.openDate ? raw.openDate : null,
      closeDate: raw.closeDate ? raw.closeDate : null
    };
  }
}
