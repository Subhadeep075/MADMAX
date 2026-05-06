import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';
import { finalize, forkJoin } from 'rxjs';
import { CustomerApiService } from '../../core/services/customer-api.service';
import { CategoryResponse, ServiceResponse, ServiceStatus } from '../../shared/models/api.models';

@Component({
  standalone: true,
  selector: 'app-service-list-page',
  imports: [CommonModule, ...IONIC_STANDALONE_IMPORTS],
  templateUrl: './service-list.page.html'
})
export class ServiceListPage implements OnInit {
  categories: CategoryResponse[] = [];
  services: ServiceResponse[] = [];
  searchTerm = '';
  selectedCategoryId: number | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly api: CustomerApiService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const search = params.get('q') ?? '';
      const categoryIdParam = params.get('categoryId');
      this.searchTerm = search;
      this.selectedCategoryId = categoryIdParam ? Number(categoryIdParam) : null;
    });

    this.loadData();
  }

  loadData(event?: CustomEvent): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      categories: this.api.getCategories(),
      services: this.api.getServices()
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
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  get filteredServices(): ServiceResponse[] {
    const term = this.searchTerm.toLowerCase().trim();
    return this.services.filter((service) => {
      const matchesSearch = !term || service.title.toLowerCase().includes(term);
      const matchesCategory = this.selectedCategoryId === null || service.categoryId === this.selectedCategoryId;
      return matchesSearch && matchesCategory;
    });
  }

  onSearchChange(value: string | null): void {
    this.searchTerm = value ?? '';
  }

  onCategoryChange(value: unknown): void {
    if (value === null || value === undefined || value === '') {
      this.selectedCategoryId = null;
      return;
    }

    const parsed = Number(value);
    this.selectedCategoryId = Number.isNaN(parsed) ? null : parsed;
  }

  openService(serviceId: number): void {
    this.router.navigate(['/service', serviceId]);
  }

  statusColor(status: ServiceStatus): 'success' | 'medium' | 'warning' {
    if (status === 'OPEN') {
      return 'success';
    }
    if (status === 'COMING_SOON') {
      return 'warning';
    }
    return 'medium';
  }

  canApply(service: ServiceResponse): boolean {
    return service.status === 'OPEN';
  }
}
