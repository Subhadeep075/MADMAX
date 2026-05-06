import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AccountDeletionRequestStatus,
  AdminPastRecordResponse,
  AdminDashboardResponse,
  AdminAccountDeletionRequestResponse,
  ApiMessage,
  ApplicationRequestResponse,
  ApplicationStatusUpdateRequest,
  CategoryResponse,
  CategoryUpsertRequest,
  PaymentSettingsResponse,
  PaymentVerifyRequest,
  ServiceResponse,
  ServiceUpsertRequest
} from '../../shared/models/api.models';
import { extractApiErrorMessage } from './api-error.util';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  getDashboard(): Observable<AdminDashboardResponse> {
    return this.mapApiError(this.http.get<AdminDashboardResponse>(`${this.baseUrl}/admin/dashboard`));
  }

  getCategories(): Observable<CategoryResponse[]> {
    return this.mapApiError(this.http.get<CategoryResponse[]>(`${this.baseUrl}/categories`));
  }

  getAdminCategories(): Observable<CategoryResponse[]> {
    return this.mapApiError(this.http.get<CategoryResponse[]>(`${this.baseUrl}/admin/categories`));
  }

  createCategory(payload: CategoryUpsertRequest): Observable<CategoryResponse> {
    return this.mapApiError(this.http.post<CategoryResponse>(`${this.baseUrl}/admin/categories`, payload));
  }

  updateCategory(id: number, payload: CategoryUpsertRequest): Observable<CategoryResponse> {
    return this.mapApiError(this.http.put<CategoryResponse>(`${this.baseUrl}/admin/categories/${id}`, payload));
  }

  deleteCategory(id: number): Observable<ApiMessage> {
    return this.mapApiError(this.http.delete<ApiMessage>(`${this.baseUrl}/admin/categories/${id}`));
  }

  getServices(): Observable<ServiceResponse[]> {
    return this.mapApiError(this.http.get<ServiceResponse[]>(`${this.baseUrl}/services`));
  }

  getServiceById(id: number): Observable<ServiceResponse> {
    return this.mapApiError(this.http.get<ServiceResponse>(`${this.baseUrl}/services/${id}`));
  }

  createService(payload: ServiceUpsertRequest): Observable<ServiceResponse> {
    return this.mapApiError(this.http.post<ServiceResponse>(`${this.baseUrl}/admin/services`, payload));
  }

  updateService(id: number, payload: ServiceUpsertRequest): Observable<ServiceResponse> {
    return this.mapApiError(this.http.put<ServiceResponse>(`${this.baseUrl}/admin/services/${id}`, payload));
  }

  deleteService(id: number): Observable<ApiMessage> {
    return this.mapApiError(this.http.delete<ApiMessage>(`${this.baseUrl}/admin/services/${id}`));
  }

  getAllRequests(): Observable<ApplicationRequestResponse[]> {
    return this.mapApiError(this.http.get<ApplicationRequestResponse[]>(`${this.baseUrl}/admin/requests`));
  }

  getRequestById(id: number): Observable<ApplicationRequestResponse> {
    return this.mapApiError(this.http.get<ApplicationRequestResponse>(`${this.baseUrl}/admin/requests/${id}`));
  }

  updateRequestStatus(id: number, payload: ApplicationStatusUpdateRequest): Observable<ApplicationRequestResponse> {
    return this.mapApiError(
      this.http.put<ApplicationRequestResponse>(`${this.baseUrl}/admin/requests/${id}/status`, payload)
    );
  }

  verifyPayment(id: number, payload: PaymentVerifyRequest): Observable<ApplicationRequestResponse> {
    return this.mapApiError(
      this.http.put<ApplicationRequestResponse>(`${this.baseUrl}/admin/requests/${id}/payment/verify`, payload)
    );
  }

  uploadFinalDocument(id: number, file: File): Observable<ApplicationRequestResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.mapApiError(this.http.post<ApplicationRequestResponse>(`${this.baseUrl}/admin/requests/${id}/final-document`, formData));
  }

  deleteRequestDocument(requestId: number, documentId: number): Observable<ApiMessage> {
    return this.mapApiError(this.http.delete<ApiMessage>(`${this.baseUrl}/requests/${requestId}/documents/${documentId}`));
  }

  getPaymentSettings(): Observable<PaymentSettingsResponse> {
    return this.mapApiError(this.http.get<PaymentSettingsResponse>(`${this.baseUrl}/payment-settings`));
  }

  updateUpiQr(
    file: File | null,
    shopUpiName: string,
    shopUpiId: string,
    centerName: string,
    officialUpiId: string,
    centerMobile: string,
    centerWhatsappNumber: string,
    centerAddress: string,
    centerWorkingHours: string
  ): Observable<PaymentSettingsResponse> {
    const formData = new FormData();
    if (file) {
      formData.append('qrImage', file);
    }

    if (shopUpiName.trim()) {
      formData.append('shopUpiName', shopUpiName.trim());
    }
    if (shopUpiId.trim()) {
      formData.append('shopUpiId', shopUpiId.trim());
    }
    if (centerName.trim()) {
      formData.append('centerName', centerName.trim());
    }
    if (officialUpiId.trim()) {
      formData.append('officialUpiId', officialUpiId.trim());
    }
    if (centerMobile.trim()) {
      formData.append('centerMobile', centerMobile.trim());
    }
    if (centerWhatsappNumber.trim()) {
      formData.append('centerWhatsappNumber', centerWhatsappNumber.trim());
    }
    if (centerAddress.trim()) {
      formData.append('centerAddress', centerAddress.trim());
    }
    if (centerWorkingHours.trim()) {
      formData.append('centerWorkingHours', centerWorkingHours.trim());
    }

    return this.mapApiError(this.http.put<PaymentSettingsResponse>(`${this.baseUrl}/admin/payment-settings/upi-qr`, formData));
  }

  getAccountDeletionRequests(
    status?: AccountDeletionRequestStatus
  ): Observable<AdminAccountDeletionRequestResponse[]> {
    const url = status
      ? `${this.baseUrl}/admin/account-deletion?status=${encodeURIComponent(status)}`
      : `${this.baseUrl}/admin/account-deletion`;

    return this.mapApiError(this.http.get<AdminAccountDeletionRequestResponse[]>(url));
  }

  approveAccountDeletion(requestId: number, remarks: string | null): Observable<ApiMessage> {
    return this.mapApiError(
      this.http.put<ApiMessage>(`${this.baseUrl}/admin/account-deletion/${requestId}/approve`, {
        remarks
      })
    );
  }

  rejectAccountDeletion(requestId: number, remarks: string | null): Observable<ApiMessage> {
    return this.mapApiError(
      this.http.put<ApiMessage>(`${this.baseUrl}/admin/account-deletion/${requestId}/reject`, {
        remarks
      })
    );
  }

  getPastRecords(fromDate?: string, toDate?: string): Observable<AdminPastRecordResponse[]> {
    const query = this.buildPastRecordQuery(fromDate, toDate);
    return this.mapApiError(this.http.get<AdminPastRecordResponse[]>(`${this.baseUrl}/admin/past-records${query}`));
  }

  downloadPastRecords(fromDate?: string, toDate?: string): Observable<Blob> {
    const query = this.buildPastRecordQuery(fromDate, toDate);
    return this.mapApiError(
      this.http.get(`${this.baseUrl}/admin/past-records/download${query}`, { responseType: 'blob' })
    );
  }

  deletePastRecord(requestId: number): Observable<ApiMessage> {
    return this.mapApiError(this.http.delete<ApiMessage>(`${this.baseUrl}/admin/past-records/${requestId}`));
  }

  private buildPastRecordQuery(fromDate?: string, toDate?: string): string {
    const params = new URLSearchParams();
    if (fromDate?.trim()) {
      params.set('fromDate', fromDate.trim());
    }
    if (toDate?.trim()) {
      params.set('toDate', toDate.trim());
    }
    const query = params.toString();
    return query ? `?${query}` : '';
  }

  private mapApiError<T>(request: Observable<T>): Observable<T> {
    return request.pipe(catchError((error: unknown) => throwError(() => new Error(extractApiErrorMessage(error)))));
  }
}
