import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../services/toast.service';

import { FraudConfigService, FraudConfig } from '../../services/fraud-config.service';

@Component({
  selector: 'app-fraud-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './fraud-settings.html',
  styleUrls: ['./fraud-settings.scss']
})
export class FraudSettingsComponent implements OnInit {
  private fraudConfigService = inject(FraudConfigService);
  private toast = inject(ToastService);
  
  config: FraudConfig = {
    coldStartThreshold: 50000,
    minHistoryForBaseline: 5,
    averageMultiplier: 5,
    maxGeoDistanceKm: 500,
    velocityWindowMinutes: 10,
    velocityLookbackWindows: 6,
    velocityColdStartMax: 5,
    velocityMultiplier: 3.0
  };

  ngOnInit(): void {
    this.fraudConfigService.getConfig().subscribe({
      next: (res) => this.config = res,
      error: (err) => console.error('Failed to load config', err)
    });
  }

  saveConfig() {
    this.fraudConfigService.updateConfig(this.config).subscribe({
      next: () => this.toast.success('Fraud rules updated successfully'),
      error: () => this.toast.error('Failed to update fraud rules')
    });
  }
}
