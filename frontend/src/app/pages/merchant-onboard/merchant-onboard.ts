import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MerchantService, MerchantCreateRequest } from '../../services/merchant.service';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-merchant-onboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './merchant-onboard.html',
  styleUrls: ['./merchant-onboard.scss']
})
export class MerchantOnboard {
  request: MerchantCreateRequest = {
    businessName: '',
    category: '',
    settlementAccount: ''
  };

  loading = false;
  error = '';

  constructor(
    private merchantService: MerchantService,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {}

  onSubmit() {
    this.loading = true;
    this.error = '';
    
    this.merchantService.onboardMerchant(this.request).subscribe({
      next: () => {
        this.loading = false;
        this.toastService.show('Merchant account created successfully! Please log in again to access merchant features.', 'success');
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to onboard as merchant';
        this.loading = false;
      }
    });
  }
}
