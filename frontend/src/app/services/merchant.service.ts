import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface MerchantCreateRequest {
  businessName: string;
  category: string;
  settlementAccount: string;
}

export interface MerchantResponse {
  id: string;
  businessName: string;
  category: string;
  settlementAccount: string;
  status: string;
  walletId: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class MerchantService {
  // Assuming a generic API URL if environment is not set up
  private apiUrl = 'http://localhost:8080/api/v1/merchants';

  constructor(private http: HttpClient) {}

  onboardMerchant(request: MerchantCreateRequest): Observable<MerchantResponse> {
    return this.http.post<MerchantResponse>(`${this.apiUrl}/onboard`, request);
  }

  getMerchantProfile(): Observable<MerchantResponse> {
    return this.http.get<MerchantResponse>(`${this.apiUrl}/profile`);
  }
}
