import { HttpErrorResponse } from '@angular/common/http';

export function extractApiErrorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Something went wrong. Please try again.';
  }

  const payload = error.error;
  if (typeof payload === 'string' && payload.trim()) {
    return payload;
  }

  if (payload && typeof payload === 'object') {
    const maybeMessage = (payload as { message?: unknown }).message;
    if (typeof maybeMessage === 'string' && maybeMessage.trim()) {
      return maybeMessage;
    }

    const values = Object.values(payload as Record<string, unknown>)
      .filter((v): v is string => typeof v === 'string' && v.trim().length > 0);
    if (values.length > 0) {
      return values.join(', ');
    }
  }

  if (error.status === 0) {
    return 'Cannot connect to server. Check if backend is running.';
  }

  return `Request failed (${error.status}).`;
}
