import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { CustomerLoginPage } from './features/auth/customer-login.page';
import { CustomerRegisterPage } from './features/auth/customer-register.page';
import { HomePage } from './features/home/home.page';
import { LanguageSelectPage } from './features/language/language-select.page';
import { StartupRedirectPage } from './features/language/startup-redirect.page';
import { PaymentPage } from './features/payments/payment.page';
import { CustomerNotificationsPage } from './features/notifications/customer-notifications.page';
import { ProfilePage } from './features/profile/profile.page';
import { MyRequestsPage } from './features/requests/my-requests.page';
import { RequestDetailPage } from './features/requests/request-detail.page';
import { RequestFormPage } from './features/requests/request-form.page';
import { ReviewSubmitPage } from './features/requests/review-submit.page';
import { ServiceDetailPage } from './features/services/service-detail.page';
import { ServiceListPage } from './features/services/service-list.page';
import { CustomerTabsPage } from './features/tabs/customer-tabs.page';

export const routes: Routes = [
  { path: '', component: StartupRedirectPage, pathMatch: 'full' },
  { path: 'language', component: LanguageSelectPage },
  { path: 'login', component: CustomerLoginPage },
  { path: 'register', component: CustomerRegisterPage },
  {
    path: 'app',
    component: CustomerTabsPage,
    canActivate: [authGuard],
    children: [
      { path: 'home', component: HomePage },
      { path: 'services', component: ServiceListPage },
      { path: 'requests', component: MyRequestsPage },
      { path: 'notifications', component: CustomerNotificationsPage },
      { path: 'profile', component: ProfilePage },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },
  { path: 'service/:id', component: ServiceDetailPage, canActivate: [authGuard] },
  { path: 'request-form/:serviceId', component: RequestFormPage, canActivate: [authGuard] },
  { path: 'review-submit/:serviceId', component: ReviewSubmitPage, canActivate: [authGuard] },
  { path: 'payment/:requestId', component: PaymentPage, canActivate: [authGuard] },
  { path: 'request-detail/:id', component: RequestDetailPage, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
