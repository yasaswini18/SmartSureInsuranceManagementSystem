import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./components/auth/home/home.component').then(m => m.HomeComponent) },
  { path: 'login', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'register', redirectTo: 'auth/register', pathMatch: 'full' },
  { path: 'unauthorized', loadComponent: () => import('./components/shared/unauthorized.component').then(m => m.UnauthorizedComponent) },
  {
    path: 'auth',
    canActivate: [guestGuard],
    children: [
      { path: 'login', loadComponent: () => import('./components/auth/login.component').then(m => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./components/auth/register.component').then(m => m.RegisterComponent) },
      { path: '', redirectTo: 'login', pathMatch: 'full' },
    ]
  },
  {
    path: 'customer',
    loadComponent: () => import('./components/layout/layout.component').then(m => m.LayoutComponent),
    canActivate: [authGuard, roleGuard],
    data: { role: 'USER' },
    children: [
      { path: 'dashboard', loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'policies/browse', loadComponent: () => import('./components/policies/browse-policies.component').then(m => m.BrowsePoliciesComponent) },
      { path: 'policies/my-policies', loadComponent: () => import('./components/policies/my-policies.component').then(m => m.MyPoliciesComponent) },
      { path: 'claims/my-claims', loadComponent: () => import('./components/claims/my-claims.component').then(m => m.MyClaimsComponent) },
      { path: 'claims/new', loadComponent: () => import('./components/claims/new-claim.component').then(m => m.NewClaimComponent) },
      { path: 'claims/:id', loadComponent: () => import('./components/claims/claim-detail.component').then(m => m.ClaimDetailComponent) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ]
  },
  {
    path: 'admin',
    loadComponent: () => import('./components/layout/layout.component').then(m => m.LayoutComponent),
    canActivate: [authGuard, roleGuard],
    data: { role: 'ADMIN' },
    children: [
      { path: 'dashboard', loadComponent: () => import('./components/admin/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'claims', loadComponent: () => import('./components/admin/admin-claims.component').then(m => m.AdminClaimsComponent) },
      { path: 'policies', loadComponent: () => import('./components/admin/admin-policies.component').then(m => m.AdminPoliciesComponent) },
      { path: 'purchased-policies', loadComponent: () => import('./components/admin/admin-purchased-policies.component').then(m => m.AdminPurchasedPoliciesComponent) },
      { path: 'users', loadComponent: () => import('./components/admin/admin-users.component').then(m => m.AdminUsersComponent) },
      { path: 'reports', loadComponent: () => import('./components/admin/admin-reports.component').then(m => m.AdminReportsComponent) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ]
  },
  { path: 'dashboard', redirectTo: 'customer/dashboard', pathMatch: 'full' },
  { path: 'policies/browse', redirectTo: 'customer/policies/browse', pathMatch: 'full' },
  { path: 'policies/my-policies', redirectTo: 'customer/policies/my-policies', pathMatch: 'full' },
  { path: 'claims/my-claims', redirectTo: 'customer/claims/my-claims', pathMatch: 'full' },
  { path: 'claims/new', redirectTo: 'customer/claims/new', pathMatch: 'full' },
  { path: '**', redirectTo: '' }
];
