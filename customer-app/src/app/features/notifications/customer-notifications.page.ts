import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { PlatformService } from '../../core/services/platform.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { CustomerNotificationResponse } from '../../shared/models/api.models';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';

@Component({
  standalone: true,
  selector: 'app-customer-notifications-page',
  imports: [CommonModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './customer-notifications.page.html'
})
export class CustomerNotificationsPage implements OnInit {
  readonly enablePullToRefresh: boolean;

  notifications: CustomerNotificationResponse[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly customerApiService: CustomerApiService,
    private readonly router: Router,
    private readonly platformService: PlatformService,
    readonly i18n: I18nService
  ) {
    this.enablePullToRefresh = this.platformService.isNativeApp();
  }

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(event?: CustomEvent): void {
    this.loading = true;
    this.errorMessage = '';

    this.customerApiService
      .getNotifications(50)
      .pipe(
        finalize(() => {
          this.loading = false;
          event?.detail?.complete();
        })
      )
      .subscribe({
        next: (response) => {
          this.notifications = response;
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
          this.notifications = [];
        }
      });
  }

  openNotification(notification: CustomerNotificationResponse): void {
    if (!notification.read) {
      this.markAsRead(notification.id);
    }

    if (notification.requestId) {
      void this.router.navigate(['/request-detail', notification.requestId]);
      return;
    }
    void this.router.navigate(['/app/requests']);
  }

  markAsRead(notificationId: number): void {
    this.customerApiService.markNotificationRead(notificationId).subscribe({
      next: () => {
        this.notifications = this.notifications.map((item) =>
          item.id === notificationId ? { ...item, read: true } : item
        );
      },
      error: () => {
        // Do nothing. This is a non-blocking action.
      }
    });
  }
}
