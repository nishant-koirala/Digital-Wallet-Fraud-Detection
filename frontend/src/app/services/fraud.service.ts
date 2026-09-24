import { environment } from '../../environments/environment';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class FraudService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl + '/fraud-flags';

  getPendingFlags() {
    return this.http.get<any[]>(`${this.baseUrl}/pending`);
  }

  approve(transactionId: string, adminUserId: string = 'admin_123') {
    return this.http.post<any>(`${this.baseUrl}/${transactionId}/approve`, { adminUserId });
  }

  reject(transactionId: string, adminUserId: string = 'admin_123') {
    return this.http.post<any>(`${this.baseUrl}/${transactionId}/reject`, { adminUserId });
  }
}

