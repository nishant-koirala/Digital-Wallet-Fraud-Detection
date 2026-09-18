import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminReportService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/v1/admin/reports';

  downloadTransactionsCsv(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/transactions/csv`, {
      responseType: 'blob'
    });
  }

  downloadUsersCsv(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/users/csv`, {
      responseType: 'blob'
    });
  }

  saveBlob(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}
