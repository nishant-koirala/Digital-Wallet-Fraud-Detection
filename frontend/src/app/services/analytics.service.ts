import { environment } from '../../environments/environment';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AnalyticsResponse {
  totalVolume: number;
  safeCount: number;
  flaggedCount: number;
  totalUsers: number;
  volumeLast7Days: { [key: string]: number };
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl + '/admin/analytics';

  getDashboardStats(): Observable<AnalyticsResponse> {
    return this.http.get<AnalyticsResponse>(this.baseUrl);
  }
}

