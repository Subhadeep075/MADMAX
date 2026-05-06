import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthLoginRequest, AuthRegisterRequest, AuthResponse } from '../../shared/models/api.models';
import { extractApiErrorMessage } from './api-error.util';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  login(payload: AuthLoginRequest): Observable<AuthResponse> {
    return this.mapApiError(this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, payload));
  }

  register(payload: AuthRegisterRequest): Observable<AuthResponse> {
    return this.mapApiError(this.http.post<AuthResponse>(`${this.baseUrl}/auth/register`, payload));
  }

  private mapApiError<T>(request: Observable<T>): Observable<T> {
    return request.pipe(catchError((error: unknown) => throwError(() => new Error(extractApiErrorMessage(error)))));
  }
}
