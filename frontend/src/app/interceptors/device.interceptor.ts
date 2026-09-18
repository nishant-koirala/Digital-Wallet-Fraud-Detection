import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { DeviceSimulatorService } from '../services/device-simulator.service';

export const deviceInterceptor: HttpInterceptorFn = (req, next) => {
  const deviceSimulator = inject(DeviceSimulatorService);
  const profile = deviceSimulator.getCurrentProfile();

  const modifiedReq = req.clone({
    setHeaders: {
      'X-Device-Id': profile.deviceId,
      'X-Forwarded-For': profile.ipAddress
    }
  });

  return next(modifiedReq);
};
