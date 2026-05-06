import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { AdminLoginComponent } from './features/auth/admin-login.component';
import { AdminLayoutComponent } from './features/layout/admin-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { CategoryManagementComponent } from './features/categories/category-management.component';
import { ServiceManagementComponent } from './features/services/service-management.component';
import { RequestQueueComponent } from './features/requests/request-queue.component';
import { RequestDetailComponent } from './features/requests/request-detail.component';
import { PaymentSettingsComponent } from './features/payment-settings/payment-settings.component';
import { AccountDeletionRequestsComponent } from './features/account-deletion/account-deletion-requests.component';
import { PastRecordsComponent } from './features/past-records/past-records.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: AdminLoginComponent },
  {
    path: '',
    component: AdminLayoutComponent,
    canActivate: [authGuard, roleGuard],
    data: { role: 'ADMIN' },
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'categories', component: CategoryManagementComponent },
      { path: 'services', component: ServiceManagementComponent },
      { path: 'requests', component: RequestQueueComponent },
      { path: 'requests/:id', component: RequestDetailComponent },
      { path: 'past-records', component: PastRecordsComponent },
      { path: 'payment-settings', component: PaymentSettingsComponent },
      { path: 'account-deletion', component: AccountDeletionRequestsComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
