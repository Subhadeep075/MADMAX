import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';
import { catchError, finalize, forkJoin, of } from 'rxjs';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { PlatformService } from '../../core/services/platform.service';
import { CategoryResponse, CenterInfoResponse, ServiceResponse } from '../../shared/models/api.models';

@Component({
  standalone: true,
  selector: 'app-home-page',
  imports: [CommonModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './home.page.html'
})
export class HomePage implements OnInit {
  readonly enablePullToRefresh: boolean;

  categories: CategoryResponse[] = [];
  services: ServiceResponse[] = [];
  centerInfo: CenterInfoResponse | null = null;
  searchTerm = '';
  loading = false;
  errorMessage = '';

  constructor(
    private readonly api: CustomerApiService,
    private readonly router: Router,
    private readonly platformService: PlatformService
  ) {
    this.enablePullToRefresh = this.platformService.isNativeApp();
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(event?: CustomEvent): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      categories: this.api.getCategories(),
      services: this.api.getServices(),
      centerInfo: this.api.getCenterInfo().pipe(catchError(() => of(null)))
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          event?.detail?.complete();
        })
      )
      .subscribe({
        next: (result) => {
          this.categories = result.categories.filter((item) => item.active);
          this.services = result.services;
          this.centerInfo = result.centerInfo;
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  get openServices(): ServiceResponse[] {
    const term = this.searchTerm.toLowerCase().trim();
    return this.services
      .filter((item) => item.status === 'OPEN')
      .filter((item) => !term || item.title.toLowerCase().includes(term));
  }

  get popularServices(): ServiceResponse[] {
    return this.openServices.slice(0, 6);
  }

  openService(serviceId: number): void {
    this.router.navigate(['/service', serviceId]);
  }

  goToServices(search = '', categoryId?: number): void {
    this.router.navigate(['/app/services'], {
      queryParams: {
        q: search || undefined,
        categoryId: categoryId || undefined
      }
    });
  }

  categoryEmoji(categoryName: string): string {
    const name = categoryName.toLowerCase();
    if (name.includes('document')) {
      return '📄';
    }
    if (name.includes('certificate')) {
      return '🏅';
    }
    if (name.includes('registration')) {
      return '📝';
    }
    return '📂';
  }

  get callHref(): string {
    const digits = this.normalizePhone(this.centerInfo?.mobile);
    return digits ? `tel:+91${digits}` : '';
  }

  get whatsappHref(): string {
    const digits = this.normalizePhone(this.centerInfo?.whatsappNumber);
    if (!digits) {
      return '';
    }
    return `https://wa.me/91${digits}?text=${encodeURIComponent('I need help with Digital Cyber Seva')}`;
  }

  get hasCallContact(): boolean {
    return !!this.normalizePhone(this.centerInfo?.mobile);
  }

  get hasWhatsappContact(): boolean {
    return !!this.normalizePhone(this.centerInfo?.whatsappNumber);
  }

  private normalizePhone(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    const digits = value.replace(/\D/g, '');
    return digits.length >= 10 ? digits : '';
  }
}
