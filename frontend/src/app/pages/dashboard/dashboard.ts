import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WalletService } from '../../services/wallet.service';
import { AuthService } from '../../services/auth.service';
import { TransactionService } from '../../services/transaction.service';
import { ActivatedRoute } from '@angular/router';

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
  private route = inject(ActivatedRoute);
  displayBalance = signal(0);
  targetBalance = 0;
  
  showDepositModal = signal(false);
  depositAmount = signal<number | null>(null);

  showWithdrawModal = signal(false);
  withdrawAmount = signal<number | null>(null);

  showTransferModal = signal(false);
  transferToWalletId = signal('');
  transferAmount = signal<number | null>(null);
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
        this.transferToWalletId.set(params['transferTo']);
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
        const mapped = res.map((t: any) => ({
          id: t.id,
          name: t.toWallet?.id ? 'Transfer' : 'Deposit/System',
          meta: t.status,
          amount: t.amount,
          time: new Date(t.createdAt).toLocaleDateString()
        }));
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
      },
      error: (err) => {
        console.error('Deposit failed', err);
        alert('Deposit failed. Please try again.');
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
      },
      error: (err) => {
        console.error('Withdraw failed', err);
        alert('Withdraw failed. Please check your balance and try again.');
      }
    });
  }

  openTransferModal() {
    this.showTransferModal.set(true);
    this.transferToWalletId.set('');
    this.transferAmount.set(null);
  }

  closeTransferModal() {
    this.showTransferModal.set(false);
  }

  submitTransfer() {
    const amount = this.transferAmount();
    const toWalletId = this.transferToWalletId();
    if (!amount || amount <= 0 || !toWalletId) return;

    // Simulate location (either real or mocked anomaly)
    let latitude = 27.7172; // Default Kathmandu
    let longitude = 85.3240;

    if (this.simulateForeignLocation()) {
       latitude = 40.7128; // New York
       longitude = -74.0060;
    }

    this.transactionService.transfer(toWalletId, amount, latitude, longitude).subscribe({
      next: () => {
        this.closeTransferModal();
        this.fetchBalance();
        this.fetchTransactions();
      },
      error: (err: any) => {
        console.error('Transfer failed', err);
        alert('Transfer failed. Please check the wallet ID and your balance.');
      }
    });
  }
}
