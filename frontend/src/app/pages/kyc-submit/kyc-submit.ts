import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../services/toast.service';

interface KycStatusResponse {
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'NOT_SUBMITTED';
  documentType?: string;
}

@Component({
  selector: 'app-kyc-submit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './kyc-submit.html',
  styleUrls: ['./kyc-submit.scss']
})
export class KycSubmitComponent implements OnInit {
  private http = inject(HttpClient);
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
    this.http.get<KycStatusResponse>('http://localhost:8080/api/v1/kyc').subscribe({
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
    this.http.post('http://localhost:8080/api/v1/kyc', this.formData).subscribe({
      next: () => {
        this.toast.success('KYC submitted successfully!');
        this.status = 'PENDING';
      },
      error: () => this.toast.error('Failed to submit KYC.')
    });
  }
}
