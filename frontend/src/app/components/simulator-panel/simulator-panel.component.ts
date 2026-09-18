import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DeviceSimulatorService, SimulatorMode, DeviceProfile } from '../../services/device-simulator.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-simulator-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="simulator-panel" [class.hacker-mode]="currentMode === 'HACKER'">
      <div class="panel-header">
        🛠️ Demo Simulator
      </div>
      <div class="panel-body">
        <label>Simulate Device:</label>
        <select [(ngModel)]="currentMode" (change)="onModeChange()">
          <option *ngFor="let profile of profiles" [value]="profile.mode">
            {{ profile.description }}
          </option>
        </select>
        
        <div class="active-profile">
          <small>IP: {{ currentProfile.ipAddress }}</small><br>
          <small>Device: {{ currentProfile.deviceId | slice:0:18 }}...</small>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .simulator-panel {
      position: fixed;
      bottom: 20px;
      right: 20px;
      background: white;
      border: 1px solid #ddd;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      z-index: 9999;
      width: 250px;
      font-family: var(--font-family, sans-serif);
      transition: all 0.3s ease;
    }

    .hacker-mode {
      border: 2px solid #ff4444;
      box-shadow: 0 4px 12px rgba(255, 68, 68, 0.3);
    }

    .panel-header {
      background: #f8f9fa;
      padding: 10px 15px;
      font-weight: bold;
      border-bottom: 1px solid #ddd;
      border-radius: 8px 8px 0 0;
      font-size: 0.9rem;
    }

    .hacker-mode .panel-header {
      background: #ffeeee;
      color: #cc0000;
    }

    .panel-body {
      padding: 15px;
    }

    label {
      display: block;
      margin-bottom: 8px;
      font-size: 0.85rem;
      color: #555;
    }

    select {
      width: 100%;
      padding: 6px;
      border-radius: 4px;
      border: 1px solid #ccc;
      margin-bottom: 12px;
    }

    .active-profile {
      background: #f1f3f5;
      padding: 8px;
      border-radius: 4px;
      color: #666;
    }
  `]
})
export class SimulatorPanelComponent {
  private simulator = inject(DeviceSimulatorService);
  
  profiles: DeviceProfile[] = this.simulator.getAllProfiles();
  currentMode: SimulatorMode = 'NORMAL';
  
  get currentProfile() {
    return this.simulator.getCurrentProfile();
  }

  ngOnInit() {
    this.simulator.currentMode$.subscribe(mode => {
      this.currentMode = mode;
    });
  }

  onModeChange() {
    this.simulator.setMode(this.currentMode);
  }
}
