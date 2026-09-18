import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PageData<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ReportTransaction {
  id: string;
  fromWalletId: string;
  toWalletId: string;
  amount: number;
  currency: string;
  status: string;
  isFraudulent: boolean;
  createdAt: string;
}

export interface ReportUser {
  id: string;
  name: string;
  email: string;
  phoneNumber: string;
  role: string;
  kycStatus: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminReportService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1/admin/reports';

  getTransactions(status: string | null, page: number = 0, size: number = 10): Observable<PageData<ReportTransaction>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (status && status !== 'ALL') {
      params = params.set('status', status);
    }
    return this.http.get<PageData<ReportTransaction>>(`${this.baseUrl}/transactions`, { params });
  }

  getUsers(role: string | null, page: number = 0, size: number = 10): Observable<PageData<ReportUser>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (role && role !== 'ALL') {
      params = params.set('role', role);
    }
    return this.http.get<PageData<ReportUser>>(`${this.baseUrl}/users`, { params });
  }

  downloadTransactionsCsv(status: string | null = null): Observable<Blob> {
    let params = new HttpParams();
    if (status && status !== 'ALL') {
      params = params.set('status', status);
    }
    return this.http.get(`${this.baseUrl}/transactions/csv`, { params, responseType: 'blob' });
  }

  downloadUsersCsv(role: string | null = null): Observable<Blob> {
    let params = new HttpParams();
    if (role && role !== 'ALL') {
      params = params.set('role', role);
    }
    return this.http.get(`${this.baseUrl}/users/csv`, { params, responseType: 'blob' });
  }

  saveBlob(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}
