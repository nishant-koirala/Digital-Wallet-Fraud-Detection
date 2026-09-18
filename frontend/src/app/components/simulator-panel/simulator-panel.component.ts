import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DeviceSimulatorService, SimulatorMode, DeviceProfile } from '../../services/device-simulator.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-simulator-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <!-- Floating Action Button -->
    <button class="simulator-fab" (click)="toggleExpanded()" [class.hacker-mode]="currentMode === 'HACKER'" *ngIf="!isExpanded">
      🛠️ Demo
    </button>

    <!-- Expanded Panel -->
    <div class="simulator-panel" [class.hacker-mode]="currentMode === 'HACKER'" *ngIf="isExpanded">
      <div class="panel-header">
        🛠️ Demo Simulator
        <button class="close-btn" (click)="toggleExpanded()">✕</button>
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
    .simulator-fab {
      position: fixed;
      bottom: 20px;
      right: 20px;
      background: white;
      border: 1px solid #ddd;
      border-radius: 24px;
      padding: 10px 16px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      z-index: 9999;
      cursor: pointer;
      font-weight: 600;
      color: #333;
      transition: all 0.2s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0,0,0,0.2);
      }
      
      &.hacker-mode {
        border-color: #ff4444;
        color: #cc0000;
      }
    }

    .simulator-panel {
      position: fixed;
      bottom: 20px;
      right: 20px;
      background: white;
      border: 1px solid #ddd;
      border-radius: 8px;
      box-shadow: 0 8px 24px rgba(0,0,0,0.2);
      z-index: 9999;
      width: 250px;
      font-family: var(--font-family, sans-serif);
      animation: popIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    }

    @keyframes popIn {
      0% { transform: scale(0.9) translateY(20px); opacity: 0; }
      100% { transform: scale(1) translateY(0); opacity: 1; }
    }

    .hacker-mode {
      border: 2px solid #ff4444;
      box-shadow: 0 8px 24px rgba(255, 68, 68, 0.3);
    }

    .panel-header {
      background: #f8f9fa;
      padding: 10px 15px;
      font-weight: bold;
      border-bottom: 1px solid #ddd;
      border-radius: 8px 8px 0 0;
      font-size: 0.9rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .hacker-mode .panel-header {
      background: #ffeeee;
      color: #cc0000;
      border-bottom-color: #ffcccc;
    }

    .close-btn {
      background: transparent;
      border: none;
      cursor: pointer;
      font-size: 1.2rem;
      line-height: 1;
      color: #888;
      
      &:hover { color: #333; }
    }

    .hacker-mode .close-btn {
      color: #cc0000;
      &:hover { color: #990000; }
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
  isExpanded = false;
  
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

  toggleExpanded() {
    this.isExpanded = !this.isExpanded;
  }
}
