import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { PolicyService } from '../../services/policy.service';
import { ClaimService } from '../../services/claim.service';
import { AuthService } from '../../services/auth.service';
import { PurchasedPolicy, Claim } from '../../models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  policies: PurchasedPolicy[] = [];
  claims: Claim[] = [];
  loading = true;
  userName = '';

  get activePolicies() { return this.policies.filter(p => p.status === 'ACTIVE').length; }
  get pendingClaims() { return this.claims.filter(c => c.status === 'PENDING' || c.status === 'UNDER_REVIEW').length; }
  get approvedClaims() { return this.claims.filter(c => c.status === 'APPROVED').length; }
  get totalPremium() { return this.policies.filter(p => p.status === 'ACTIVE').reduce((s, p) => s + (p.premiumPaid || 0), 0); }
  get recentClaims() { return this.claims.slice(0, 5); }
  get recentPolicies() { return this.policies.slice(0, 3); }

  constructor(
    private policyService: PolicyService,
    private claimService: ClaimService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.userName = this.auth.currentUser?.name || 'User';
    forkJoin({
      policiesRes: this.policyService.getMyPolicies(),
      claimsRes: this.claimService.getMyClaims()
    }).subscribe({
      next: ({ policiesRes, claimsRes }) => {
        this.policies = policiesRes.data || [];
        this.claims = claimsRes.data || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  statusColor(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'text-emerald-400 bg-emerald-400/10',
      CREATED: 'text-blue-400 bg-blue-400/10',
      EXPIRED: 'text-slate-400 bg-slate-400/10',
      CANCELLED: 'text-red-400 bg-red-400/10',
      PENDING: 'text-yellow-400 bg-yellow-400/10',
      UNDER_REVIEW: 'text-orange-400 bg-orange-400/10',
      APPROVED: 'text-emerald-400 bg-emerald-400/10',
      REJECTED: 'text-red-400 bg-red-400/10',
      SETTLED: 'text-slate-400 bg-slate-400/10',
    };
    return map[status] || 'text-slate-400 bg-slate-400/10';
  }
}
