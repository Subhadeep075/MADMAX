import { Injectable } from '@angular/core';
import { AlertController, LoadingController, ToastController } from '@ionic/angular/standalone';

@Injectable({ providedIn: 'root' })
export class UiFeedbackService {
  constructor(
    private readonly loadingController: LoadingController,
    private readonly toastController: ToastController,
    private readonly alertController: AlertController
  ) {}

  async presentLoading(message = 'Please wait...'): Promise<HTMLIonLoadingElement> {
    const loading = await this.loadingController.create({
      message,
      spinner: 'crescent'
    });
    await loading.present();
    return loading;
  }

  async dismissLoading(loading: HTMLIonLoadingElement | null): Promise<void> {
    if (loading) {
      await loading.dismiss();
    }
  }

  async showToast(message: string, color: 'success' | 'danger' | 'warning' | 'primary' = 'primary'): Promise<void> {
    const toast = await this.toastController.create({
      message,
      color,
      duration: 2200,
      position: 'bottom'
    });
    await toast.present();
  }

  async confirm(title: string, message: string, confirmText = 'Confirm'): Promise<boolean> {
    return new Promise<boolean>(async (resolve) => {
      const alert = await this.alertController.create({
        header: title,
        message,
        buttons: [
          {
            text: 'Cancel',
            role: 'cancel',
            handler: () => resolve(false)
          },
          {
            text: confirmText,
            role: 'confirm',
            handler: () => resolve(true)
          }
        ]
      });

      await alert.present();
    });
  }
}
