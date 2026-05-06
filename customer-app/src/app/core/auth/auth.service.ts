import { Injectable } from '@angular/core';
import { AuthResponse, UserRole } from '../../shared/models/api.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'dcs_customer_token';
  private readonly roleKey = 'dcs_customer_role';
  private readonly nameKey = 'dcs_customer_name';
  private readonly userIdKey = 'dcs_customer_user_id';
  private readonly userIdentifierKey = 'dcs_customer_user_identifier';
  private readonly accountDeletionStatusKey = 'dcs_customer_account_deletion_status';

  setSession(response: AuthResponse, userIdentifier: string): void {
    localStorage.setItem(this.tokenKey, response.token);
    localStorage.setItem(this.roleKey, response.role);
    localStorage.setItem(this.nameKey, response.name);
    localStorage.setItem(this.userIdKey, String(response.userId));
    localStorage.setItem(this.userIdentifierKey, userIdentifier);
  }

  clearSession(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.roleKey);
    localStorage.removeItem(this.nameKey);
    localStorage.removeItem(this.userIdKey);
    localStorage.removeItem(this.userIdentifierKey);
    localStorage.removeItem(this.accountDeletionStatusKey);
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

  setName(name: string): void {
    localStorage.setItem(this.nameKey, name);
  }

  getUserId(): number | null {
    const value = localStorage.getItem(this.userIdKey);
    if (!value) {
      return null;
    }
    const parsed = Number(value);
    return Number.isNaN(parsed) ? null : parsed;
  }

  getUserIdentifier(): string | null {
    return localStorage.getItem(this.userIdentifierKey);
  }

  setAccountDeletionStatus(status: string | null): void {
    if (!status) {
      localStorage.removeItem(this.accountDeletionStatusKey);
      return;
    }
    localStorage.setItem(this.accountDeletionStatusKey, status);
  }

  getAccountDeletionStatus(): string | null {
    return localStorage.getItem(this.accountDeletionStatusKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasRole(expectedRole: UserRole): boolean {
    return this.getRole() === expectedRole;
  }
}
