import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { Observable } from 'rxjs';

export interface TransferResponse {
  id: string;
  status: string;
  fromWalletId: string;
  toWalletId: string;
  amount: number;
  currency: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private baseUrl = 'http://localhost:8080/api/v1/transactions';

  transfer(toWalletId: string, amount: number, latitude?: number, longitude?: number): Observable<TransferResponse> {
    const idempotencyKey = crypto.randomUUID();
    const fromWalletId = this.authService.walletId;

    return this.http.post<TransferResponse>(`${this.baseUrl}/transfer`, {
      idempotencyKey,
      fromWalletId,
      toWalletId,
      amount,
      latitude,
      longitude
    });
  }

  payBill(billerId: string, customerId: string, amount: number): Observable<TransferResponse> {
    const idempotencyKey = crypto.randomUUID();
    const fromWalletId = this.authService.walletId;

    return this.http.post<TransferResponse>(`${this.baseUrl}/pay-bill`, {
      idempotencyKey,
      fromWalletId,
      billerId,
      customerId,
      amount
    });
  }
}
