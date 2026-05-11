import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { User } from '../../models';
import { PaginationComponent } from '../shared/pagination.component';
import { PaginationHelper } from '../shared/pagination.helper';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginationComponent],
  template: `
<div class="p-6 max-w-7xl mx-auto animate-enter">
  <div class="mb-8">
    <h1 class="section-title">Users</h1>
    <p class="section-subtitle">All registered customers and administrators</p>
  </div>

  <div class="mb-5 flex items-center gap-3">
    <input [(ngModel)]="search" (input)="applyFilter()" type="text" class="input-field max-w-xs"
      placeholder="Search by name or email..." />
    <span class="text-xs text-slate-500">{{ paginator.total }} total</span>
  </div>

  <div *ngIf="loading" class="flex justify-center py-20">
    <svg class="w-8 h-8 animate-spin text-primary-400" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
    </svg>
  </div>

  <div *ngIf="!loading" class="card overflow-hidden">
    <div *ngIf="paginator.total === 0" class="p-10 text-center text-slate-500 text-sm">No users found.</div>
    <table *ngIf="paginator.total > 0" class="w-full">
      <thead class="bg-slate-800/60 border-b border-slate-700/50">
        <tr>
          <th class="table-header text-left">User</th>
          <th class="table-header text-left hidden md:table-cell">Email</th>
          <th class="table-header text-left hidden lg:table-cell">Phone</th>
          <th class="table-header text-center">Role</th>
          <th class="table-header text-center">Status</th>
          <th class="table-header text-center hidden md:table-cell">Joined</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-slate-800/60">
        <tr *ngFor="let u of paginator.paged" class="hover:bg-slate-800/30 transition-colors">
          <td class="table-cell">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold"
                [class]="u.role === 'ADMIN' ? 'bg-amber-500/20 text-amber-400' : 'bg-primary-500/20 text-primary-400'">
                {{ (u.name || '?').charAt(0).toUpperCase() }}
              </div>
              <span class="text-sm font-medium text-white">{{ u.name }}</span>
            </div>
          </td>
          <td class="table-cell hidden md:table-cell text-slate-400 text-sm">{{ u.email }}</td>
          <td class="table-cell hidden lg:table-cell text-slate-400 text-sm">{{ u.phone || '—' }}</td>
          <td class="table-cell text-center">
            <span class="status-badge text-xs"
              [class]="u.role === 'ADMIN' ? 'text-amber-400 bg-amber-400/10' : 'text-primary-400 bg-primary-400/10'">
              {{ u.role }}
            </span>
          </td>
          <td class="table-cell text-center">
            <span class="status-badge text-xs"
              [class]="u.enabled !== false ? 'text-emerald-400 bg-emerald-400/10' : 'text-red-400 bg-red-400/10'">
              {{ u.enabled !== false ? 'Active' : 'Disabled' }}
            </span>
          </td>
          <td class="table-cell hidden md:table-cell text-center text-slate-400 text-xs">
            {{ u.createdAt | date:'mediumDate' }}
          </td>
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
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  paginator = new PaginationHelper<User>(10);
  loading = true;
  search = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getAllUsers().subscribe({
      next: (res) => {
        this.users = (res.data || []).map(user => ({
          ...user,
          name: user.name || user.fullName || user.email,
        }));
        this.paginator.setData(this.users);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  applyFilter(): void {
    const q = this.search.toLowerCase();
    const filtered = this.users.filter(u =>
      (u.name || u.fullName || '').toLowerCase().includes(q) || u.email?.toLowerCase().includes(q)
    );
    this.paginator.setData(filtered);
  }
}
