import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AccountDeletionStatusResponse,
  ApiMessage,
  ApplicationCreateRequest,
  ApplicationRequestResponse,
  CategoryResponse,
  CenterInfoResponse,
  CustomerNameUpdateRequest,
  CustomerNotificationResponse,
  CustomerProfileResponse,
  PaymentProofSubmitRequest,
  PaymentResponse,
  PaymentSettingsResponse,
  RequestDocumentResponse,
  ServiceResponse
} from '../../shared/models/api.models';
import { extractApiErrorMessage } from './api-error.util';

@Injectable({ providedIn: 'root' })
export class CustomerApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  getCategories(): Observable<CategoryResponse[]> {
    return this.mapApiError(this.http.get<CategoryResponse[]>(`${this.baseUrl}/categories`));
  }

  getCenterInfo(): Observable<CenterInfoResponse> {
    return this.mapApiError(this.http.get<CenterInfoResponse>(`${this.baseUrl}/public/center-info`));
  }

  getServices(): Observable<ServiceResponse[]> {
    return this.mapApiError(this.http.get<ServiceResponse[]>(`${this.baseUrl}/services`));
  }

  getServiceById(id: number): Observable<ServiceResponse> {
    return this.mapApiError(this.http.get<ServiceResponse>(`${this.baseUrl}/services/${id}`));
  }

  createRequest(payload: ApplicationCreateRequest): Observable<ApplicationRequestResponse> {
    return this.mapApiError(this.http.post<ApplicationRequestResponse>(`${this.baseUrl}/requests`, payload));
  }

  getMyRequests(): Observable<ApplicationRequestResponse[]> {
    return this.mapApiError(this.http.get<ApplicationRequestResponse[]>(`${this.baseUrl}/requests/my`));
  }

  getRequestById(id: number): Observable<ApplicationRequestResponse> {
    return this.mapApiError(this.http.get<ApplicationRequestResponse>(`${this.baseUrl}/requests/${id}`));
  }

  uploadRequestDocument(requestId: number, documentType: string, file: File): Observable<RequestDocumentResponse> {
    return this.mapApiError(
      this.http.post<RequestDocumentResponse>(
        `${this.baseUrl}/requests/${requestId}/documents`,
        this.buildRequestDocumentFormData(documentType, file)
      )
    );
  }

  deleteRequestDocument(requestId: number, documentId: number): Observable<ApiMessage> {
    return this.mapApiError(this.http.delete<ApiMessage>(`${this.baseUrl}/requests/${requestId}/documents/${documentId}`));
  }

  getPaymentSettings(): Observable<PaymentSettingsResponse> {
    return this.mapApiError(this.http.get<PaymentSettingsResponse>(`${this.baseUrl}/payment-settings`));
  }

  submitPaymentProof(requestId: number, payload: PaymentProofSubmitRequest): Observable<PaymentResponse> {
    return this.mapApiError(
      this.http.post<PaymentResponse>(
        `${this.baseUrl}/requests/${requestId}/payment-proof`,
        this.buildPaymentProofFormData(payload)
      )
    );
  }

  requestAccountDeletion(reason?: string | null): Observable<ApiMessage> {
    return this.mapApiError(
      this.http.post<ApiMessage>(`${this.baseUrl}/account-deletion/request`, {
        reason: reason?.trim() || null
      })
    );
  }

  getAccountDeletionStatus(): Observable<AccountDeletionStatusResponse> {
    return this.mapApiError(this.http.get<AccountDeletionStatusResponse>(`${this.baseUrl}/account-deletion/status`));
  }

  getNotifications(limit = 20): Observable<CustomerNotificationResponse[]> {
    return this.mapApiError(
      this.http.get<CustomerNotificationResponse[]>(`${this.baseUrl}/notifications?limit=${Math.max(1, Math.min(100, limit))}`)
    );
  }

  markNotificationRead(notificationId: number): Observable<ApiMessage> {
    return this.mapApiError(this.http.put<ApiMessage>(`${this.baseUrl}/notifications/${notificationId}/read`, {}));
  }

  getMyProfile(): Observable<CustomerProfileResponse> {
    return this.mapApiError(this.http.get<CustomerProfileResponse>(`${this.baseUrl}/customer/profile`));
  }

  updateMyName(payload: CustomerNameUpdateRequest): Observable<CustomerProfileResponse> {
    return this.mapApiError(this.http.put<CustomerProfileResponse>(`${this.baseUrl}/customer/profile/name`, payload));
  }

  private buildRequestDocumentFormData(documentType: string, file: File): FormData {
    const formData = new FormData();
    formData.append('documentType', documentType);
    formData.append('file', file, file.name);
    return formData;
  }

  private buildPaymentProofFormData(payload: PaymentProofSubmitRequest): FormData {
    const formData = new FormData();
    formData.append('method', payload.method);
    if (payload.upiTransactionId && payload.upiTransactionId.trim()) {
      formData.append('upiTransactionId', payload.upiTransactionId.trim());
    }
    if (payload.screenshot) {
      formData.append('screenshot', payload.screenshot, payload.screenshot.name);
    }
    return formData;
  }

  private mapApiError<T>(request: Observable<T>): Observable<T> {
    return request.pipe(catchError((error: unknown) => throwError(() => new Error(extractApiErrorMessage(error)))));
  }
}
