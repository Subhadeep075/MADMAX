import { HttpErrorResponse } from '@angular/common/http';

const FALLBACK_ERROR = 'Something went wrong. Please try again.';

export function extractApiErrorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return FALLBACK_ERROR;
  }

  if (typeof error.error === 'string' && error.error.trim()) {
    return error.error;
  }

  if (error.error && typeof error.error.message === 'string' && error.error.message.trim()) {
    return error.error.message;
  }

  if (error.status === 0) {
    return 'Network problem. Please check internet and try again.';
  }

  if (error.status === 401 || error.status === 403) {
    return 'Session expired or access denied. Please login again.';
  }

  return FALLBACK_ERROR;
}
