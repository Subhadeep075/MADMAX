import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { AppLanguage } from '../../core/i18n/i18n.types';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';

@Component({
  standalone: true,
  selector: 'app-language-select',
  imports: [CommonModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './language-select.page.html'
})
export class LanguageSelectPage {
  selectedLanguage: AppLanguage;

  constructor(
    private readonly i18n: I18nService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.selectedLanguage = this.i18n.currentLanguage;
  }

  chooseLanguage(language: AppLanguage): void {
    this.selectedLanguage = language;
    this.i18n.setLanguage(language);
  }

  continueToAuth(): void {
    if (this.authService.isLoggedIn() && this.authService.hasRole('CUSTOMER')) {
      this.router.navigateByUrl('/app/home', { replaceUrl: true });
      return;
    }
    this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}
