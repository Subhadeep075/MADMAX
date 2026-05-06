import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AdminApiService } from '../../core/services/admin-api.service';
import { AdminPastRecordResponse, PaymentStatus } from '../../shared/models/api.models';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './past-records.component.html'
})
export class PastRecordsComponent implements OnInit {
  records: AdminPastRecordResponse[] = [];

  loading = false;
  downloading = false;
  deletingRecordId: number | null = null;
  errorMessage = '';
  successMessage = '';

  fromDate = '';
  toDate = '';
  readonly retentionDays = 30;

  constructor(private readonly adminApiService: AdminApiService) {}

  ngOnInit(): void {
    this.setDefaultDateRange();
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminApiService
      .getPastRecords(this.fromDate, this.toDate)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (records) => {
          this.records = records;
          this.successMessage = `Loaded ${records.length} record${records.length === 1 ? '' : 's'}.`;
        },
        error: (error: Error) => {
          this.records = [];
          this.errorMessage = error.message;
        }
      });
  }

  downloadCsv(): void {
    this.downloading = true;
    this.errorMessage = '';

    this.adminApiService
      .downloadPastRecords(this.fromDate, this.toDate)
      .pipe(finalize(() => (this.downloading = false)))
      .subscribe({
        next: (blob) => {
          const objectUrl = URL.createObjectURL(blob);
          const anchor = document.createElement('a');
          anchor.href = objectUrl;
          anchor.download = `past-records-${this.fromDate}-to-${this.toDate}.csv`;
          anchor.click();
          URL.revokeObjectURL(objectUrl);
          this.loadRecords();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  canDelete(record: AdminPastRecordResponse): boolean {
    return !!record.archivedAt;
  }

  deleteRecord(record: AdminPastRecordResponse): void {
    if (!this.canDelete(record) || this.deletingRecordId !== null) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    const confirmed = window.confirm(
      `Delete record ${record.trackingId} permanently?\n\n` +
      'This should be used only for valid operational/compliance reasons.\n' +
      'Please avoid removing records just to hide history.\n\n' +
      'This action cannot be undone.'
    );

    if (!confirmed) {
      return;
    }

    this.deletingRecordId = record.requestId;
    this.adminApiService
      .deletePastRecord(record.requestId)
      .pipe(finalize(() => (this.deletingRecordId = null)))
      .subscribe({
        next: (message) => {
          this.successMessage = message.message || 'Record deleted successfully.';
          this.records = this.records.filter((item) => item.requestId !== record.requestId);
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

  private setDefaultDateRange(): void {
    const today = new Date();
    const from = new Date(today);
    from.setDate(today.getDate() - (this.retentionDays - 1));

    this.fromDate = this.toDateInputValue(from);
    this.toDate = this.toDateInputValue(today);
  }

  private toDateInputValue(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
