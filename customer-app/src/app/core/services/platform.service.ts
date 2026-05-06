import { Injectable } from '@angular/core';
import { Capacitor } from '@capacitor/core';

@Injectable({ providedIn: 'root' })
export class PlatformService {
  isNativeApp(): boolean {
    return Capacitor.isNativePlatform();
  }

  isBrowser(): boolean {
    return !this.isNativeApp();
  }

  getPlatform(): string {
    return Capacitor.getPlatform();
  }
}
