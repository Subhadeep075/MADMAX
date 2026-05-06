import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthLoginRequest, AuthResponse } from '../../shared/models/api.models';
import { extractApiErrorMessage } from './api-error.util';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  login(payload: AuthLoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, payload).pipe(
      catchError((error: unknown) => throwError(() => new Error(extractApiErrorMessage(error))))
    );
  }
}
