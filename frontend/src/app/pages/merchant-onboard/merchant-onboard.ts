import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MerchantService, MerchantCreateRequest } from '../../services/merchant.service';
import { AuthService } from '../../services/auth.service';

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
    private router: Router
  ) {}

  onSubmit() {
    this.loading = true;
    this.error = '';
    
    this.merchantService.onboardMerchant(this.request).subscribe({
      next: () => {
        // We need to refresh the user profile to get the new role and wallet
        // But since we don't have a profile endpoint, we can just logout and ask them to login again
        // Or we could just navigate to dashboard and show a success message
        alert('Merchant account created successfully! Please log in again to access merchant features.');
        this.authService.logout();
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to onboard as merchant';
        this.loading = false;
      }
    });
  }
}
