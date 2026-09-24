import { environment } from '../../environments/environment';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private baseUrl = environment.apiUrl + '/wallets';
  
  getBalance() {
    const walletId = this.authService.walletId;
    return this.http.get<any>(`${this.baseUrl}/${walletId}/balance`);
  }
  
  getTransactions(page = 0, size = 20) {
    const walletId = this.authService.walletId;
    return this.http.get<any>(`${this.baseUrl}/${walletId}/transactions`, {
      params: { page, size }
    });
  }

  deposit(amount: number) {
    const walletId = this.authService.walletId;
    const idempotencyKey = crypto.randomUUID();
    return this.http.post<any>(`${this.baseUrl}/${walletId}/deposit`, {
      idempotencyKey,
      amount
    });
  }

  withdraw(amount: number) {
    const walletId = this.authService.walletId;
    const idempotencyKey = crypto.randomUUID();
    return this.http.post<any>(`${this.baseUrl}/${walletId}/withdraw`, {
      idempotencyKey,
      amount
    });
  }

  downloadStatementPdf() {
    const walletId = this.authService.walletId;
    return this.http.get(`${environment.apiUrl}/statements/${walletId}/pdf`, {
      responseType: 'blob'
    });
  }

  downloadStatementCsv() {
    const walletId = this.authService.walletId;
    return this.http.get(`${environment.apiUrl}/statements/${walletId}/csv`, {
      responseType: 'blob'
    });
  }
}

