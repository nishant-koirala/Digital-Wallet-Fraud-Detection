import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';
import { KycService, KycDocument } from '../../services/kyc.service';

@Component({
  selector: 'app-kyc-review',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './kyc-review.html',
  styleUrls: ['./kyc-review.scss']
})
export class KycReviewComponent implements OnInit {
  private kycService = inject(KycService);
  private toast = inject(ToastService);

  documents: KycDocument[] = [];

  ngOnInit() {
    this.fetchPending();
  }

  fetchPending() {
    this.kycService.getPending().subscribe({
      next: (res) => this.documents = res,
      error: () => this.toast.error('Failed to load pending KYC applications')
    });
  }

  approve(id: string) {
    this.kycService.approve(id).subscribe({
      next: () => {
        this.toast.success('KYC Approved');
        this.fetchPending();
      },
      error: () => this.toast.error('Failed to approve KYC')
    });
  }

  reject(id: string) {
    this.kycService.reject(id).subscribe({
      next: () => {
        this.toast.success('KYC Rejected');
        this.fetchPending();
      },
      error: () => this.toast.error('Failed to reject KYC')
    });
  }
}
