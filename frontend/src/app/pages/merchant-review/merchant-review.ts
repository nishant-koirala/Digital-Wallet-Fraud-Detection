import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MerchantAdminService } from '../../services/merchant-admin.service';
import { MerchantResponse } from '../../services/merchant.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-merchant-review',
  imports: [CommonModule],
  templateUrl: './merchant-review.html',
  styleUrl: './merchant-review.scss'
})
export class MerchantReview implements OnInit {
  private merchantAdminService = inject(MerchantAdminService);
  private toastService = inject(ToastService);
  
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
        this.toastService.error('Failed to fetch pending merchants');
        this.isLoading.set(false);
      }
    });
  }

  approve(merchantId: string) {
    this.merchantAdminService.approveMerchant(merchantId).subscribe({
      next: () => {
        this.toastService.success('Merchant approved successfully');
        this.fetchPendingMerchants();
      },
      error: (err) => {
        this.toastService.error(err.error?.detail || 'Approve failed');
      }
    });
  }

  reject(merchantId: string) {
    if (!confirm('Are you sure you want to reject this merchant?')) return;
    
    this.merchantAdminService.rejectMerchant(merchantId).subscribe({
      next: () => {
        this.toastService.info('Merchant rejected');
        this.fetchPendingMerchants();
      },
      error: (err) => {
        this.toastService.error(err.error?.detail || 'Reject failed');
      }
    });
  }
}
