import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MerchantAdminService } from '../../services/merchant-admin.service';
import { MerchantResponse } from '../../services/merchant.service';

@Component({
  selector: 'app-merchant-review',
  imports: [CommonModule],
  templateUrl: './merchant-review.html',
  styleUrl: './merchant-review.scss'
})
export class MerchantReview implements OnInit {
  private merchantAdminService = inject(MerchantAdminService);
  
  pendingMerchants = signal<MerchantResponse[]>([]);
  isLoading = signal(true);

  ngOnInit() {
    this.fetchPendingMerchants();
  }

  fetchPendingMerchants() {
    this.isLoading.set(true);
    this.merchantAdminService.getPendingMerchants().subscribe({
      next: (merchants) => {
        this.pendingMerchants.set(merchants);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to fetch pending merchants', err);
        this.isLoading.set(false);
      }
    });
  }

  approve(merchantId: string) {
    this.merchantAdminService.approveMerchant(merchantId).subscribe({
      next: () => {
        this.fetchPendingMerchants();
      },
      error: (err) => {
        console.error('Approve failed', err);
      }
    });
  }

  reject(merchantId: string) {
    if (!confirm('Are you sure you want to reject this merchant?')) return;
    
    this.merchantAdminService.rejectMerchant(merchantId).subscribe({
      next: () => {
        this.fetchPendingMerchants();
      },
      error: (err) => {
        console.error('Reject failed', err);
      }
    });
  }
}
