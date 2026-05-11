import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { AdminService } from '../../services/admin.service';
import { Claim } from '../../models';
import { PaginationComponent } from '../shared/pagination.component';
import { PaginationHelper } from '../shared/pagination.helper';

@Component({
  selector: 'app-admin-claims',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationComponent],
  templateUrl: './admin-claims.component.html',
})
export class AdminClaimsComponent implements OnInit {
  claims: Claim[] = [];
  filtered: Claim[] = [];
  paginator = new PaginationHelper<Claim>(10);
  loading = true;
  selected: Claim | null = null;
  reviewModal = false;
  reviewStatus: 'APPROVED' | 'REJECTED' = 'APPROVED';
  adminRemarks = '';
  approvedAmount: number | null = null;
  reviewLoading = false;
  error = '';
  success = '';
  filterStatus = 'ALL';
  statuses = ['ALL', 'PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'SETTLED'];
  openingDocument = '';
  previewDocumentName = '';
  previewDocumentUrl = '';
  previewDocumentType = '';

  constructor(private adminService: AdminService, private toastr: ToastrService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.adminService.getAllClaims().subscribe({
      next: (res) => {
        this.claims = res.data || [];
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load claims.';
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    this.filtered = this.filterStatus === 'ALL'
      ? this.claims
      : this.claims.filter(c => c.status === this.filterStatus);
    this.paginator.setData(this.filtered);
  }

  openReview(claim: Claim): void {
    this.reviewStatus = 'APPROVED';
    this.adminRemarks = '';
    this.error = '';
    this.reviewModal = true;

    this.adminService.getClaimById(claim.id).subscribe({
      next: (res) => {
        this.selected = res.data || claim;
        this.approvedAmount = this.selected?.claimedAmount || null;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load claim details.';
        this.selected = claim;
        this.approvedAmount = claim.claimedAmount;
      }
    });
  }

  startReview(claim: Claim): void {
    this.adminService.startReview(claim.id).subscribe({
      next: () => {
        this.success = `Claim ${claim.claimNumber} moved to Under Review`;
        this.toastr.success(this.success);
        this.load();
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to start review';
        this.toastr.error(this.error);
      }
    });
  }

  submitReview(): void {
    if (!this.selected) {
      return;
    }

    if (!this.adminRemarks.trim()) {
      this.error = 'Remarks are required';
      return;
    }

    if (this.reviewStatus === 'APPROVED' && (!this.approvedAmount || this.approvedAmount <= 0)) {
      this.error = 'Approved amount is required';
      return;
    }

    this.reviewLoading = true;
    this.adminService.reviewClaim(this.selected.id, {
      decision: this.reviewStatus,
      adminRemarks: this.adminRemarks,
      approvedAmount: this.reviewStatus === 'APPROVED' ? Number(this.approvedAmount) : null
    }).subscribe({
      next: () => {
        this.success = `Claim ${this.selected?.claimNumber} has been ${this.reviewStatus.toLowerCase()}`;
        this.reviewLoading = false;
        this.reviewModal = false;
        this.toastr.success(this.success);
        this.load();
      },
      error: (err) => {
        this.error = err.error?.message || 'Review failed';
        this.toastr.error(this.error);
        this.reviewLoading = false;
      }
    });
  }

  get documents(): Array<{ fileName?: string; fileType?: string; fileSize?: string; downloadUrl?: string; uploadedAt?: string }> {
    const docs = this.selected?.documents;
    return Array.isArray(docs) ? docs as Array<{ fileName?: string; fileType?: string; fileSize?: string; downloadUrl?: string; uploadedAt?: string }> : [];
  }

  openDocument(document: { fileName?: string; fileType?: string; downloadUrl?: string }): void {
    if (!document.downloadUrl) {
      this.toastr.error('Document link is not available for this file.');
      return;
    }

    this.openingDocument = document.fileName || document.downloadUrl;
    this.adminService.downloadDocument(document.downloadUrl).subscribe({
      next: (blob) => {
        this.clearPreviewUrl();
        this.previewDocumentName = document.fileName || 'Document';
        this.previewDocumentType = document.fileType || blob.type || '';
        this.previewDocumentUrl = URL.createObjectURL(blob);
        this.openingDocument = '';
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to open document.';
        this.toastr.error(this.error);
        this.openingDocument = '';
      }
    });
  }

  downloadDocument(document: { fileName?: string; downloadUrl?: string }): void {
    if (!document.downloadUrl) {
      this.toastr.error('Document link is not available for this file.');
      return;
    }

    this.openingDocument = document.fileName || document.downloadUrl;
    this.adminService.downloadDocument(document.downloadUrl).subscribe({
      next: (blob) => {
        const blobUrl = URL.createObjectURL(blob);
        const anchor = window.document.createElement('a');
        anchor.href = blobUrl;
        anchor.download = document.fileName || 'claim-document';
        anchor.click();
        URL.revokeObjectURL(blobUrl);
        this.openingDocument = '';
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to download document.';
        this.toastr.error(this.error);
        this.openingDocument = '';
      }
    });
  }

  closePreview(): void {
    this.clearPreviewUrl();
    this.previewDocumentName = '';
    this.previewDocumentType = '';
  }

  isImagePreview(): boolean {
    return this.previewDocumentType.toLowerCase().includes('image');
  }

  private clearPreviewUrl(): void {
    if (this.previewDocumentUrl) {
      URL.revokeObjectURL(this.previewDocumentUrl);
      this.previewDocumentUrl = '';
    }
  }

  statusColor(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'text-yellow-400 bg-yellow-400/10',
      UNDER_REVIEW: 'text-orange-400 bg-orange-400/10',
      APPROVED: 'text-emerald-400 bg-emerald-400/10',
      REJECTED: 'text-red-400 bg-red-400/10',
      SETTLED: 'text-slate-500 bg-slate-500/10',
    };
    return map[status] || 'text-slate-400 bg-slate-400/10';
  }
}
