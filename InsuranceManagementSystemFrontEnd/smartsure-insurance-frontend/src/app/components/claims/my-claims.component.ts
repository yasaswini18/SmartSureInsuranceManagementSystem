import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ClaimService } from '../../services/claim.service';
import { Claim } from '../../models';
import { PaginationComponent } from '../shared/pagination.component';
import { PaginationHelper } from '../shared/pagination.helper';

@Component({
  selector: 'app-my-claims',
  standalone: true,
  imports: [CommonModule, RouterLink, PaginationComponent],
  templateUrl: './my-claims.component.html',
})
export class MyClaimsComponent implements OnInit {
  claims: Claim[] = [];
  paginator = new PaginationHelper<Claim>(8);
  loading = true;
  error = '';
  steps = ['PENDING', 'UNDER_REVIEW', 'APPROVED', 'SETTLED'];

  constructor(private claimService: ClaimService) {}

  ngOnInit(): void {
    this.claimService.getMyClaims().subscribe({
      next: (res) => {
        this.claims = res.data || [];
        this.paginator.setData(this.claims);
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load claims.';
        this.loading = false;
      }
    });
  }

  statusColor(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'text-yellow-400 bg-yellow-400/10',
      UNDER_REVIEW: 'text-orange-400 bg-orange-400/10',
      APPROVED: 'text-emerald-400 bg-emerald-400/10',
      REJECTED: 'text-red-400 bg-red-400/10',
      SETTLED: 'text-slate-400 bg-slate-400/10',
    };
    return map[status] || 'text-slate-400 bg-slate-400/10';
  }

  stepIndex(status: string): number {
    return this.steps.indexOf(status);
  }
}
