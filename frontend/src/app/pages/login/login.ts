import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  email = '';
  password = '';
  loading = false;

  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  onSubmit() {
    this.loading = true;
    
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.router.navigate(['/']);
        this.toastService.success('Logged in successfully!');
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 428) {
          this.toastService.info('OTP required. Please check your email.');
          this.router.navigate(['/verify-otp'], { 
            state: { action: 'login', authRequest: { email: this.email, password: this.password } } 
          });
        } else {
          this.toastService.error(err.error?.detail || 'Login failed.');
        }
      }
    });
  }
}
