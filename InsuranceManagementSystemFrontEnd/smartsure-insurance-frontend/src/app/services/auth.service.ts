import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly BASE_URL = '/api/auth';
  private readonly STORAGE_KEY = 'smartsure_user';
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.loadUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  private loadUser(): AuthResponse | null {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    if (!stored) {
      return null;
    }

    try {
      const parsed = JSON.parse(stored) as AuthResponse;
      return this.normalizeAuthResponse(parsed);
    } catch {
      localStorage.removeItem(this.STORAGE_KEY);
      return null;
    }
  }

  private saveUser(user: AuthResponse | null): void {
    if (!user) {
      localStorage.removeItem(this.STORAGE_KEY);
      this.currentUserSubject.next(null);
      return;
    }

    const normalized = this.normalizeAuthResponse(user);
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(normalized));
    this.currentUserSubject.next(normalized);
  }

  private normalizeAuthResponse(user: AuthResponse): AuthResponse {
    return {
      ...user,
      accessToken: user.accessToken || user.token,
      token: user.token || user.accessToken,
      tokenType: user.tokenType || 'Bearer'
    };
  }

  get currentUser(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  get userName(): string {
    const user = this.currentUser;
    return user?.name || user?.email || 'User';
  }

  get token(): string | null {
    return this.getToken();
  }

  get loggedIn(): boolean {
    return this.isLoggedIn();
  }

  get admin(): boolean {
    return this.isAdmin();
  }

  get customer(): boolean {
    return this.isCustomer();
  }

  getToken(): string | null {
    const stored = this.loadUser();
    const token = stored?.accessToken?.trim() || stored?.token?.trim();
    return token ? token : null;
  }

  getRefreshToken(): string | null {
    const stored = this.loadUser();
    const refreshToken = stored?.refreshToken?.trim();
    return refreshToken ? refreshToken : null;
  }

  getCurrentUser(): AuthResponse | null {
    return this.loadUser();
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.getCurrentUser()?.role === 'ADMIN';
  }

  isCustomer(): boolean {
    const role = this.getCurrentUser()?.role;
    return role === 'USER' || role === 'CUSTOMER';
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE_URL}/register`, data);
  }

  login(email: string, password: string): Observable<AuthResponse>;
  login(data: LoginRequest): Observable<AuthResponse>;
  login(emailOrData: string | LoginRequest, password?: string): Observable<AuthResponse> {
    const payload = typeof emailOrData === 'string'
      ? { email: emailOrData, password: password ?? '' }
      : emailOrData;

    return this.http.post<AuthResponse>(`${this.BASE_URL}/login`, payload).pipe(
      tap(response => {
        this.saveUser(response);
      })
    );
  }

  refreshAccessToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('Refresh token is missing'));
    }

    return this.http.post<AuthResponse>(`${this.BASE_URL}/refresh`, { refreshToken }).pipe(
      tap(response => {
        const current = this.currentUser;
        this.saveUser({
          ...current,
          ...response,
          refreshToken: response.refreshToken || refreshToken
        });
      })
    );
  }

  logout(redirectToLogin = true): void {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      this.finishLogout(redirectToLogin);
      return;
    }

    this.http.post(`${this.BASE_URL}/logout`, { refreshToken }).subscribe({
      next: () => this.finishLogout(redirectToLogin),
      error: () => this.finishLogout(redirectToLogin)
    });
  }

  forceClientLogout(redirectToLogin = true): void {
    this.finishLogout(redirectToLogin);
  }

  private finishLogout(redirectToLogin: boolean): void {
    this.saveUser(null);
    if (redirectToLogin) {
      this.router.navigate(['/login']);
    }
  }
}
