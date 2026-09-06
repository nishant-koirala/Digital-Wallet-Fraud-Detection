import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, switchMap, tap } from 'rxjs/operators';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1';
  
  private activeWalletId = new BehaviorSubject<string | null>(null);
  activeWalletId$ = this.activeWalletId.asObservable();

  seedAndInitialize() {
    return this.http.post<any>(`${this.baseUrl}/dev/seed`, {}).pipe(
      tap(res => {
        this.activeWalletId.next(res.aliceWalletId);
      })
    );
  }
  
  getBalance() {
    return this.activeWalletId$.pipe(
      switchMap(walletId => this.http.get<any>(`${this.baseUrl}/wallets/${walletId}/balance`))
    );
  }
  
  getTransactions() {
    return this.activeWalletId$.pipe(
      switchMap(walletId => this.http.get<any[]>(`${this.baseUrl}/wallets/${walletId}/transactions`))
    );
  }
}
