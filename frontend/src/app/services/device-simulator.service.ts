import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type SimulatorMode = 'NORMAL' | 'HACKER' | 'WORK_LAPTOP';

export interface DeviceProfile {
  mode: SimulatorMode;
  deviceId: string;
  ipAddress: string;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
export class DeviceSimulatorService {
  
  private profiles: Record<SimulatorMode, DeviceProfile> = {
    'NORMAL': {
      mode: 'NORMAL',
      deviceId: 'device-id-normal-87391',
      ipAddress: '192.168.1.100', // Home IP
      description: 'Normal Device (Trusted)'
    },
    'WORK_LAPTOP': {
      mode: 'WORK_LAPTOP',
      deviceId: 'device-id-work-laptop-4512',
      ipAddress: '203.0.113.45', // Work IP
      description: 'Work Laptop (Different IP)'
    },
    'HACKER': {
      mode: 'HACKER',
      deviceId: 'device-id-hacker-UNKNOWN',
      ipAddress: '185.154.21.99', // Unknown/Foreign IP
      description: 'Hacker (Account Takeover)'
    }
  };

  private currentModeSubject = new BehaviorSubject<SimulatorMode>('NORMAL');
  currentMode$ = this.currentModeSubject.asObservable();

  setMode(mode: SimulatorMode) {
    this.currentModeSubject.next(mode);
    console.log(`[Simulator] Switched to ${mode} mode`);
  }

  getCurrentProfile(): DeviceProfile {
    return this.profiles[this.currentModeSubject.value];
  }

  getAllProfiles(): DeviceProfile[] {
    return Object.values(this.profiles);
  }
}
