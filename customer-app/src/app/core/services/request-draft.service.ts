import { Injectable } from '@angular/core';
import { DraftDocumentFile, RequestDraft } from '../../shared/models/api.models';

interface StoredDraftDocument {
  id: string;
  documentType: string;
  fileName: string;
  mimeType: string;
  dataUrl: string;
}

interface StoredRequestDraft {
  serviceId: number;
  serviceTitle: string;
  totalAmount: number;
  govtFee: number;
  serviceFee: number;
  applicantDetails: RequestDraft['applicantDetails'];
  documents: StoredDraftDocument[];
  createdRequestId?: number | null;
  trackingId?: string | null;
  failedDocumentIds?: string[];
}

@Injectable({ providedIn: 'root' })
export class RequestDraftService {
  private readonly storageKey = 'dcs_customer_request_drafts_v2';
  private draftByServiceId = new Map<number, RequestDraft>();
  private loadedFromStorage = false;

  saveDraft(draft: RequestDraft): void {
    this.draftByServiceId.set(draft.serviceId, draft);
    this.persistToLocalStorage();
  }

  getDraft(serviceId: number): RequestDraft | null {
    this.loadFromLocalStorageIfNeeded();
    return this.draftByServiceId.get(serviceId) ?? null;
  }

  clearDraft(serviceId: number): void {
    this.loadFromLocalStorageIfNeeded();
    const existing = this.draftByServiceId.get(serviceId);
    if (existing) {
      this.revokeDocumentPreviews(existing.documents);
    }
    this.draftByServiceId.delete(serviceId);
    this.persistToLocalStorage();
  }

  clearAll(): void {
    this.loadFromLocalStorageIfNeeded();
    this.draftByServiceId.forEach((draft) => this.revokeDocumentPreviews(draft.documents));
    this.draftByServiceId.clear();
    this.persistToLocalStorage();
  }

  private persistToLocalStorage(): void {
    const storedDrafts: StoredRequestDraft[] = Array.from(this.draftByServiceId.values()).map((draft) => ({
      serviceId: draft.serviceId,
      serviceTitle: draft.serviceTitle,
      totalAmount: draft.totalAmount,
      govtFee: draft.govtFee,
      serviceFee: draft.serviceFee,
      applicantDetails: draft.applicantDetails,
      createdRequestId: draft.createdRequestId ?? null,
      trackingId: draft.trackingId ?? null,
      failedDocumentIds: draft.failedDocumentIds ?? [],
      documents: draft.documents.map((document) => ({
        id: document.id,
        documentType: document.documentType,
        fileName: document.fileName,
        mimeType: document.mimeType,
        dataUrl: document.dataUrl
      }))
    }));

    localStorage.setItem(this.storageKey, JSON.stringify(storedDrafts));
  }

  private loadFromLocalStorageIfNeeded(): void {
    if (this.loadedFromStorage) {
      return;
    }
    this.loadedFromStorage = true;

    const raw = localStorage.getItem(this.storageKey);
    if (!raw?.trim()) {
      return;
    }

    try {
      const parsed = JSON.parse(raw) as StoredRequestDraft[];
      parsed.forEach((storedDraft) => {
        const draft: RequestDraft = {
          serviceId: storedDraft.serviceId,
          serviceTitle: storedDraft.serviceTitle,
          totalAmount: storedDraft.totalAmount,
          govtFee: storedDraft.govtFee,
          serviceFee: storedDraft.serviceFee,
          applicantDetails: storedDraft.applicantDetails,
          createdRequestId: storedDraft.createdRequestId ?? null,
          trackingId: storedDraft.trackingId ?? null,
          failedDocumentIds: storedDraft.failedDocumentIds ?? [],
          documents: storedDraft.documents
            .map((document) => this.restoreDocument(document))
            .filter((document): document is DraftDocumentFile => !!document)
        };
        this.draftByServiceId.set(draft.serviceId, draft);
      });
    } catch {
      localStorage.removeItem(this.storageKey);
    }
  }

  private restoreDocument(stored: StoredDraftDocument): DraftDocumentFile | null {
    const file = this.dataUrlToFile(stored.dataUrl, stored.fileName, stored.mimeType);
    if (!file) {
      return null;
    }
    const previewUrl = stored.mimeType.startsWith('image/') ? stored.dataUrl : null;
    return {
      id: stored.id,
      documentType: stored.documentType,
      fileName: stored.fileName,
      mimeType: stored.mimeType,
      file,
      dataUrl: stored.dataUrl,
      previewUrl
    };
  }

  private dataUrlToFile(dataUrl: string, fileName: string, mimeType: string): File | null {
    const marker = ';base64,';
    const splitAt = dataUrl.indexOf(marker);
    if (splitAt < 0) {
      return null;
    }

    try {
      const base64 = dataUrl.slice(splitAt + marker.length);
      const binary = atob(base64);
      const bytes = new Uint8Array(binary.length);
      for (let i = 0; i < binary.length; i += 1) {
        bytes[i] = binary.charCodeAt(i);
      }
      return new File([bytes], fileName, { type: mimeType });
    } catch {
      return null;
    }
  }

  private revokeDocumentPreviews(documents: DraftDocumentFile[]): void {
    documents.forEach((document) => {
      if (document.previewUrl && document.previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(document.previewUrl);
      }
    });
  }
}
