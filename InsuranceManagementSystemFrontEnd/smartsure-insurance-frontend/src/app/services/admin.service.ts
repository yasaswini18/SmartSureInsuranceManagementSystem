import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, DashboardReport, AuditLog, User, Claim, PurchasedPolicy, PolicyProduct, PolicyProductRequest, ReviewRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly BASE_URL = '/api/admin';

  constructor(private http: HttpClient) {}

  // Reports
  getDashboard(): Observable<ApiResponse<DashboardReport>> {
    return this.http.get<ApiResponse<DashboardReport>>(`${this.BASE_URL}/reports/dashboard`);
  }

  getClaimsReport(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.BASE_URL}/reports/claims`);
  }

  getPoliciesReport(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.BASE_URL}/reports/policies`);
  }

  getRevenueReport(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.BASE_URL}/reports/revenue`);
  }

  getAuditLogs(): Observable<ApiResponse<AuditLog[]>> {
    return this.http.get<ApiResponse<AuditLog[]>>(`${this.BASE_URL}/reports/audit-logs`);
  }

  // Users
  getAllUsers(): Observable<ApiResponse<User[]>> {
    return this.http.get<ApiResponse<User[]>>(`${this.BASE_URL}/users`);
  }

  getUserByEmail(email: string): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${this.BASE_URL}/users/${email}`);
  }

  getUserCount(): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.BASE_URL}/users/count`);
  }

  // Claims
  getAllClaims(): Observable<ApiResponse<Claim[]>> {
    return this.http.get<ApiResponse<Claim[]>>(`${this.BASE_URL}/claims`);
  }

  getPendingClaims(): Observable<ApiResponse<Claim[]>> {
    return this.http.get<ApiResponse<Claim[]>>(`${this.BASE_URL}/claims/pending`);
  }

  getClaimById(claimId: number): Observable<ApiResponse<Claim>> {
    return this.http.get<ApiResponse<Claim>>(`${this.BASE_URL}/claims/${claimId}`);
  }

  startReview(claimId: number): Observable<ApiResponse<Claim>> {
    return this.http.put<ApiResponse<Claim>>(`${this.BASE_URL}/claims/${claimId}/start-review`, {});
  }

  reviewClaim(claimId: number, data: ReviewRequest): Observable<ApiResponse<Claim>> {
    return this.http.put<ApiResponse<Claim>>(`${this.BASE_URL}/claims/${claimId}/review`, data);
  }

  settleClaim(claimId: number): Observable<ApiResponse<Claim>> {
    return this.http.put<ApiResponse<Claim>>(`${this.BASE_URL}/claims/${claimId}/settle`, {});
  }

  downloadDocument(url: string): Observable<Blob> {
    return this.http.get(url, { responseType: 'blob' });
  }

  // Policy Products (Admin)
  getAllProducts(): Observable<ApiResponse<PolicyProduct[]>> {
    return this.http.get<ApiResponse<PolicyProduct[]>>(`${this.BASE_URL}/policies`);
  }

  createProduct(data: PolicyProductRequest): Observable<ApiResponse<PolicyProduct>> {
    return this.http.post<ApiResponse<PolicyProduct>>(`${this.BASE_URL}/policies`, data);
  }

  updateProduct(id: number, data: PolicyProductRequest): Observable<ApiResponse<PolicyProduct>> {
    return this.http.put<ApiResponse<PolicyProduct>>(`${this.BASE_URL}/policies/${id}`, data);
  }

  deleteProduct(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.BASE_URL}/policies/${id}`);
  }

  reactivateProduct(id: number): Observable<ApiResponse<PolicyProduct>> {
    return this.http.put<ApiResponse<PolicyProduct>>(`${this.BASE_URL}/policies/${id}/reactivate`, {});
  }

  getAllPurchasedPolicies(): Observable<ApiResponse<PurchasedPolicy[]>> {
    return this.http.get<ApiResponse<PurchasedPolicy[]>>(`${this.BASE_URL}/policies/purchased`);
  }
}
