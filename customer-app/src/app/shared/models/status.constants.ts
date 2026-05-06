import { RequestStatus } from './api.models';

export const REQUEST_TIMELINE: RequestStatus[] = [
  'PENDING',
  'DOCUMENTS_NEEDED',
  'IN_PROGRESS',
  'SUBMITTED',
  'COMPLETED',
  'REJECTED'
];
