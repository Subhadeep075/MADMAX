import { Injectable } from '@angular/core';
import { UserRole } from '../../shared/models/api.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'dcs_admin_token';
  private readonly roleKey = 'dcs_admin_role';
  private readonly nameKey = 'dcs_admin_name';

  setSession(token: string, role: UserRole, name: string): void {
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.roleKey, role);
    localStorage.setItem(this.nameKey, name);
  }

  clearSession(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.roleKey);
    localStorage.removeItem(this.nameKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRole(): UserRole | null {
    const role = localStorage.getItem(this.roleKey);
    if (role === 'ADMIN' || role === 'CUSTOMER') {
      return role;
    }
    return null;
  }

  getName(): string | null {
    return localStorage.getItem(this.nameKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasRole(expectedRole: UserRole): boolean {
    return this.getRole() === expectedRole;
  }
}
