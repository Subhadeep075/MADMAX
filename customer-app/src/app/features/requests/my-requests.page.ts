import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';
import { finalize } from 'rxjs';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { PlatformService } from '../../core/services/platform.service';
import { ApplicationRequestResponse, PaymentStatus, RequestStatus } from '../../shared/models/api.models';

@Component({
  standalone: true,
  selector: 'app-my-requests-page',
  imports: [CommonModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './my-requests.page.html'
})
export class MyRequestsPage implements OnInit {
  readonly enablePullToRefresh: boolean;

  requests: ApplicationRequestResponse[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly api: CustomerApiService,
    private readonly router: Router,
    private readonly platformService: PlatformService
  ) {
    this.enablePullToRefresh = this.platformService.isNativeApp();
  }

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(event?: CustomEvent): void {
    this.loading = true;
    this.errorMessage = '';

    this.api
      .getMyRequests()
      .pipe(
        finalize(() => {
          this.loading = false;
          event?.detail?.complete();
        })
      )
      .subscribe({
        next: (requests) => {
          this.requests = requests;
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  openDetail(requestId: number): void {
    this.router.navigate(['/request-detail', requestId]);
  }

  statusColor(status: RequestStatus): 'warning' | 'primary' | 'secondary' | 'success' | 'danger' {
    if (status === 'PENDING' || status === 'DOCUMENTS_NEEDED') {
      return 'warning';
    }
    if (status === 'IN_PROGRESS' || status === 'SUBMITTED') {
      return 'primary';
    }
    if (status === 'COMPLETED') {
      return 'success';
    }
    return 'danger';
  }

  paymentColor(status: PaymentStatus): 'medium' | 'warning' | 'success' {
    if (status === 'UNPAID') {
      return 'medium';
    }
    if (status === 'PROOF_SUBMITTED') {
      return 'warning';
    }
    return 'success';
  }
}
