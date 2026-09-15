import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WalletService } from '../../services/wallet.service';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { TransactionService } from '../../services/transaction.service';
import { ToastService } from '../../services/toast.service';
import { ActivatedRoute, Router } from '@angular/router';

interface Transaction {
  id: string;
  name: string;
  meta: string;
  amount: number;
  time: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
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

  showTransferModal = signal(false);
  transferToPhone = signal('');
  transferAmount = signal<number | null>(null);
  transferOtp = signal('');
  showOtpField = signal(false);
  simulateForeignLocation = signal(false);
  
  simulateForeignLocation = signal(false);
  
  searchQuery = signal('');
  filter = signal<'ALL' | 'SENT' | 'RECEIVED'>('ALL');
  
  selectedTransaction = signal<Transaction | null>(null);
  transactions = signal<Transaction[]>([]);

  filteredTransactions = computed(() => {
    let filtered = this.transactions();
    if (this.filter() === 'SENT') filtered = filtered.filter(t => t.amount < 0);
    if (this.filter() === 'RECEIVED') filtered = filtered.filter(t => t.amount > 0);
    
    if (this.searchQuery()) {
      const lowerQ = this.searchQuery().toLowerCase();
      filtered = filtered.filter(t => t.name.toLowerCase().includes(lowerQ) || t.meta.toLowerCase().includes(lowerQ));
    }
    return filtered;
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
    this.walletService.getTransactions().subscribe({
      next: (res) => {
        const mapped = res.map((t: any) => {
          const isOutgoing = t.fromWallet?.id === this.authService.walletId;
          const displayAmount = isOutgoing ? -t.amount : t.amount;
          
          return {
            id: t.id,
            name: t.toWallet?.id ? 'Transfer' : 'Deposit/System',
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
    const timer = setInterval(() => {
      current += increment;
      if (current >= this.targetBalance) {
        this.displayBalance.set(this.targetBalance);
        clearInterval(timer);
      } else {
        this.displayBalance.set(current);
      }
    }, stepTime);
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
    if (!amount || amount <= 0) return;
    
    this.walletService.deposit(amount).subscribe({
      next: () => {
        this.closeDepositModal();
        this.fetchBalance();
        this.fetchTransactions();
        this.toastService.success(`Successfully deposited Rs. ${amount}`);
      },
      error: (err) => {
        this.toastService.error(err.error?.message || 'Deposit failed.');
      }
    });
  }

  openWithdrawModal() {
    this.showWithdrawModal.set(true);
    this.withdrawAmount.set(null);
  }

  closeWithdrawModal() {
    this.showWithdrawModal.set(false);
  }

  submitWithdraw() {
    const amount = this.withdrawAmount();
    if (!amount || amount <= 0) return;
    
    this.walletService.withdraw(amount).subscribe({
      next: () => {
        this.closeWithdrawModal();
        this.fetchBalance();
        this.fetchTransactions();
        this.toastService.success(`Successfully withdrew Rs. ${amount}`);
      },
      error: (err) => {
        this.toastService.error(err.error?.message || 'Withdraw failed.');
      }
    });
  }

  openTransferModal() {
    this.showTransferModal.set(true);
    this.transferToPhone.set('');
    this.transferAmount.set(null);
    this.transferOtp.set('');
    this.showOtpField.set(false);
  }

  closeTransferModal() {
    this.showTransferModal.set(false);
    this.showOtpField.set(false);
  }

  submitTransfer() {
    const amount = this.transferAmount();
    const toPhone = this.transferToPhone();
    if (!amount || amount <= 0 || !toPhone) return;

    // Simulate location (either real or mocked anomaly)
    let latitude = 27.7172; // Default Kathmandu
    let longitude = 85.3240;

    if (this.simulateForeignLocation()) {
       latitude = 40.7128; // New York
       longitude = -74.0060;
    }

    const otp = this.transferOtp();

    this.transactionService.transfer(toPhone, amount, latitude, longitude, otp).subscribe({
      next: () => {
        this.closeTransferModal();
        this.fetchBalance();
        this.fetchTransactions();
        this.toastService.success('Transfer Successful!');
      },
      error: (err) => {
        if (err.status === 428) {
          // Precondition Required (OTP required)
          this.showOtpField.set(true);
          this.toastService.info('OTP required. Please check your email.');
        } else {
          this.toastService.error(err.error?.message || 'Transfer failed.');
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
