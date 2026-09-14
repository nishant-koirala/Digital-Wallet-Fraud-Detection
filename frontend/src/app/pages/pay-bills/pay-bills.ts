import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transaction.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pay-bills',
  imports: [CommonModule, FormsModule],
  templateUrl: './pay-bills.html',
  styleUrl: './pay-bills.scss'
})
export class PayBills {
  private transactionService = inject(TransactionService);
  private router = inject(Router);

  billerId = signal<string>('electricity');
  customerId = signal<string>('');
  amount = signal<number | null>(null);

  isSubmitting = signal(false);

  submitPayment() {
    if (!this.billerId() || !this.customerId() || !this.amount()) return;
    
    this.isSubmitting.set(true);
    
    this.transactionService.payBill(this.billerId(), this.customerId(), this.amount()!).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        alert('Bill paid successfully!');
        this.router.navigate(['/']);
      },
      error: (err: any) => {
        this.isSubmitting.set(false);
        console.error('Bill payment failed', err);
        alert('Bill payment failed. Please try again.');
      }
    });
  }
}
