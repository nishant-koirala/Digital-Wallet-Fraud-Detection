import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WalletService } from '../../services/wallet.service';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { TransactionService } from '../../services/transaction.service';
import { ToastService } from '../../services/toast.service';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ModalComponent } from '../../components/modal/modal.component';
import { LucideAngularModule, Send, Download, Upload, Receipt, QrCode, User, FileText, ShieldCheck } from 'lucide-angular';

interface Transaction {
  id: string;
  name: string;
  meta: string;
  amount: number;
  time: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule, RouterModule, ModalComponent, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  // Lucide Icons
  readonly Send = Send;
  readonly Download = Download;
  readonly Upload = Upload;
  readonly Receipt = Receipt;
  readonly QrCode = QrCode;
  readonly User = User;
  readonly FileText = FileText;
  readonly ShieldCheck = ShieldCheck;
  private authService = inject(AuthService);
  private walletService = inject(WalletService);
  private transactionService = inject(TransactionService);
  private notificationService = inject(NotificationService);
  private toastService = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  displayBalance = signal(0);
  targetBalance = 0;
  
  showDepositModal = signal(false);
  depositAmount = signal<number | null>(null);

  showWithdrawModal = signal(false);
  withdrawAmount = signal<number | null>(null);
  withdrawPin = signal('');

  showTransferModal = signal(false);
  transferToPhone = signal('');
  transferAmount = signal<number | null>(null);
  transferOtp = signal('');
  transferPin = signal('');
  showOtpField = signal(false);
  isSubmitting = signal(false);
  
  selectedTransaction = signal<Transaction | null>(null);
  transactions = signal<Transaction[]>([]);

  filteredTransactions = computed(() => {
    return this.transactions().slice(0, 5);
  });

  ngOnInit() {
    this.fetchBalance();
    this.fetchTransactions();
    this.route.queryParams.subscribe(params => {
      if (params['transferTo']) {
        this.openTransferModal();
        this.transferToPhone.set(params['transferTo']);
      }
    });
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin;
  }

  fetchBalance() {
    this.walletService.getBalance().subscribe({
      next: (res) => {
        this.targetBalance = res.balance;
        this.animateBalance();
      }
    });
  }

  fetchTransactions() {
    this.walletService.getTransactions(0, 10).subscribe({
      next: (res) => {
        const mapped = res.content.map((t: any) => {
          const isOutgoing = t.fromWallet?.id === this.authService.walletId;
          const displayAmount = isOutgoing ? -t.amount : t.amount;
          
          let txName = 'Transfer';
          if (t.fromWallet?.type === 'MINT') {
            txName = 'Deposit';
          } else if (t.toWallet?.type === 'MINT') {
            txName = 'Withdrawal';
          } else if (!t.toWallet?.user) {
            // Fallback in case type isn't serialized but user is null for MINT
            txName = isOutgoing ? 'Withdrawal' : 'Deposit';
          }
          
          return {
            id: t.id,
            name: txName,
            meta: t.status,
            amount: displayAmount,
            time: new Date(t.createdAt).toLocaleDateString()
          };
        });
        this.transactions.set(mapped);
      }
    });
  }

  animateBalance() {
    const duration = 1000;
    const steps = 60;
    const stepTime = duration / steps;
    const increment = this.targetBalance / steps;
    
    let current = 0;
    
    // Clear previous interval if it exists
    if ((this as any).balanceInterval) {
      clearInterval((this as any).balanceInterval);
    }
    
    const timer = setInterval(() => {
      current += increment;
      if (current >= this.targetBalance) {
        this.displayBalance.set(this.targetBalance);
        clearInterval(timer);
      } else {
        this.displayBalance.set(current);
      }
    }, stepTime);
    
    (this as any).balanceInterval = timer;
  }

  openTransaction(t: Transaction) {
    this.selectedTransaction.set(t);
  }
  
  closeTransaction() {
    this.selectedTransaction.set(null);
  }

  openDepositModal() {
    this.showDepositModal.set(true);
    this.depositAmount.set(null);
  }

  closeDepositModal() {
    this.showDepositModal.set(false);
  }

  submitDeposit() {
    const amount = this.depositAmount();
    if (!amount || amount <= 0 || this.isSubmitting()) return;
    
    this.isSubmitting.set(true);
    this.walletService.deposit(amount).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.closeDepositModal();
        this.fetchBalance();
        this.fetchTransactions();
        this.toastService.success(`Successfully deposited Rs. ${amount}`);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.toastService.error(err.error?.detail || 'Deposit failed.');
      }
    });
  }

  openWithdrawModal() {
    this.showWithdrawModal.set(true);
    this.withdrawAmount.set(null);
    this.withdrawPin.set('');
  }

  closeWithdrawModal() {
    this.showWithdrawModal.set(false);
  }

  submitWithdraw() {
    const amount = this.withdrawAmount();
    const pin = this.withdrawPin();
    if (!amount || amount <= 0 || !pin || this.isSubmitting()) return;
    
    this.isSubmitting.set(true);
    this.walletService.withdraw(amount, pin).subscribe({
      next: (res: any) => {
        this.isSubmitting.set(false);
        this.closeWithdrawModal();
        this.fetchBalance();
        this.fetchTransactions();
        if (res?.status === 'FLAGGED' || res?.status === 'PENDING') {
           this.toastService.info('Transaction held for review.');
        } else {
           this.toastService.success(`Successfully withdrew Rs. ${amount}`);
        }
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.toastService.error(err.error?.detail || 'Withdraw failed.');
      }
    });
  }

  openTransferModal() {
    this.showTransferModal.set(true);
    this.transferToPhone.set('');
    this.transferAmount.set(null);
    this.transferOtp.set('');
    this.transferPin.set('');
    this.showOtpField.set(false);
  }

  closeTransferModal() {
    this.showTransferModal.set(false);
    this.showOtpField.set(false);
  }

  submitTransfer() {
    const amount = this.transferAmount();
    const toPhone = this.transferToPhone();
    if (!amount || amount <= 0 || !toPhone || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    // Real location data would be grabbed via navigator.geolocation in a real app
    let latitude = 27.7172; // Default Kathmandu
    let longitude = 85.3240;

    const otp = this.transferOtp();
    const pin = this.transferPin();

    this.transactionService.transfer(toPhone, amount, latitude, longitude, otp, pin).subscribe({
      next: (res: any) => {
        this.isSubmitting.set(false);
        this.closeTransferModal();
        this.fetchBalance();
        this.fetchTransactions();
        
        if (res?.status === 'FLAGGED' || res?.status === 'PENDING') {
           this.toastService.info('Transaction held for review.');
        } else {
           this.toastService.success('Transfer Successful!');
        }
      },
      error: (err) => {
        this.isSubmitting.set(false);
        if (err.status === 428) {
          // Precondition Required (OTP required)
          this.showOtpField.set(true);
          this.toastService.info('OTP required. Please check your email.');
        } else {
          this.toastService.error(err.error?.detail || 'Transfer failed.');
        }
      }
    });
  }

  downloadStatement(format: 'pdf' | 'csv') {
    const request = format === 'pdf' 
      ? this.walletService.downloadStatementPdf()
      : this.walletService.downloadStatementCsv();

    request.subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `statement.${format}`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Failed to download statement', err)
    });
  }
}
