export type UserRole = 'ADMIN' | 'CUSTOMER';
export type ServiceStatus = 'OPEN' | 'CLOSED' | 'COMING_SOON';
export type RequestStatus =
  | 'PENDING'
  | 'DOCUMENTS_NEEDED'
  | 'IN_PROGRESS'
  | 'SUBMITTED'
  | 'COMPLETED'
  | 'REJECTED';
export type PaymentStatus = 'UNPAID' | 'PROOF_SUBMITTED' | 'PAID';
export type PaymentMethod = 'CASH' | 'UPI_QR';
export type DeletionStatus = 'NONE' | 'PENDING' | 'APPROVED' | 'REJECTED';

export interface ApiMessage {
  message: string;
}

export interface AuthLoginRequest {
  mobile: string;
  pin: string;
}

export interface AuthRegisterRequest {
  name: string;
  mobile: string;
  email?: string | null;
  pin: string;
  address?: string | null;
}

export interface AuthResponse {
  userId: number;
  name: string;
  role: UserRole;
  token: string;
}

export interface CustomerProfileResponse {
  id: number;
  name: string;
  mobile: string | null;
  email: string | null;
  address: string | null;
}

export interface CustomerNameUpdateRequest {
  name: string;
}

export interface CategoryResponse {
  id: number;
  name: string;
  iconName: string | null;
  displayOrder: number;
  active: boolean;
}

export interface ServiceResponse {
  id: number;
  categoryId: number;
  categoryName: string;
  title: string;
  description: string;
  requiredDocumentsJson: string;
  applicantFieldsJson: string;
  govtFee: number;
  serviceFee: number;
  totalFee: number;
  status: ServiceStatus;
  openDate: string | null;
  closeDate: string | null;
}

export interface ApplicationCreateRequest {
  serviceId: number;
  remarks: string | null;
}

export interface RequestDocumentResponse {
  id: number;
  documentType: string;
  fileName: string;
  fileUrl: string;
  uploadedAt: string;
}

export interface PaymentResponse {
  id: number;
  amount: number;
  method: PaymentMethod;
  upiTransactionId: string | null;
  paymentProofUrl: string | null;
  status: PaymentStatus;
  createdAt: string;
}

export interface ApplicationRequestResponse {
  id: number;
  trackingId: string;
  customerId: number;
  customerName: string;
  service: ServiceResponse;
  status: RequestStatus;
  totalAmount: number;
  paymentStatus: PaymentStatus;
  remarks: string | null;
  createdAt: string;
  updatedAt: string;
  documents: RequestDocumentResponse[];
  latestPayment: PaymentResponse | null;
}

export interface PaymentSettingsResponse {
  upiQrImageUrl: string | null;
  shopUpiName: string | null;
  shopUpiId: string | null;
  centerName: string | null;
  centerMobile: string | null;
  centerWhatsappNumber: string | null;
  centerAddress: string | null;
  centerWorkingHours: string | null;
  officialUpiId: string | null;
  lastUpdatedAt: string | null;
}

export interface CenterInfoResponse {
  centerName: string | null;
  mobile: string | null;
  whatsappNumber: string | null;
  address: string | null;
  workingHours: string | null;
}

export interface PaymentProofSubmitRequest {
  method: PaymentMethod;
  upiTransactionId?: string;
  screenshot?: File;
}

export interface AccountDeletionRequestCreate {
  reason?: string | null;
}

export interface AccountDeletionStatusResponse {
  deletionStatus: DeletionStatus;
  deletionRequestedAt: string | null;
  deletionApprovedAt: string | null;
}

export type CustomerNotificationType = 'REQUEST_SUBMITTED' | 'PAYMENT_VERIFIED' | 'FINAL_DOCUMENT_READY';

export interface CustomerNotificationResponse {
  id: number;
  requestId: number | null;
  type: CustomerNotificationType;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface DraftApplicantFieldEntry {
  label: string;
  value: string;
}

export interface DraftApplicantDetails {
  entries: DraftApplicantFieldEntry[];
}

export interface DraftDocumentFile {
  id: string;
  documentType: string;
  fileName: string;
  mimeType: string;
  file: File;
  dataUrl: string;
  previewUrl: string | null;
  lastError?: string | null;
}

export interface RequestDraft {
  serviceId: number;
  serviceTitle: string;
  totalAmount: number;
  govtFee: number;
  serviceFee: number;
  applicantDetails: DraftApplicantDetails;
  documents: DraftDocumentFile[];
  createdRequestId?: number | null;
  trackingId?: string | null;
  failedDocumentIds?: string[];
}
