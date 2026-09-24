import { environment } from '../../environments/environment';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';
import { NotificationService } from './notification.service';

export interface AuthResponse {
  walletId: string;
  role: string;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private notificationService = inject(NotificationService);
  private baseUrl = environment.apiUrl + '/auth';

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();
  
  private pendingAuthRequest: any = null;

  constructor() {
    const saved = localStorage.getItem('auth_user');
    if (saved) {
      const auth = JSON.parse(saved);
      this.currentUserSubject.next(auth);
      this.currentUserSubject.next(auth);
      this.notificationService.connect();
    }
  }

  get walletId(): string | null {
    return this.currentUserSubject.value?.walletId || null;
  }

  get isAdmin(): boolean {
    return this.currentUserSubject.value?.role === 'ADMIN';
  }

  setPendingAuthRequest(request: any) {
    this.pendingAuthRequest = request;
  }

  getPendingAuthRequest(): any {
    const req = this.pendingAuthRequest;
    this.pendingAuthRequest = null;
    return req;
  }

  login(credentials: any) {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, credentials).pipe(
      tap(res => this.setSession(res))
    );
  }

  register(userData: any) {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, userData).pipe(
      tap(res => this.setSession(res))
    );
  }

  logout() {
    this.http.post(`${this.baseUrl}/logout`, {}, { withCredentials: true }).subscribe({
      next: () => this.clearSession(),
      error: () => this.clearSession()
    });
  }

  private clearSession() {
    localStorage.removeItem('auth_user');
    this.currentUserSubject.next(null);
    this.notificationService.disconnect();
    this.router.navigate(['/login']);
  }

  private setSession(authResult: AuthResponse) {
    localStorage.setItem('auth_user', JSON.stringify(authResult));
    this.currentUserSubject.next(authResult);
    this.notificationService.connect();
  }
}
