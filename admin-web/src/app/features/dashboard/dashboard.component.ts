import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { AdminApiService } from '../../core/services/admin-api.service';
import { AdminDashboardResponse } from '../../shared/models/api.models';

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  data: AdminDashboardResponse | null = null;
  loading = false;
  errorMessage = '';

  constructor(private readonly adminApiService: AdminApiService) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminApiService
      .getDashboard()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          this.data = response;
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }
}
