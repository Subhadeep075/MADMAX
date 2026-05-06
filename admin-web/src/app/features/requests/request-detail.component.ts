import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AdminApiService } from '../../core/services/admin-api.service';
import {
  ApplicationRequestResponse,
  ApplicationRequestStatus,
  PaymentStatus,
  RequestDocumentResponse
} from '../../shared/models/api.models';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './request-detail.component.html'
})
export class RequestDetailComponent implements OnInit {
  request: ApplicationRequestResponse | null = null;
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  selectedStatus: ApplicationRequestStatus = 'PENDING';
  statusRemarks = '';
  finalDocumentFile: File | null = null;

  showDeleteModal = false;
  documentToDelete: RequestDocumentResponse | null = null;

  readonly requestStatuses: ApplicationRequestStatus[] = [
    'PENDING',
    'DOCUMENTS_NEEDED',
    'IN_PROGRESS',
    'SUBMITTED',
    'COMPLETED',
    'REJECTED'
  ];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly adminApiService: AdminApiService
  ) {}

  ngOnInit(): void {
    this.loadRequest();
  }

  loadRequest(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Invalid request ID.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminApiService
      .getRequestById(id)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          this.request = response;
          this.selectedStatus = response.status;
          this.statusRemarks = response.remarks || '';
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  updateStatus(): void {
    if (!this.request) {
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminApiService
      .updateRequestStatus(this.request.id, {
        status: this.selectedStatus,
        remarks: this.statusRemarks.trim() || null
      })
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Request status updated successfully.';
          this.loadRequest();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  verifyPayment(targetStatus: 'PAID' | 'UNPAID'): void {
    if (!this.request) {
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminApiService
      .verifyPayment(this.request.id, {
        status: targetStatus,
        remarks: this.statusRemarks.trim() || null
      })
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.successMessage = `Payment marked as ${targetStatus}.`;
          this.loadRequest();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  onFinalDocumentSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.finalDocumentFile = input.files?.item(0) ?? null;
  }

  uploadFinalDocument(): void {
    if (!this.request || !this.finalDocumentFile) {
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminApiService
      .uploadFinalDocument(this.request.id, this.finalDocumentFile)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Final document uploaded successfully.';
          this.finalDocumentFile = null;
          this.loadRequest();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  openDeleteConfirmation(document: RequestDocumentResponse): void {
    this.documentToDelete = document;
    this.showDeleteModal = true;
  }

  closeDeleteConfirmation(): void {
    this.showDeleteModal = false;
    this.documentToDelete = null;
  }

  confirmDeleteDocument(): void {
    if (!this.request || !this.documentToDelete) {
      return;
    }
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.adminApiService
      .deleteRequestDocument(this.request.id, this.documentToDelete.id)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Document deleted successfully.';
          this.closeDeleteConfirmation();
          this.loadRequest();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  paymentStatusBadge(status: PaymentStatus): string {
    if (status === 'PAID') {
      return 'text-bg-success';
    }
    if (status === 'PROOF_SUBMITTED') {
      return 'text-bg-warning';
    }
    return 'text-bg-secondary';
  }
}
