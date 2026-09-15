import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../../services/toast.service';

interface KycDocument {
  id: string;
  userId: string;
  documentType: string;
  documentNumber: string;
  frontImageUrl: string;
  backImageUrl: string;
  status: string;
  createdAt: string;
}

@Component({
  selector: 'app-kyc-review',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './kyc-review.html',
  styleUrls: ['./kyc-review.scss']
})
export class KycReviewComponent implements OnInit {
  private http = inject(HttpClient);
  private toast = inject(ToastService);

  documents: KycDocument[] = [];

  ngOnInit() {
    this.fetchPending();
  }

  fetchPending() {
    this.http.get<KycDocument[]>('http://localhost:8080/api/v1/admin/kyc/pending').subscribe({
      next: (res) => this.documents = res,
      error: () => this.toast.error('Failed to load pending KYC applications')
    });
  }

  approve(id: string) {
    this.http.post(`http://localhost:8080/api/v1/admin/kyc/${id}/approve`, {}).subscribe({
      next: () => {
        this.toast.success('KYC Approved');
        this.fetchPending();
      },
      error: () => this.toast.error('Failed to approve KYC')
    });
  }

  reject(id: string) {
    this.http.post(`http://localhost:8080/api/v1/admin/kyc/${id}/reject`, {}).subscribe({
      next: () => {
        this.toast.success('KYC Rejected');
        this.fetchPending();
      },
      error: () => this.toast.error('Failed to reject KYC')
    });
  }
}
