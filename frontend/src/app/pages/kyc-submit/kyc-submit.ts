import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../services/toast.service';
import { KycService, KycStatusResponse } from '../../services/kyc.service';

@Component({
  selector: 'app-kyc-submit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './kyc-submit.html',
  styleUrls: ['./kyc-submit.scss']
})
export class KycSubmitComponent implements OnInit {
  private kycService = inject(KycService);
  private toast = inject(ToastService);

  status: KycStatusResponse['status'] = 'NOT_SUBMITTED';
  isLoading = true;

  formData = {
    documentType: 'PASSPORT',
    documentNumber: '',
    frontImageUrl: 'https://example.com/mock-id-front.jpg',
    backImageUrl: 'https://example.com/mock-id-back.jpg'
  };

  ngOnInit() {
    this.kycService.getStatus().subscribe({
      next: (res) => {
        this.status = res.status;
        this.isLoading = false;
      },
      error: (err) => {
        if (err.status === 404) {
          this.status = 'NOT_SUBMITTED';
        } else {
          this.toast.error('Failed to load KYC status');
        }
        this.isLoading = false;
      }
    });
  }

  submitKyc() {
    this.kycService.submitKyc(this.formData).subscribe({
      next: () => {
        this.toast.success('KYC submitted successfully!');
        this.status = 'PENDING';
      },
      error: () => this.toast.error('Failed to submit KYC.')
    });
  }
}
