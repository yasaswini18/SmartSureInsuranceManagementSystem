import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../services/admin.service';
import { DashboardReport } from '../../models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
})
export class AdminDashboardComponent implements OnInit {
  report: DashboardReport | null = null;
  loading = true;
  error = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getDashboard().subscribe({
      next: (res) => { this.report = res.data || null; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Failed to load dashboard data.'; }
    });
  }

  get claimApprovalRate(): number {
    if (!this.report) return 0;
    const total = this.safeNumber(this.report.approvedClaims) + this.safeNumber(this.report.rejectedClaims);
    return total === 0 ? 0 : Math.round((this.safeNumber(this.report.approvedClaims) / total) * 100);
  }

  get policyActivationRate(): number {
    if (!this.report) return 0;
    return this.safeNumber(this.report.totalPurchasedPolicies) === 0
      ? 0
      : Math.round((this.safeNumber(this.report.activePolicies) / this.safeNumber(this.report.totalPurchasedPolicies)) * 100);
  }

  safeNumber(value: number | undefined | null): number {
    return Number(value ?? 0);
  }
}
