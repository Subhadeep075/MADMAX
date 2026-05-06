import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AlertController } from '@ionic/angular';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { AppLanguage } from '../../core/i18n/i18n.types';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { UiFeedbackService } from '../../core/services/ui-feedback.service';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';
import {
  AccountDeletionStatusResponse,
  CustomerNotificationResponse,
  CustomerProfileResponse,
  DeletionStatus
} from '../../shared/models/api.models';

@Component({
  standalone: true,
  selector: 'app-profile-page',
  imports: [CommonModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './profile.page.html'
})
export class ProfilePage implements OnInit {
  profile: CustomerProfileResponse | null = null;
  loadingProfile = false;

  editingName = false;
  nameDraft = '';
  savingName = false;

  notifications: CustomerNotificationResponse[] = [];
  loadingNotifications = false;

  deletionInfo: AccountDeletionStatusResponse | null = null;
  loadingDeletionStatus = false;
  submittingDeletionRequest = false;
  private hasHandledApprovedState = false;

  constructor(
    private readonly customerApiService: CustomerApiService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly alertController: AlertController,
    private readonly ui: UiFeedbackService,
    readonly i18n: I18nService
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.loadNotifications();
    this.loadDeletionStatus();
  }

  get name(): string {
    return this.profile?.name ?? this.authService.getName() ?? 'Customer';
  }

  get identifier(): string {
    return this.authService.getUserIdentifier() ?? '-';
  }

  get mobile(): string {
    return this.profile?.mobile ?? '-';
  }

  get email(): string | null {
    return this.profile?.email || null;
  }

  get address(): string {
    return this.profile?.address || this.i18n.t('profile.not_provided');
  }

  get deletionStatus(): DeletionStatus {
    if (this.deletionInfo?.deletionStatus) {
      return this.deletionInfo.deletionStatus;
    }

    const cached = this.authService.getAccountDeletionStatus();
    if (cached === 'PENDING' || cached === 'APPROVED' || cached === 'REJECTED' || cached === 'NONE') {
      return cached;
    }

    return 'NONE';
  }

  get canRequestDeletion(): boolean {
    return !this.submittingDeletionRequest && this.deletionStatus !== 'PENDING' && this.deletionStatus !== 'APPROVED';
  }

  get statusDescription(): string {
    switch (this.deletionStatus) {
      case 'PENDING':
        return this.i18n.t('profile.status.pending');
      case 'APPROVED':
        return this.i18n.t('profile.status.approved');
      case 'REJECTED':
        return this.i18n.t('profile.status.rejected');
      default:
        return this.i18n.t('profile.status.none');
    }
  }

  get selectedLanguage(): AppLanguage {
    return this.i18n.currentLanguage;
  }

  loadProfile(): void {
    this.loadingProfile = true;
    this.customerApiService
      .getMyProfile()
      .pipe(finalize(() => (this.loadingProfile = false)))
      .subscribe({
        next: (response) => {
          this.profile = response;
          this.nameDraft = response.name;
          this.authService.setName(response.name);
        },
        error: (error: Error) => {
          void this.ui.showToast(error.message || 'Unable to load profile details.', 'danger');
        }
      });
  }

  loadNotifications(): void {
    this.loadingNotifications = true;
    this.customerApiService
      .getNotifications(15)
      .pipe(finalize(() => (this.loadingNotifications = false)))
      .subscribe({
        next: (response) => {
          this.notifications = response;
        },
        error: () => {
          this.notifications = [];
        }
      });
  }

  startNameEdit(): void {
    this.nameDraft = this.name;
    this.editingName = true;
  }

  cancelNameEdit(): void {
    this.editingName = false;
    this.nameDraft = this.name;
  }

  saveName(): void {
    const nextName = this.nameDraft.trim();
    if (nextName.length < 2 || nextName.length > 80) {
      void this.ui.showToast(this.i18n.t('profile.name_invalid'), 'warning');
      return;
    }

    this.savingName = true;
    this.customerApiService
      .updateMyName({ name: nextName })
      .pipe(finalize(() => (this.savingName = false)))
      .subscribe({
        next: (updatedProfile) => {
          this.profile = updatedProfile;
          this.nameDraft = updatedProfile.name;
          this.editingName = false;
          this.authService.setName(updatedProfile.name);
          void this.ui.showToast(this.i18n.t('profile.name_updated'), 'success');
        },
        error: (error: Error) => {
          void this.ui.showToast(error.message || 'Unable to update name right now.', 'danger');
        }
      });
  }

  markNotificationRead(notificationId: number): void {
    this.customerApiService.markNotificationRead(notificationId).subscribe({
      next: () => {
        this.notifications = this.notifications.map((notification) =>
          notification.id === notificationId ? { ...notification, read: true } : notification
        );
      }
    });
  }

  changeLanguage(language: AppLanguage): void {
    this.i18n.setLanguage(language);
    void this.ui.showToast(this.i18n.t('language.changed'), 'success');
  }

  openMyRequests(): void {
    this.router.navigate(['/app/requests']);
  }

  loadDeletionStatus(): void {
    this.loadingDeletionStatus = true;
    this.customerApiService
      .getAccountDeletionStatus()
      .pipe(finalize(() => (this.loadingDeletionStatus = false)))
      .subscribe({
        next: (response) => {
          this.deletionInfo = response;
          this.authService.setAccountDeletionStatus(response.deletionStatus);
          if (response.deletionStatus === 'APPROVED') {
            void this.forceLogoutForApprovedStatus();
          }
        },
        error: (error: Error) => {
          void this.ui.showToast(error.message || 'Unable to load account deletion status.', 'danger');
        }
      });
  }

  async requestAccountDeletion(): Promise<void> {
    if (!this.canRequestDeletion) {
      return;
    }

    const alert = await this.alertController.create({
      header: this.i18n.t('profile.deletion_warning_title'),
      message: this.i18n.t('profile.deletion_warning_message'),
      buttons: [
        { text: this.i18n.t('common.cancel'), role: 'cancel' },
        {
          text: this.i18n.t('common.submit'),
          role: 'destructive',
          handler: () => this.submitDeletionRequest()
        }
      ]
    });
    await alert.present();
  }

  logout(): void {
    this.authService.clearSession();
    this.router.navigateByUrl('/login', { replaceUrl: true });
  }

  private submitDeletionRequest(): void {
    this.submittingDeletionRequest = true;
    this.customerApiService
      .requestAccountDeletion()
      .pipe(finalize(() => (this.submittingDeletionRequest = false)))
      .subscribe({
        next: (response) => {
          void this.ui.showToast(response.message || 'Deletion request submitted.', 'success');
          this.loadDeletionStatus();
        },
        error: (error: Error) => {
          void this.ui.showToast(error.message || 'Unable to submit deletion request.', 'danger');
        }
      });
  }

  private async forceLogoutForApprovedStatus(): Promise<void> {
    if (this.hasHandledApprovedState) {
      return;
    }
    this.hasHandledApprovedState = true;

    const alert = await this.alertController.create({
      header: this.i18n.t('profile.deletion_approved_title'),
      message: this.i18n.t('profile.deletion_approved_message'),
      backdropDismiss: false,
      buttons: [
        {
          text: this.i18n.t('common.ok'),
          handler: () => this.logout()
        }
      ]
    });
    await alert.present();
  }
}
