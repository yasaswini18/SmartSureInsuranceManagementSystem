import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Claim, ClaimRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ClaimService {
  private readonly BASE_URL = '/api/claims';

  constructor(private http: HttpClient) {}

  initiateClaim(data: ClaimRequest): Observable<ApiResponse<Claim>> {
    return this.http.post<ApiResponse<Claim>>(`${this.BASE_URL}/initiate`, data);
  }

  uploadDocument(claimId: number, file: File): Observable<ApiResponse<any>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<any>>(`${this.BASE_URL}/${claimId}/documents`, formData);
  }

  getMyClaims(): Observable<ApiResponse<Claim[]>> {
    return this.http.get<ApiResponse<Claim[]>>(`${this.BASE_URL}/my-claims`);
  }

  getClaimStatus(claimId: number): Observable<ApiResponse<Claim>> {
    return this.http.get<ApiResponse<Claim>>(`${this.BASE_URL}/status/${claimId}`);
  }

  getClaimDetails(claimId: number): Observable<ApiResponse<Claim>> {
    return this.http.get<ApiResponse<Claim>>(`${this.BASE_URL}/${claimId}`);
  }
}
