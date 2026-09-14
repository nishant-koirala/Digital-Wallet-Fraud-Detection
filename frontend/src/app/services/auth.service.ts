import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface AuthResponse {
  token: string;
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
  private baseUrl = 'http://localhost:8080/api/v1/auth';

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    const saved = localStorage.getItem('auth_user');
    if (saved) {
      this.currentUserSubject.next(JSON.parse(saved));
    }
  }

  get token(): string | null {
    return this.currentUserSubject.value?.token || null;
  }

  get walletId(): string | null {
    return this.currentUserSubject.value?.walletId || null;
  }

  get isAdmin(): boolean {
    return this.currentUserSubject.value?.role === 'ADMIN';
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
    localStorage.removeItem('auth_user');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  private setSession(authResult: AuthResponse) {
    localStorage.setItem('auth_user', JSON.stringify(authResult));
    this.currentUserSubject.next(authResult);
  }
}
