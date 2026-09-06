import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private baseUrl = 'http://localhost:8080/api/v1/wallets';
  
  getBalance() {
    const walletId = this.authService.walletId;
    return this.http.get<any>(`${this.baseUrl}/${walletId}/balance`);
  }
  
  getTransactions() {
    const walletId = this.authService.walletId;
    return this.http.get<any[]>(`${this.baseUrl}/${walletId}/transactions`);
  }
}
