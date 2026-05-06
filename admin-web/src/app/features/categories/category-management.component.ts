import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { AdminApiService } from '../../core/services/admin-api.service';
import { CategoryResponse, CategoryUpsertRequest } from '../../shared/models/api.models';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './category-management.component.html'
})
export class CategoryManagementComponent implements OnInit {
  categories: CategoryResponse[] = [];
  editingCategoryId: number | null = null;
  loading = false;
  saving = false;
  deleting = false;
  errorMessage = '';
  successMessage = '';

  readonly categoryForm = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    iconName: [''],
    displayOrder: [1, [Validators.required]],
    active: [true]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly adminApiService: AdminApiService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminApiService
      .getAdminCategories()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (response) => {
          this.categories = [...response].sort((a, b) => a.displayOrder - b.displayOrder);
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  saveCategory(): void {
    this.successMessage = '';
    this.errorMessage = '';
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    const payload = this.toPayload();
    this.saving = true;

    const operation = this.editingCategoryId
      ? this.adminApiService.updateCategory(this.editingCategoryId, payload)
      : this.adminApiService.createCategory(payload);

    operation.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        this.successMessage = this.editingCategoryId
          ? 'Category updated successfully.'
          : 'Category created successfully.';
        this.resetForm();
        this.loadCategories();
      },
      error: (error: Error) => {
        this.errorMessage = error.message;
      }
    });
  }

  editCategory(item: CategoryResponse): void {
    this.editingCategoryId = item.id;
    this.categoryForm.patchValue({
      name: item.name,
      iconName: item.iconName ?? '',
      displayOrder: item.displayOrder,
      active: item.active
    });
  }

  deleteEditingCategory(): void {
    if (!this.editingCategoryId || this.deleting) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    const confirmed = window.confirm(
      'Delete this category permanently? This cannot be undone.'
    );
    if (!confirmed) {
      return;
    }

    const categoryId = this.editingCategoryId;
    this.deleting = true;
    this.adminApiService
      .deleteCategory(categoryId)
      .pipe(finalize(() => (this.deleting = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Category deleted successfully.';
          this.resetForm();
          this.loadCategories();
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        }
      });
  }

  toggleActive(item: CategoryResponse): void {
    this.errorMessage = '';
    this.successMessage = '';
    const payload: CategoryUpsertRequest = {
      name: item.name,
      iconName: item.iconName,
      displayOrder: item.displayOrder,
      active: !item.active
    };
    this.adminApiService.updateCategory(item.id, payload).subscribe({
      next: () => {
        this.successMessage = `Category ${payload.active ? 'activated' : 'deactivated'} successfully.`;
        this.loadCategories();
      },
      error: (error: Error) => {
        this.errorMessage = error.message;
      }
    });
  }

  resetForm(): void {
    this.editingCategoryId = null;
    this.categoryForm.reset({
      name: '',
      iconName: '',
      displayOrder: 1,
      active: true
    });
  }

  private toPayload(): CategoryUpsertRequest {
    const value = this.categoryForm.getRawValue();
    return {
      name: value.name.trim(),
      iconName: value.iconName.trim() ? value.iconName.trim() : null,
      displayOrder: Number(value.displayOrder),
      active: value.active
    };
  }
}
