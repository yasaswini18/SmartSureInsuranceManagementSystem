import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { AuditLog, Claim, PurchasedPolicy } from '../../models';
import { PaginationComponent } from '../shared/pagination.component';
import { PaginationHelper } from '../shared/pagination.helper';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, PaginationComponent],
  template: `
<div class="p-6 max-w-7xl mx-auto animate-enter">
  <div class="mb-8">
    <h1 class="section-title">Reports</h1>
    <p class="section-subtitle">Policy, claims, and audit reporting for the admin workspace</p>
  </div>

  <div *ngIf="loading" class="flex justify-center py-20">
    <svg class="w-8 h-8 animate-spin text-primary-400" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
    </svg>
  </div>

  <ng-container *ngIf="!loading">
    <div class="mb-6 flex flex-wrap gap-2">
      <button *ngFor="let tab of tabs" (click)="selectTab(tab)"
        class="rounded-xl px-4 py-2 text-sm font-semibold transition-colors"
        [class]="activeTab === tab ? 'bg-primary-700 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'">
        {{ tab }}
      </button>
    </div>

    <div *ngIf="activeTab === 'Policy Report'" class="space-y-6">
      <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <div class="card p-5 text-center">
          <p class="text-3xl font-display font-bold text-slate-950">{{ policies.length }}</p>
          <p class="mt-1 text-xs uppercase tracking-wider text-slate-400">Total Policies</p>
        </div>
        <div class="card p-5 text-center">
          <p class="text-3xl font-display font-bold text-emerald-600">{{ countPolicyStatus('ACTIVE') }}</p>
          <p class="mt-1 text-xs uppercase tracking-wider text-slate-400">Active</p>
        </div>
        <div class="card p-5 text-center">
          <p class="text-3xl font-display font-bold text-red-600">{{ countPolicyStatus('CANCELLED') }}</p>
          <p class="mt-1 text-xs uppercase tracking-wider text-slate-400">Cancelled</p>
        </div>
        <div class="card p-5 text-center">
          <p class="text-3xl font-display font-bold text-primary-700">Rs {{ totalPremium | number:'1.0-0' }}</p>
          <p class="mt-1 text-xs uppercase tracking-wider text-slate-400">Premium Collected</p>
        </div>
      </div>

      <div class="card overflow-hidden">
        <div *ngIf="policies.length === 0" class="p-10 text-center text-slate-500 text-sm">No policy report data available.</div>
        <table *ngIf="policies.length > 0" class="w-full">
          <thead class="bg-slate-100 border-b border-slate-200">
            <tr>
              <th class="table-header text-left">Policy #</th>
              <th class="table-header text-left">Product</th>
              <th class="table-header text-right">Coverage</th>
              <th class="table-header text-right">Premium</th>
              <th class="table-header text-center">Status</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr *ngFor="let policy of policies">
              <td class="table-cell text-slate-700 font-mono text-xs">{{ policy.policyNumber }}</td>
              <td class="table-cell text-slate-700">{{ policy.productName }}</td>
              <td class="table-cell text-right text-slate-700">Rs {{ policy.coverageAmount | number:'1.0-0' }}</td>
              <td class="table-cell text-right text-slate-700">Rs {{ policy.premiumPaid | number:'1.0-0' }}</td>
              <td class="table-cell text-center"><span class="status-badge text-xs">{{ policy.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div *ngIf="activeTab === 'Claims Report'" class="space-y-6">
      <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <div class="card p-5 text-center">
          <p class="text-3xl font-display font-bold text-slate-950">{{ claims.length }}</p>
          <p class="mt-1 text-xs uppercase tracking-wider text-slate-400">Total Claims</p>
        </div>
        <div class="card p-5 text-center">
          <p class="text-3xl font-display font-bold text-amber-600">{{ countClaimStatus('PENDING') + countClaimStatus('UNDER_REVIEW') }}</p>
          <p class="mt-1 text-xs uppercase tracking-wider text-slate-400">Open Claims</p>
        </div>
        <div class="card p-5 text-center">
          <p class="text-3xl font-display font-bold text-emerald-600">{{ countClaimStatus('APPROVED') }}</p>
          <p class="mt-1 text-xs uppercase tracking-wider text-slate-400">Approved</p>
        </div>
        <div class="card p-5 text-center">
          <p class="text-3xl font-display font-bold text-primary-700">Rs {{ totalClaimed | number:'1.0-0' }}</p>
          <p class="mt-1 text-xs uppercase tracking-wider text-slate-400">Claimed Amount</p>
        </div>
      </div>

      <div class="card overflow-hidden">
        <div *ngIf="claims.length === 0" class="p-10 text-center text-slate-500 text-sm">No claims report data available.</div>
        <table *ngIf="claims.length > 0" class="w-full">
          <thead class="bg-slate-100 border-b border-slate-200">
            <tr>
              <th class="table-header text-left">Claim #</th>
              <th class="table-header text-left">Policy #</th>
              <th class="table-header text-left">Type</th>
              <th class="table-header text-right">Claimed</th>
              <th class="table-header text-center">Status</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr *ngFor="let claim of claims">
              <td class="table-cell text-slate-700 font-mono text-xs">{{ claim.claimNumber }}</td>
              <td class="table-cell text-slate-700">{{ claim.policyNumber }}</td>
              <td class="table-cell text-slate-700">{{ claim.claimType }}</td>
              <td class="table-cell text-right text-slate-700">Rs {{ claim.claimedAmount | number:'1.0-0' }}</td>
              <td class="table-cell text-center"><span class="status-badge text-xs">{{ claim.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div *ngIf="activeTab === 'Audit Log'" class="card overflow-hidden">
      <div class="px-5 py-4 border-b border-slate-200 flex items-center justify-between">
        <h3 class="font-semibold text-slate-900 text-sm">Audit Log</h3>
        <span class="text-xs text-slate-500">{{ auditLogs.length }} entries</span>
      </div>
      <div *ngIf="auditLogs.length === 0" class="p-10 text-center text-slate-500 text-sm">No audit logs available.</div>
      <table *ngIf="auditLogs.length > 0" class="w-full">
        <thead class="bg-slate-100 border-b border-slate-200">
          <tr>
            <th class="table-header text-left">Admin</th>
            <th class="table-header text-left">Action</th>
            <th class="table-header text-left hidden md:table-cell">Resource</th>
            <th class="table-header text-left hidden lg:table-cell">Details</th>
            <th class="table-header text-right">Timestamp</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr *ngFor="let log of paginator.paged" class="hover:bg-slate-50 transition-colors">
            <td class="table-cell text-slate-700 text-xs">{{ log.adminEmail }}</td>
            <td class="table-cell">
              <span class="status-badge text-xs" [class]="actionColor(log.action)">{{ log.action }}</span>
            </td>
            <td class="table-cell hidden md:table-cell text-slate-500 text-xs">
              {{ log.resourceType }} #{{ log.resourceId }}
            </td>
            <td class="table-cell hidden lg:table-cell text-slate-500 text-xs max-w-[200px] truncate">{{ log.details }}</td>
            <td class="table-cell text-right text-xs text-slate-500">{{ log.timestamp | date:'short' }}</td>
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
  </ng-container>
</div>
  `,
})
export class AdminReportsComponent implements OnInit {
  tabs = ['Policy Report', 'Claims Report', 'Audit Log'];
  activeTab = 'Policy Report';
  claims: Claim[] = [];
  policies: PurchasedPolicy[] = [];
  auditLogs: AuditLog[] = [];
  paginator = new PaginationHelper<AuditLog>(15);
  loading = true;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getPoliciesReport().subscribe({
      next: (res) => {
        this.policies = res.data || [];
        this.loading = false;
        this.loadClaims();
        this.loadAuditLogs();
      },
      error: () => {
        this.loading = false;
        this.loadClaims();
        this.loadAuditLogs();
      }
    });
  }

  selectTab(tab: string): void {
    this.activeTab = tab;
  }

  loadClaims(): void {
    this.adminService.getClaimsReport().subscribe({
      next: (res) => {
        this.claims = res.data || [];
      }
    });
  }

  loadAuditLogs(): void {
    this.adminService.getAuditLogs().subscribe({
      next: (res) => {
        this.auditLogs = res.data || [];
        this.paginator.setData(this.auditLogs);
      }
    });
  }

  countPolicyStatus(status: string): number {
    return this.policies.filter(policy => policy.status === status).length;
  }

  countClaimStatus(status: string): number {
    return this.claims.filter(claim => claim.status === status).length;
  }

  get totalPremium(): number {
    return this.policies.reduce((sum, policy) => sum + Number(policy.premiumPaid || 0), 0);
  }

  get totalClaimed(): number {
    return this.claims.reduce((sum, claim) => sum + Number(claim.claimedAmount || 0), 0);
  }

  actionColor(action: string): string {
    if (action?.toUpperCase().includes('APPROVE')) return 'text-emerald-400 bg-emerald-400/10';
    if (action?.toUpperCase().includes('REJECT')) return 'text-red-400 bg-red-400/10';
    if (action?.toUpperCase().includes('CREATE')) return 'text-blue-400 bg-blue-400/10';
    if (action?.toUpperCase().includes('DELETE')) return 'text-red-400 bg-red-400/10';
    if (action?.toUpperCase().includes('UPDATE')) return 'text-yellow-400 bg-yellow-400/10';
    return 'text-slate-400 bg-slate-400/10';
  }
}
