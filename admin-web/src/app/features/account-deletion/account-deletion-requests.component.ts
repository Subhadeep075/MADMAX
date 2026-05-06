import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AdminApiService } from '../../core/services/admin-api.service';
import {
  AccountDeletionRequestStatus,
  AdminAccountDeletionRequestResponse
} from '../../shared/models/api.models';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './account-deletion-requests.component.html'
})
export class AccountDeletionRequestsComponent implements OnInit {
  requests: AdminAccountDeletionRequestResponse[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  activeActionRequestId: number | null = null;
  statusFilter: AccountDeletionRequestStatus | '' = '';

  readonly statuses: AccountDeletionRequestStatus[] = ['PENDING', 'APPROVED', 'REJECTED'];

  constructor(private readonly adminApiService: AdminApiService) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const status = this.statusFilter || undefined;
    this.adminApiService
      .getAccountDeletionRequests(status)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          this.requests = response;
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  approveRequest(item: AdminAccountDeletionRequestResponse): void {
    const confirmed = window.confirm(
      'User will be marked for deletion and removed permanently after the safety window. Continue?'
    );
    if (!confirmed) {
      return;
    }

    const remarks = window.prompt('Optional admin remarks:', item.adminRemarks ?? '') ?? '';
    this.activeActionRequestId = item.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminApiService
      .approveAccountDeletion(item.id, remarks.trim() || null)
      .pipe(finalize(() => (this.activeActionRequestId = null)))
      .subscribe({
        next: (response) => {
          this.successMessage = response.message;
          this.loadRequests();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  rejectRequest(item: AdminAccountDeletionRequestResponse): void {
    const remarksInput = window.prompt('Add rejection remarks (optional):', item.adminRemarks ?? '');
    if (remarksInput === null) {
      return;
    }

    this.activeActionRequestId = item.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminApiService
      .rejectAccountDeletion(item.id, remarksInput.trim() || null)
      .pipe(finalize(() => (this.activeActionRequestId = null)))
      .subscribe({
        next: (response) => {
          this.successMessage = response.message;
          this.loadRequests();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  canProcess(item: AdminAccountDeletionRequestResponse): boolean {
    return item.status === 'PENDING';
  }
}
