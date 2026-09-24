import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transaction.service';
import { Router } from '@angular/router';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-pay-bills',
  imports: [CommonModule, FormsModule],
  templateUrl: './pay-bills.html',
  styleUrl: './pay-bills.scss'
})
export class PayBills {
  private transactionService = inject(TransactionService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  billerId = signal<string>('electricity');
  customerId = signal<string>('');
  amount = signal<number | null>(null);
  pin = signal<string>('');

  isSubmitting = signal(false);

  submitPayment() {
    if (!this.billerId() || !this.customerId() || !this.amount() || !this.pin()) return;
    
    this.isSubmitting.set(true);
    
    this.transactionService.payBill(this.billerId(), this.customerId(), this.amount()!, this.pin()).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toastService.show('Bill paid successfully!', 'success');
        this.router.navigate(['/']);
      },
      error: (err: any) => {
        this.isSubmitting.set(false);
        console.error('Bill payment failed', err);
        this.toastService.error(err.error?.detail || 'Bill payment failed. Please try again.');
      }
    });
  }
}
