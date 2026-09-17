import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface KycStatusResponse {
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'NOT_SUBMITTED';
  documentType?: string;
}

export interface KycDocument {
  id: string;
  userId: string;
  documentType: string;
  documentNumber: string;
  frontImageUrl: string;
  backImageUrl: string;
  status: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class KycService {
  private http = inject(HttpClient);
  private userBaseUrl = 'http://localhost:8080/api/v1/kyc';
  private adminBaseUrl = 'http://localhost:8080/api/v1/admin/kyc';

  // User Actions
  getStatus(): Observable<KycStatusResponse> {
    return this.http.get<KycStatusResponse>(this.userBaseUrl);
  }

  submitKyc(formData: any): Observable<void> {
    return this.http.post<void>(this.userBaseUrl, formData);
  }

  // Admin Actions
  getPending(): Observable<KycDocument[]> {
    return this.http.get<KycDocument[]>(`${this.adminBaseUrl}/pending`);
  }

  approve(id: string): Observable<void> {
    return this.http.post<void>(`${this.adminBaseUrl}/${id}/approve`, {});
  }

  reject(id: string): Observable<void> {
    return this.http.post<void>(`${this.adminBaseUrl}/${id}/reject`, {});
  }
}
