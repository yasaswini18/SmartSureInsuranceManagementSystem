import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { PurchasedPolicy } from '../../models';
import { PaginationComponent } from '../shared/pagination.component';
import { PaginationHelper } from '../shared/pagination.helper';

@Component({
  selector: 'app-admin-purchased-policies',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationComponent],
  template: `
<div class="p-6 max-w-7xl mx-auto animate-enter">
  <div class="mb-8">
    <h1 class="section-title">All Purchased Policies</h1>
    <p class="section-subtitle">View all customer-purchased policies across the system</p>
  </div>

  <div class="flex flex-wrap gap-3 mb-5 items-center">
    <input [(ngModel)]="search" (input)="applyFilter()" type="text" class="input-field max-w-xs"
      placeholder="Search policy number or customer..." />
    <select [(ngModel)]="filterStatus" (change)="applyFilter()" class="input-field w-40">
      <option value="ALL">All Statuses</option>
      <option *ngFor="let s of statuses" [value]="s">{{ s }}</option>
    </select>
    <span class="text-xs text-slate-500">{{ paginator.total }} results</span>
  </div>

  <div *ngIf="loading" class="flex justify-center py-20">
    <svg class="w-8 h-8 animate-spin text-primary-400" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
    </svg>
  </div>

  <div *ngIf="!loading" class="card overflow-hidden">
    <div *ngIf="paginator.total === 0" class="p-10 text-center text-slate-500 text-sm">No policies found.</div>
    <table *ngIf="paginator.total > 0" class="w-full">
      <thead class="bg-slate-800/60 border-b border-slate-700/50">
        <tr>
          <th class="table-header text-left">Policy #</th>
          <th class="table-header text-left hidden md:table-cell">Customer</th>
          <th class="table-header text-left hidden lg:table-cell">Product</th>
          <th class="table-header text-right hidden md:table-cell">Premium</th>
          <th class="table-header text-right hidden lg:table-cell">Coverage</th>
          <th class="table-header text-center">Status</th>
          <th class="table-header text-right hidden md:table-cell">Expiry</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-slate-800/60">
        <tr *ngFor="let p of paginator.paged" class="hover:bg-slate-800/30 transition-colors">
          <td class="table-cell font-mono text-xs text-primary-400">{{ p.policyNumber }}</td>
          <td class="table-cell hidden md:table-cell">
            <p class="text-sm text-white">{{ p.customerName || '-' }}</p>
            <p class="text-xs text-slate-500">{{ p.customerEmail || '' }}</p>
          </td>
          <td class="table-cell hidden lg:table-cell text-slate-300 text-sm">{{ p.productName }}</td>
          <td class="table-cell text-right hidden md:table-cell text-white font-medium">Rs {{ p.premiumPaid | number:'1.0-0' }}</td>
          <td class="table-cell text-right hidden lg:table-cell text-slate-400">Rs {{ p.coverageAmount | number:'1.0-0' }}</td>
          <td class="table-cell text-center">
            <span class="status-badge text-xs" [class]="statusColor(p.status)">{{ p.status }}</span>
          </td>
          <td class="table-cell text-right hidden md:table-cell text-slate-400 text-xs">{{ p.endDate | date:'mediumDate' }}</td>
        </tr>
      </tbody>
    </table>
    <app-pagination
      [totalItems]="paginator.total"
      [pageSize]="paginator.pageSize"
      [currentPage]="paginator.currentPage"
      (pageChange)="paginator.goTo($event)">
    </app-pagination>
  </div>
</div>
  `,
})
export class AdminPurchasedPoliciesComponent implements OnInit {
  policies: PurchasedPolicy[] = [];
  paginator = new PaginationHelper<PurchasedPolicy>(10);
  loading = true;
  search = '';
  filterStatus = 'ALL';
  statuses = ['ACTIVE', 'CREATED', 'EXPIRED', 'CANCELLED'];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getAllPurchasedPolicies().subscribe({
      next: (res) => {
        this.policies = res.data || [];
        this.paginator.setData(this.policies);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  applyFilter(): void {
    const q = this.search.toLowerCase();
    const filtered = this.policies.filter(p => {
      const matchSearch = !q || p.policyNumber?.toLowerCase().includes(q)
        || p.customerName?.toLowerCase().includes(q)
        || p.customerEmail?.toLowerCase().includes(q);
      const matchStatus = this.filterStatus === 'ALL' || p.status === this.filterStatus;
      return matchSearch && matchStatus;
    });
    this.paginator.setData(filtered);
  }

  statusColor(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'text-emerald-400 bg-emerald-400/10',
      CREATED: 'text-blue-400 bg-blue-400/10',
      EXPIRED: 'text-slate-400 bg-slate-400/10',
      CANCELLED: 'text-red-400 bg-red-400/10',
    };
    return map[status] || 'text-slate-400 bg-slate-400/10';
  }
}
