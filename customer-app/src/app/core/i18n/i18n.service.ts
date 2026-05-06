import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { TRANSLATIONS } from './translations';
import { AppLanguage } from './i18n.types';

@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly storageKey = 'dcs_customer_language';

  private readonly languageSubject = new BehaviorSubject<AppLanguage>(this.resolveInitialLanguage());
  readonly language$ = this.languageSubject.asObservable();

  get currentLanguage(): AppLanguage {
    return this.languageSubject.value;
  }

  isLanguageSelected(): boolean {
    return !!localStorage.getItem(this.storageKey);
  }

  setLanguage(language: AppLanguage): void {
    localStorage.setItem(this.storageKey, language);
    this.languageSubject.next(language);
  }

  t(key: string, params?: Record<string, string | number | null | undefined>): string {
    const dict = TRANSLATIONS[this.currentLanguage];
    const fallback = TRANSLATIONS.en;
    const template = dict[key] ?? fallback[key] ?? key;

    if (!params) {
      return template;
    }

    return Object.entries(params).reduce(
      (result, [token, value]) => result.replaceAll(`{{${token}}}`, value == null ? '' : String(value)),
      template
    );
  }

  private resolveInitialLanguage(): AppLanguage {
    const stored = localStorage.getItem(this.storageKey);
    if (stored === 'bn' || stored === 'en') {
      return stored;
    }
    return 'en';
  }
}
