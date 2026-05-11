import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  template: `
<div *ngIf="totalPages > 1" class="flex items-center justify-between px-4 py-3 border-t border-slate-800">
  <!-- Info -->
  <p class="text-xs text-slate-500">
    Showing <span class="text-slate-300 font-medium">{{ startItem }}–{{ endItem }}</span>
    of <span class="text-slate-300 font-medium">{{ totalItems }}</span> results
  </p>

  <!-- Controls -->
  <div class="flex items-center gap-1">
    <!-- Prev -->
    <button (click)="go(currentPage - 1)" [disabled]="currentPage === 1"
      class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 hover:text-white hover:bg-slate-700 disabled:opacity-30 disabled:cursor-not-allowed transition-all">
      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
      </svg>
    </button>

    <!-- Page numbers -->
    <ng-container *ngFor="let p of visiblePages">
      <span *ngIf="p === -1" class="w-8 h-8 flex items-center justify-center text-slate-600 text-xs">…</span>
      <button *ngIf="p !== -1" (click)="go(p)"
        class="w-8 h-8 flex items-center justify-center rounded-lg text-xs font-medium transition-all"
        [class]="p === currentPage
          ? 'bg-primary-600 text-white'
          : 'text-slate-400 hover:text-white hover:bg-slate-700'">
        {{ p }}
      </button>
    </ng-container>

    <!-- Next -->
    <button (click)="go(currentPage + 1)" [disabled]="currentPage === totalPages"
      class="w-8 h-8 flex items-center justify-center rounded-lg text-slate-400 hover:text-white hover:bg-slate-700 disabled:opacity-30 disabled:cursor-not-allowed transition-all">
      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
      </svg>
    </button>
  </div>
</div>
  `,
})
export class PaginationComponent implements OnChanges {
  @Input() totalItems = 0;
  @Input() pageSize = 10;
  @Input() currentPage = 1;
  @Output() pageChange = new EventEmitter<number>();

  totalPages = 0;
  visiblePages: number[] = [];

  get startItem(): number { return (this.currentPage - 1) * this.pageSize + 1; }
  get endItem(): number { return Math.min(this.currentPage * this.pageSize, this.totalItems); }

  ngOnChanges(): void {
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    this.buildPages();
  }

  go(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.pageChange.emit(page);
  }

  buildPages(): void {
    const total = this.totalPages;
    const current = this.currentPage;
    const pages: number[] = [];

    if (total <= 7) {
      for (let i = 1; i <= total; i++) pages.push(i);
    } else {
      pages.push(1);
      if (current > 3) pages.push(-1); 
      const start = Math.max(2, current - 1);
      const end = Math.min(total - 1, current + 1);
      for (let i = start; i <= end; i++) pages.push(i);
      if (current < total - 2) pages.push(-1);
      pages.push(total);
    }

    this.visiblePages = pages;
  }
}
