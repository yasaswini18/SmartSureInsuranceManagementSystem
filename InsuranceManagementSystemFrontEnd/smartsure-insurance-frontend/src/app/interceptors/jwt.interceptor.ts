import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

let refreshInProgress = false;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();
  const isPublicAuthRequest =
    req.url.includes('/api/auth/login') ||
    req.url.includes('/api/auth/register') ||
    req.url.includes('/api/auth/refresh');

  const authReq = token && !isPublicAuthRequest
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const isUnauthorized = error.status === 401;
      const hasRefreshToken = !!authService.getRefreshToken();
      const isLogoutRequest = req.url.includes('/api/auth/logout');

      if (!isUnauthorized || isPublicAuthRequest || isLogoutRequest || !hasRefreshToken || refreshInProgress) {
        if (isUnauthorized && !isPublicAuthRequest && !isLogoutRequest && !hasRefreshToken) {
          authService.forceClientLogout();
        }
        return throwError(() => error);
      }

      refreshInProgress = true;

      return authService.refreshAccessToken().pipe(
        switchMap((response) => {
          refreshInProgress = false;
          const newToken = response.accessToken || response.token;

          if (!newToken) {
            authService.forceClientLogout();
            return throwError(() => error);
          }

          return next(req.clone({
            setHeaders: { Authorization: `Bearer ${newToken}` }
          }));
        }),
        catchError((refreshError) => {
          refreshInProgress = false;
          authService.forceClientLogout();
          return throwError(() => refreshError);
        })
      );
    })
  );
};
