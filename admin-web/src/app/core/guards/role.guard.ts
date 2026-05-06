import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';
import { UserRole } from '../../shared/models/api.models';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const expectedRole = route.data['role'] as UserRole;

  if (authService.hasRole(expectedRole)) {
    return true;
  }

  router.navigateByUrl('/login');
  return false;
};
