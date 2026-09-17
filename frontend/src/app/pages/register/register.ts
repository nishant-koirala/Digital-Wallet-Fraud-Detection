import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: '../login/login.scss'
})
export class Register {
  name = '';
  email = '';
  phone = '';
  password = '';
  loading = false;

  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  onSubmit() {
    this.loading = true;
    
    this.authService.register({
      name: this.name,
      email: this.email,
      phone: this.phone,
      password: this.password
    }).subscribe({
      next: () => {
        this.router.navigate(['/']);
        this.toastService.success('Registered successfully!');
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 428) {
          this.toastService.info('OTP required. Please check your email.');
          this.router.navigate(['/verify-otp'], { 
            state: { 
              action: 'register', 
              authRequest: { 
                name: this.name, 
                email: this.email, 
                phone: this.phone, 
                password: this.password 
              } 
            } 
          });
        } else {
          this.toastService.error(err.error?.detail || 'Registration failed.');
        }
      }
    });
  }
}
