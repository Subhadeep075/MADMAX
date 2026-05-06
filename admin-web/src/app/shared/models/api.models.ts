export type UserRole = 'ADMIN' | 'CUSTOMER';

export type ServiceStatus = 'OPEN' | 'CLOSED' | 'COMING_SOON';
export type ApplicationRequestStatus =
  | 'PENDING'
  | 'DOCUMENTS_NEEDED'
  | 'IN_PROGRESS'
  | 'SUBMITTED'
  | 'COMPLETED'
  | 'REJECTED';
export type PaymentStatus = 'UNPAID' | 'PROOF_SUBMITTED' | 'PAID';
export type PaymentMethod = 'CASH' | 'UPI_QR';
export type AccountDeletionRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface ApiMessage {
  message: string;
}

export interface AuthLoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  userId: number;
  name: string;
  role: UserRole;
  token: string;
}

export interface AdminDashboardResponse {
  totalRequests: number;
  pendingRequests: number;
  inProgressRequests: number;
  completedRequests: number;
  proofSubmittedPayments: number;
}

export interface CategoryResponse {
  id: number;
  name: string;
  iconName: string | null;
  displayOrder: number;
  active: boolean;
}

export interface CategoryUpsertRequest {
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

export interface ServiceUpsertRequest {
  categoryId: number;
  title: string;
  description: string;
  requiredDocumentsJson: string;
  applicantFieldsJson: string;
  govtFee: number;
  serviceFee: number;
  status: ServiceStatus;
  openDate: string | null;
  closeDate: string | null;
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
  status: ApplicationRequestStatus;
  totalAmount: number;
  paymentStatus: PaymentStatus;
  remarks: string | null;
  createdAt: string;
  updatedAt: string;
  documents: RequestDocumentResponse[];
  latestPayment: PaymentResponse | null;
}

export interface ApplicationCreateRequest {
  serviceId: number;
  remarks: string | null;
}

export interface ApplicationStatusUpdateRequest {
  status: ApplicationRequestStatus;
  remarks: string | null;
}

export interface PaymentVerifyRequest {
  status: 'PAID' | 'UNPAID';
  remarks: string | null;
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

export interface AdminAccountDeletionRequestResponse {
  id: number;
  userId: number;
  userName: string;
  userMobile: string | null;
  userEmail: string | null;
  status: AccountDeletionRequestStatus;
  reason: string | null;
  adminRemarks: string | null;
  requestedAt: string;
  processedAt: string | null;
  processedByAdminId: number | null;
}

export interface AdminPastRecordResponse {
  requestId: number;
  trackingId: string;
  customerName: string;
  customerMobile: string | null;
  customerEmail: string | null;
  serviceTitle: string;
  categoryName: string;
  requestStatus: ApplicationRequestStatus;
  paymentStatus: PaymentStatus;
  govtFee: number;
  serviceFee: number;
  totalAmount: number;
  latestPaymentMethod: PaymentMethod | null;
  latestUpiTransactionId: string | null;
  documentCount: number;
  remarks: string | null;
  archivedAt: string | null;
  archiveSource: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface AccountDeletionDecisionRequest {
  remarks: string | null;
}
