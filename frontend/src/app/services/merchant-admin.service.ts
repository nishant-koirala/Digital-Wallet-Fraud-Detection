import { environment } from '../../environments/environment';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MerchantResponse } from './merchant.service';

@Injectable({
  providedIn: 'root'
})
export class MerchantAdminService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl + '/admin/merchants';

  getPendingMerchants(): Observable<MerchantResponse[]> {
    return this.http.get<MerchantResponse[]>(`${this.baseUrl}/pending`);
  }

  approveMerchant(merchantId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${merchantId}/approve`, {});
  }

  rejectMerchant(merchantId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${merchantId}/reject`, {});
  }
}

