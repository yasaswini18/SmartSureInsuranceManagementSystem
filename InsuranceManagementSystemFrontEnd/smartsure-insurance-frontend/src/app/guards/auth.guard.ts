import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn()) return true;
  router.navigate(['/login']);
  return false;
};

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const requiredRole = route.data?.['role'] as string | undefined;
  const currentRole = auth.getCurrentUser()?.role;

  if (!auth.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  if (!requiredRole) {
    return true;
  }

  const matchesCustomer = requiredRole === 'CUSTOMER' && (currentRole === 'USER' || currentRole === 'CUSTOMER');
  if (currentRole === requiredRole || matchesCustomer) {
    return true;
  }

  router.navigate(['/unauthorized']);
  return false;
};

export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) return true;
  if (auth.isAdmin()) router.navigate(['/admin/dashboard']);
  else router.navigate(['/customer/dashboard']);
  return false;
};
