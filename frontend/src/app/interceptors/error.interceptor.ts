import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occurred';
      
      if (error.error && typeof error.error === 'object') {
        // Spring Boot ProblemDetail
        if (error.error.detail) {
            errorMessage = error.error.detail;
            
            // Check for validation field errors map
            if (error.error.errors) {
              const fieldErrors = Object.values(error.error.errors).join(', ');
              if (fieldErrors) {
                errorMessage += ': ' + fieldErrors;
              }
            }
        } else if (error.error.message) {
            errorMessage = error.error.message;
        }
      } else if (error.status === 401) {
          errorMessage = 'Session expired. Please log in again.';
          authService.logout();
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      toastService.show(errorMessage, 'error');
      
      return throwError(() => error);
    })
  );
};
