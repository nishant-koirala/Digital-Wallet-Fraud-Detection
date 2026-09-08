import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private baseUrl = 'http://localhost:8080/api/v1/transactions';

  transfer(toWalletId: string, amount: number, latitude: number = 0, longitude: number = 0) {
    const fromWalletId = this.authService.walletId;
    const idempotencyKey = crypto.randomUUID();
    return this.http.post<any>(`${this.baseUrl}/transfer`, {
      idempotencyKey,
      fromWalletId,
      toWalletId,
      amount,
      latitude,
      longitude
    });
  }
}
