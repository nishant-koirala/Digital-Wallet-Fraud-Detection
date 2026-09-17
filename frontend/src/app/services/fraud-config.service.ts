import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface FraudConfig {
  coldStartThreshold: number;
  minHistoryForBaseline: number;
  averageMultiplier: number;
  maxGeoDistanceKm: number;
  velocityWindowMinutes: number;
  velocityLookbackWindows: number;
  velocityColdStartMax: number;
  velocityMultiplier: number;
}

@Injectable({
  providedIn: 'root'
})
export class FraudConfigService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1/admin/fraud-config';

  getConfig(): Observable<FraudConfig> {
    return this.http.get<FraudConfig>(this.baseUrl);
  }

  updateConfig(config: FraudConfig): Observable<FraudConfig> {
    return this.http.put<FraudConfig>(this.baseUrl, config);
  }
}
