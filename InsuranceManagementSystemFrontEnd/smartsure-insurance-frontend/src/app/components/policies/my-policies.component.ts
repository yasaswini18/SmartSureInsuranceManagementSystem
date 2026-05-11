import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PolicyService } from '../../services/policy.service';
import { PurchasedPolicy } from '../../models';
import { PaginationComponent } from '../shared/pagination.component';
import { PaginationHelper } from '../shared/pagination.helper';

@Component({
  selector: 'app-my-policies',
  standalone: true,
  imports: [CommonModule, RouterLink, PaginationComponent],
  templateUrl: './my-policies.component.html',
})
export class MyPoliciesComponent implements OnInit {
  policies: PurchasedPolicy[] = [];
  paginator = new PaginationHelper<PurchasedPolicy>(9);
  loading = true;
  cancellingId: number | null = null;
  error = '';
  success = '';

  constructor(private policyService: PolicyService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.policyService.getMyPolicies().subscribe({
      next: (res) => { this.policies = res.data || []; this.paginator.setData(this.policies); this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  cancel(policy: PurchasedPolicy): void {
    if (!confirm(`Cancel policy ${policy.policyNumber}?`)) return;
    this.cancellingId = policy.id;
    this.policyService.cancelPolicy(policy.id).subscribe({
      next: () => { this.success = 'Policy cancelled successfully'; this.cancellingId = null; this.load(); },
      error: (err) => { this.error = err.error?.message || 'Cancellation failed'; this.cancellingId = null; }
    });
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

  expiringBadge(policy: PurchasedPolicy): { text: string; classes: string } | null {
    if (policy.status !== 'ACTIVE') {
      return null;
    }

    const endDate = new Date(policy.endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const end = new Date(endDate);
    end.setHours(0, 0, 0, 0);
    const daysRemaining = Math.ceil((end.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

    if (daysRemaining <= 7 && daysRemaining >= 0) {
      return {
        text: `🔴 Expiring in ${daysRemaining} days — Renew Soon`,
        classes: 'bg-red-50 text-red-700 border border-red-200'
      };
    }

    if (daysRemaining <= 30 && daysRemaining >= 0) {
      return {
        text: `⚠ Expiring in ${daysRemaining} days`,
        classes: 'bg-amber-50 text-amber-700 border border-amber-200'
      };
    }

    return null;
  }
}
