import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ClaimService } from '../../services/claim.service';
import { PolicyService } from '../../services/policy.service';
import { ClaimType, PurchasedPolicy } from '../../models';

@Component({
  selector: 'app-new-claim',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './new-claim.component.html',
})
export class NewClaimComponent implements OnInit {
  form: FormGroup;
  policies: PurchasedPolicy[] = [];
  loading = false;
  loadingPolicies = true;
  error = '';
  success = '';
  selectedPolicy: PurchasedPolicy | null = null;
  selectedFiles: File[] = [];
  waitingPeriodEnd: Date | null = null;
  today = new Date().toISOString().split('T')[0];
  claimTypes: ClaimType[] = ['FULL_LOSS', 'PARTIAL_LOSS', 'MEDICAL', 'THEFT', 'ACCIDENTAL_DAMAGE'];

  constructor(
    private fb: FormBuilder,
    private claimService: ClaimService,
    private policyService: PolicyService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.form = this.fb.group({
      policyNumber: ['', Validators.required],
      claimType: ['', Validators.required],
      claimedAmount: ['', [Validators.required, Validators.min(1)]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      incidentDate: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.policyService.getMyPolicies().subscribe({
      next: (res) => {
        this.policies = (res.data || []).filter(p => p.status === 'ACTIVE');
        this.loadingPolicies = false;
      },
      error: (err) => {
        this.loadingPolicies = false;
        this.error = err.error?.message || 'Failed to load active policies.';
      }
    });
  }

  onPolicyChange(): void {
    const policyNumber = this.form.get('policyNumber')?.value;
    this.selectedPolicy = this.policies.find(policy => policy.policyNumber === policyNumber) || null;
    this.waitingPeriodEnd = null;

    if (this.selectedPolicy?.startDate) {
      const startDate = new Date(this.selectedPolicy.startDate);
      const waitingEnd = new Date(startDate);
      waitingEnd.setDate(waitingEnd.getDate() + 7);
      this.waitingPeriodEnd = waitingEnd;
    }

    const claimedAmountControl = this.form.get('claimedAmount');
    if (!claimedAmountControl) {
      return;
    }

    claimedAmountControl.setValidators([
      Validators.required,
      Validators.min(1),
      Validators.max(this.selectedPolicy?.coverageAmount || Number.MAX_SAFE_INTEGER),
    ]);
    claimedAmountControl.updateValueAndValidity();
  }

  get isWaitingPeriodBlocked(): boolean {
    if (!this.waitingPeriodEnd) {
      return false;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return today < this.waitingPeriodEnd;
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFiles = Array.from(input.files || []);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.isWaitingPeriodBlocked) {
      this.error = 'Claims can only be raised after the waiting period ends.';
      return;
    }

    this.loading = true;
    this.error = '';

    const { policyNumber, claimType, claimedAmount, description, incidentDate } = this.form.getRawValue();
    const payload = {
      policyNumber,
      claimType,
      description,
      incidentDate: `${incidentDate}T12:00:00`,
      claimedAmount: Number(claimedAmount),
    };

    this.claimService.initiateClaim(payload).pipe(
      switchMap((res) => {
        const claimId = res.data?.id;
        const claimNumber = res.data?.claimNumber || `#${claimId}`;

        if (!claimId || this.selectedFiles.length === 0) {
          return of({ claimNumber });
        }

        return forkJoin(this.selectedFiles.map(file => this.claimService.uploadDocument(claimId, file))).pipe(
          switchMap(() => of({ claimNumber }))
        );
      })
    ).subscribe({
      next: ({ claimNumber }) => {
        this.success = `Claim filed successfully. Claim number: ${claimNumber}`;
        this.loading = false;
        this.toastr.success(this.success);
        this.form.reset();
        this.selectedFiles = [];
        this.selectedPolicy = null;
        this.waitingPeriodEnd = null;
        this.router.navigate(['/customer/claims/my-claims']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to file claim';
        this.toastr.error(this.error);
        this.loading = false;
      }
    });
  }
}
