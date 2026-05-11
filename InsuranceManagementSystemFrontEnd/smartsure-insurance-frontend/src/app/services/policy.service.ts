import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PolicyProduct, PolicyProductRequest, PurchasedPolicy, PurchasePolicyRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class PolicyService {
  private readonly BASE_URL = '/api/policies';

  constructor(private http: HttpClient) {}

  // Products
  getAllProducts(): Observable<ApiResponse<PolicyProduct[]>> {
    return this.http.get<ApiResponse<PolicyProduct[]>>(`${this.BASE_URL}/products`);
  }

  getProduct(id: number): Observable<ApiResponse<PolicyProduct>> {
    return this.http.get<ApiResponse<PolicyProduct>>(`${this.BASE_URL}/products/${id}`);
  }

  createProduct(data: PolicyProductRequest): Observable<ApiResponse<PolicyProduct>> {
    return this.http.post<ApiResponse<PolicyProduct>>(`${this.BASE_URL}/products`, data);
  }

  updateProduct(id: number, data: PolicyProductRequest): Observable<ApiResponse<PolicyProduct>> {
    return this.http.put<ApiResponse<PolicyProduct>>(`${this.BASE_URL}/products/${id}`, data);
  }

  deleteProduct(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.BASE_URL}/products/${id}`);
  }

  // Purchased Policies
  purchasePolicy(data: PurchasePolicyRequest): Observable<ApiResponse<PurchasedPolicy>> {
    return this.http.post<ApiResponse<PurchasedPolicy>>(`${this.BASE_URL}/purchase`, data);
  }

  getMyPolicies(): Observable<ApiResponse<PurchasedPolicy[]>> {
    return this.http.get<ApiResponse<PurchasedPolicy[]>>(`${this.BASE_URL}/my-policies`);
  }

  getPolicyDetails(policyId: number): Observable<ApiResponse<PurchasedPolicy>> {
    return this.http.get<ApiResponse<PurchasedPolicy>>(`${this.BASE_URL}/${policyId}`);
  }

  getAllPolicies(): Observable<ApiResponse<PurchasedPolicy[]>> {
    return this.http.get<ApiResponse<PurchasedPolicy[]>>(`${this.BASE_URL}/all`);
  }

  cancelPolicy(policyId: number): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(`${this.BASE_URL}/${policyId}/cancel`, {});
  }
}
