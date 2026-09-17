import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-otp-verify',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './otp-verify.html',
  styleUrl: '../login/login.scss'
})
export class OtpVerify implements OnInit {
  otp = '';
  loading = false;
  
  private authRequest: any = null;
  action: 'login' | 'register' | null = null;
  
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  ngOnInit() {
    const state = history.state;
    if (state && state.action && state.authRequest) {
      this.action = state.action;
      this.authRequest = state.authRequest;
    } else {
      // If accessed directly without state, redirect back to login
      this.router.navigate(['/login']);
    }
  }

  onSubmit() {
    if (!this.otp || !this.authRequest || !this.action) return;
    
    this.loading = true;
    this.authRequest.otp = this.otp;

    const request$ = this.action === 'login' 
      ? this.authService.login(this.authRequest)
      : this.authService.register(this.authRequest);

    request$.subscribe({
      next: () => {
        this.router.navigate(['/']);
        this.toastService.success(`${this.action === 'login' ? 'Logged in' : 'Registered'} successfully!`);
      },
      error: (err) => {
        this.loading = false;
        this.toastService.error(err.error?.detail || 'Verification failed. Please try again.');
      }
    });
  }
}
