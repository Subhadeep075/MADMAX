import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { IonApp, IonBadge, IonButton, IonIcon, IonRouterOutlet } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { filter, interval, Subscription } from 'rxjs';
import { arrowBackOutline, gridOutline, homeOutline, listOutline, notificationsOutline, personOutline } from 'ionicons/icons';
import { AuthService } from './core/auth/auth.service';
import { CustomerApiService } from './core/services/customer-api.service';
import { PlatformService } from './core/services/platform.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, IonApp, IonRouterOutlet, IonButton, IonBadge, IonIcon],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit, OnDestroy {
  readonly isBrowser: boolean;
  showCustomerNotificationIcon = false;
  unreadNotificationCount = 0;

  private readonly hiddenNotificationRoutes = ['/login', '/register', '/language'];
  private readonly maxNotificationBadge = 99;
  private readonly subscriptions = new Subscription();

  constructor(
    private readonly platformService: PlatformService,
    private readonly authService: AuthService,
    private readonly customerApiService: CustomerApiService,
    private readonly router: Router
  ) {
    this.isBrowser = this.platformService.isBrowser();

    addIcons({
      homeOutline,
      gridOutline,
      listOutline,
      personOutline,
      arrowBackOutline,
      notificationsOutline
    });
  }

  ngOnInit(): void {
    this.updateNotificationIconVisibility(this.router.url);

    this.subscriptions.add(
      this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe((event) => {
        const navigation = event as NavigationEnd;
        this.updateNotificationIconVisibility(navigation.urlAfterRedirects || navigation.url);
      })
    );

    this.subscriptions.add(
      interval(45000).subscribe(() => {
        if (this.showCustomerNotificationIcon) {
          this.refreshUnreadNotificationCount();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  openNotifications(): void {
    void this.router.navigateByUrl('/app/notifications');
  }

  get unreadNotificationLabel(): string {
    if (this.unreadNotificationCount <= 0) {
      return 'No unread notifications';
    }
    return `${this.unreadNotificationCount} unread notification${this.unreadNotificationCount > 1 ? 's' : ''}`;
  }

  get displayUnreadCount(): string {
    if (this.unreadNotificationCount > this.maxNotificationBadge) {
      return `${this.maxNotificationBadge}+`;
    }
    return String(this.unreadNotificationCount);
  }

  private updateNotificationIconVisibility(url: string): void {
    const normalizedRoute = this.normalizeRoute(url);
    const shouldHideForRoute = this.hiddenNotificationRoutes.some((route) => normalizedRoute.startsWith(route));
    const shouldShow = this.authService.isLoggedIn() && !shouldHideForRoute;

    this.showCustomerNotificationIcon = shouldShow;
    if (!shouldShow) {
      this.unreadNotificationCount = 0;
      return;
    }

    this.refreshUnreadNotificationCount();
  }

  private refreshUnreadNotificationCount(): void {
    this.customerApiService.getNotifications(50).subscribe({
      next: (notifications) => {
        this.unreadNotificationCount = notifications.filter((notification) => !notification.read).length;
      },
      error: () => {
        // Keep previous value to avoid noisy UI jumps during temporary failures.
      }
    });
  }

  private normalizeRoute(url: string): string {
    const withoutHash = url.startsWith('#') ? url.slice(1) : url;
    const withoutQuery = withoutHash.split('?')[0];
    return withoutQuery.startsWith('/') ? withoutQuery : `/${withoutQuery}`;
  }
}
