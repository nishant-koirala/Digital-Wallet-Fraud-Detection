import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const http = inject(HttpClient);

  // Attach withCredentials to all API requests
  const cloned = req.clone({
    withCredentials: true
  });

  return next(cloned).pipe(
    catchError((error: HttpErrorResponse) => {
      // If 401 Unauthorized and not already trying to refresh or login
      if (error.status === 401 && !req.url.includes('/auth/login') && !req.url.includes('/auth/refresh')) {
        return http.post(`${environment.apiUrl}/auth/refresh`, {}, { withCredentials: true }).pipe(
          switchMap(() => {
            // Retry the original request
            return next(cloned);
          }),
          catchError((refreshError) => {
            // Refresh failed, logout
            authService.logout();
            return throwError(() => refreshError);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
