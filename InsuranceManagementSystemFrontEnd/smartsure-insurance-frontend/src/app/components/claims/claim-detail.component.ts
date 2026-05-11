import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ClaimService } from '../../services/claim.service';
import { Claim } from '../../models';

@Component({
  selector: 'app-claim-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './claim-detail.component.html',
})
export class ClaimDetailComponent implements OnInit {
  claim: Claim | null = null;
  loading = true;
  error = '';
  timeline: string[] = ['PENDING', 'UNDER_REVIEW', 'APPROVED', 'SETTLED'];

  constructor(private route: ActivatedRoute, private claimService: ClaimService) {}

  ngOnInit(): void {
    const claimId = Number(this.route.snapshot.paramMap.get('id'));
    if (!claimId) {
      this.error = 'Invalid claim id.';
      this.loading = false;
      return;
    }

    this.claimService.getClaimStatus(claimId).subscribe({
      next: (res) => {
        this.claim = res.data || null;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load claim details.';
        this.loading = false;
      }
    });
  }

  isReached(step: string): boolean {
    if (!this.claim) {
      return false;
    }

    return this.timeline.indexOf(this.claim.status) >= this.timeline.indexOf(step);
  }

  statusColor(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'bg-amber-50 text-amber-700',
      UNDER_REVIEW: 'bg-orange-50 text-orange-700',
      APPROVED: 'bg-emerald-50 text-emerald-700',
      REJECTED: 'bg-red-50 text-red-700',
      SETTLED: 'bg-slate-100 text-slate-700',
    };
    return map[status] || 'bg-slate-100 text-slate-700';
  }
}
