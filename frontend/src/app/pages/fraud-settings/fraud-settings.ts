import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../services/toast.service';

interface FraudConfig {
  coldStartThreshold: number;
  minHistoryForBaseline: number;
  averageMultiplier: number;
  maxGeoDistanceKm: number;
}

@Component({
  selector: 'app-fraud-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './fraud-settings.html',
  styleUrls: ['./fraud-settings.scss']
})
export class FraudSettingsComponent implements OnInit {
  private http = inject(HttpClient);
  private toast = inject(ToastService);
  
  config: FraudConfig = {
    coldStartThreshold: 50000,
    minHistoryForBaseline: 5,
    averageMultiplier: 5,
    maxGeoDistanceKm: 500
  };

  ngOnInit(): void {
    this.http.get<FraudConfig>('http://localhost:8080/api/v1/admin/fraud-config').subscribe({
      next: (res) => this.config = res,
      error: (err) => console.error('Failed to load config', err)
    });
  }

  saveConfig() {
    this.http.put('http://localhost:8080/api/v1/admin/fraud-config', this.config).subscribe({
      next: () => this.toast.success('Fraud rules updated successfully'),
      error: () => this.toast.error('Failed to update fraud rules')
    });
  }
}
