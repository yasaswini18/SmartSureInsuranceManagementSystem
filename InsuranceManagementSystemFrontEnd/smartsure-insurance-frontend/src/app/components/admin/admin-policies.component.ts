import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { PolicyProduct } from '../../models';
import { PaginationComponent } from '../shared/pagination.component';
import { PaginationHelper } from '../shared/pagination.helper';

@Component({
  selector: 'app-admin-policies',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, PaginationComponent],
  templateUrl: './admin-policies.component.html',
})
export class AdminPoliciesComponent implements OnInit {
  products: PolicyProduct[] = [];
  paginator = new PaginationHelper<PolicyProduct>(10);
  loading = true;
  showModal = false;
  editing: PolicyProduct | null = null;
  form: FormGroup;
  saving = false;
  error = '';
  success = '';

  coverageTypes = ['HEALTH', 'LIFE', 'VEHICLE', 'PROPERTY', 'TRAVEL'];

  constructor(private adminService: AdminService, private fb: FormBuilder) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      type: ['', Validators.required],
      basePremium: ['', [Validators.required, Validators.min(1)]],
      coverageAmount: ['', [Validators.required, Validators.min(1000)]],
      durationMonths: ['', [Validators.required, Validators.min(1)]],
      minAge: ['', [Validators.required, Validators.min(0), Validators.max(120)]],
      maxAge: ['', [Validators.required, Validators.min(1), Validators.max(120)]],
    });
  }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.adminService.getAllProducts().subscribe({
      next: (res) => { this.products = res.data || []; this.paginator.setData(this.products); this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form.reset();
    this.error = '';
    this.showModal = true;
  }

  openEdit(p: PolicyProduct): void {
    this.editing = p;
    this.form.patchValue({
      name: p.name,
      description: p.description,
      type: p.type,
      basePremium: p.basePremium,
      coverageAmount: p.coverageAmount,
      durationMonths: p.durationMonths,
      minAge: p.minAge,
      maxAge: p.maxAge,
    });
    this.error = '';
    this.showModal = true;
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.error = '';
    const action = this.editing
      ? this.adminService.updateProduct(this.editing.id, this.form.value)
      : this.adminService.createProduct(this.form.value);
    action.subscribe({
      next: () => {
        this.success = this.editing ? 'Product updated!' : 'Product created!';
        this.saving = false;
        this.showModal = false;
        this.load();
      },
      error: (err) => { this.error = err.error?.message || 'Save failed'; this.saving = false; }
    });
  }

  deactivate(p: PolicyProduct): void {
    if (!confirm(`Deactivate "${p.name}"?`)) return;
    this.adminService.deleteProduct(p.id).subscribe({
      next: () => { this.success = 'Product deactivated'; this.load(); },
      error: (err) => { this.error = err.error?.message || 'Failed'; }
    });
  }

  reactivate(p: PolicyProduct): void {
    this.adminService.reactivateProduct(p.id).subscribe({
      next: () => { this.success = 'Product reactivated'; this.load(); },
      error: (err) => { this.error = err.error?.message || 'Failed'; }
    });
  }

  coverageColor(type: string): string {
    const map: Record<string, string> = {
      HEALTH: 'text-emerald-400 bg-emerald-400/10',
      LIFE: 'text-blue-400 bg-blue-400/10',
      VEHICLE: 'text-orange-400 bg-orange-400/10',
      PROPERTY: 'text-purple-400 bg-purple-400/10',
      TRAVEL: 'text-cyan-400 bg-cyan-400/10',
    };
    return map[type] || 'text-slate-400 bg-slate-400/10';
  }
}
