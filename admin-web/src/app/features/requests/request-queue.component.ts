import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap, RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { AdminApiService } from '../../core/services/admin-api.service';
import {
  ApplicationRequestResponse,
  ApplicationRequestStatus,
  PaymentStatus,
  ServiceResponse
} from '../../shared/models/api.models';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './request-queue.component.html'
})
export class RequestQueueComponent implements OnInit {
  requests: ApplicationRequestResponse[] = [];
  filteredRequests: ApplicationRequestResponse[] = [];
  services: ServiceResponse[] = [];

  loading = false;
  errorMessage = '';

  statusFilter = '';
  paymentStatusFilter = '';
  serviceFilter = '';
  fromDate = '';
  toDate = '';

  readonly requestStatuses: ApplicationRequestStatus[] = [
    'PENDING',
    'DOCUMENTS_NEEDED',
    'IN_PROGRESS',
    'SUBMITTED',
    'COMPLETED',
    'REJECTED'
  ];
  readonly paymentStatuses: PaymentStatus[] = ['UNPAID', 'PROOF_SUBMITTED', 'PAID'];

  constructor(
    private readonly adminApiService: AdminApiService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.applyFiltersFromQuery(params);
    });
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    forkJoin({
      requests: this.adminApiService.getAllRequests(),
      services: this.adminApiService.getServices()
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ requests, services }) => {
          this.requests = requests;
          this.services = services;
          this.applyFilters();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  applyFilters(): void {
    const from = this.fromDate ? new Date(this.fromDate) : null;
    const to = this.toDate ? new Date(this.toDate) : null;
    if (to) {
      to.setHours(23, 59, 59, 999);
    }

    this.filteredRequests = this.requests.filter((item) => {
      const byStatus = !this.statusFilter || item.status === this.statusFilter;
      const byPayment = !this.paymentStatusFilter || item.paymentStatus === this.paymentStatusFilter;
      const byService = !this.serviceFilter || String(item.service.id) === this.serviceFilter;
      const createdAt = new Date(item.createdAt);
      const byFrom = !from || createdAt >= from;
      const byTo = !to || createdAt <= to;

      return byStatus && byPayment && byService && byFrom && byTo;
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

  private applyFiltersFromQuery(params: ParamMap): void {
    this.statusFilter = this.parseStatusFilter(params.get('status'));
    this.paymentStatusFilter = this.parsePaymentStatusFilter(params.get('paymentStatus'));
    this.serviceFilter = this.parseServiceFilter(params.get('serviceId'));
    this.fromDate = this.parseDateFilter(params.get('fromDate'));
    this.toDate = this.parseDateFilter(params.get('toDate'));
    this.applyFilters();
  }

  private parseStatusFilter(value: string | null): ApplicationRequestStatus | '' {
    if (!value) {
      return '';
    }
    return this.requestStatuses.includes(value as ApplicationRequestStatus) ? (value as ApplicationRequestStatus) : '';
  }

  private parsePaymentStatusFilter(value: string | null): PaymentStatus | '' {
    if (!value) {
      return '';
    }
    return this.paymentStatuses.includes(value as PaymentStatus) ? (value as PaymentStatus) : '';
  }

  private parseServiceFilter(value: string | null): string {
    if (!value) {
      return '';
    }
    return /^[0-9]+$/.test(value) ? value : '';
  }

  private parseDateFilter(value: string | null): string {
    if (!value) {
      return '';
    }
    return /^\d{4}-\d{2}-\d{2}$/.test(value) ? value : '';
  }
}
