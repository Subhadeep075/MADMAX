import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { CustomerApiService } from '../services/customer-api.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const customerApiService = inject(CustomerApiService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigateByUrl('/login');
    return false;
  }

  if (!authService.hasRole('CUSTOMER')) {
    authService.clearSession();
    router.navigateByUrl('/login');
    return false;
  }

  if (authService.getAccountDeletionStatus() === 'APPROVED') {
    authService.clearSession();
    router.navigateByUrl('/login');
    return false;
  }

  return customerApiService.getAccountDeletionStatus().pipe(
    map((response) => {
      authService.setAccountDeletionStatus(response.deletionStatus);
      if (response.deletionStatus === 'APPROVED') {
        authService.clearSession();
        router.navigateByUrl('/login');
        return false;
      }
      return true;
    }),
    catchError(() => of(true))
  );
};
