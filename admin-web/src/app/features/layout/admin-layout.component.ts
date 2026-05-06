import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { AdminApiService } from '../../core/services/admin-api.service';
import { ApplicationRequestStatus, PaymentStatus } from '../../shared/models/api.models';

interface NavItem {
  label: string;
  link: string;
  badgeKey?: 'pendingDeletion';
}

@Component({
  standalone: true,
  selector: 'app-admin-layout',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.component.html'
})
export class AdminLayoutComponent implements OnInit, OnDestroy {
  readonly navItems: NavItem[] = [
    { label: 'Dashboard', link: '/dashboard' },
    { label: 'Categories', link: '/categories' },
    { label: 'Services', link: '/services' },
    { label: 'Request Queue', link: '/requests' },
    { label: 'Payment Settings', link: '/payment-settings' },
    { label: 'Account Deletion', link: '/account-deletion', badgeKey: 'pendingDeletion' },
    { label: 'Past Records', link: '/past-records' }
  ];
  pendingDeletionCount = 0;
  requestNotificationCount = 0;
  pendingRequestCount = 0;
  paymentProofAlertCount = 0;
  showNotificationPanel = false;
  private pendingCountPollSubscription: Subscription | null = null;

  constructor(
    public readonly authService: AuthService,
    private readonly adminApiService: AdminApiService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.refreshTopbarCounts();
    this.pendingCountPollSubscription = interval(60000).subscribe(() => this.refreshTopbarCounts());
  }

  ngOnDestroy(): void {
    this.pendingCountPollSubscription?.unsubscribe();
  }

  getBadgeCount(item: NavItem): number | null {
    if (item.badgeKey === 'pendingDeletion' && this.pendingDeletionCount > 0) {
      return this.pendingDeletionCount;
    }
    return null;
  }

  logout(): void {
    this.authService.clearSession();
    this.router.navigateByUrl('/login');
  }

  toggleNotificationPanel(): void {
    this.showNotificationPanel = !this.showNotificationPanel;
  }

  closeNotificationPanel(): void {
    this.showNotificationPanel = false;
  }

  openRequestQueue(filters?: { status?: ApplicationRequestStatus; paymentStatus?: PaymentStatus }): void {
    this.showNotificationPanel = false;
    void this.router.navigate(['/requests'], {
      queryParams: {
        status: filters?.status || null,
        paymentStatus: filters?.paymentStatus || null
      }
    });
  }

  openPendingRequestNotifications(): void {
    this.openRequestQueue({ status: 'PENDING' });
  }

  openPaymentProofNotifications(): void {
    this.openRequestQueue({ paymentStatus: 'PROOF_SUBMITTED' });
  }

  get hasRequestNotifications(): boolean {
    return this.requestNotificationCount > 0;
  }

  get requestNotificationLabel(): string {
    if (!this.hasRequestNotifications) {
      return 'No new request alerts';
    }
    return `${this.requestNotificationCount} request alert${this.requestNotificationCount > 1 ? 's' : ''}`;
  }

  get pendingRequestAlerts(): number {
    return this.pendingRequestCount;
  }

  get pendingPaymentProofAlerts(): number {
    return this.paymentProofAlertCount;
  }

  private refreshTopbarCounts(): void {
    this.refreshPendingDeletionCount();
    this.refreshRequestNotificationCount();
  }

  private refreshPendingDeletionCount(): void {
    this.adminApiService.getAccountDeletionRequests('PENDING').subscribe({
      next: (requests) => {
        this.pendingDeletionCount = requests.length;
      },
      error: () => {
        // Keep previous value if request fails.
      }
    });
  }

  private refreshRequestNotificationCount(): void {
    this.adminApiService.getDashboard().subscribe({
      next: (dashboard) => {
        this.pendingRequestCount = dashboard.pendingRequests;
        this.paymentProofAlertCount = dashboard.proofSubmittedPayments;
        this.requestNotificationCount = this.pendingRequestCount + this.paymentProofAlertCount;
      },
      error: () => {
        // Keep previous value if request fails.
      }
    });
  }
}
