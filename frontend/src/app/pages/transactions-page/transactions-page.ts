import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WalletService } from '../../services/wallet.service';
import { AuthService } from '../../services/auth.service';

interface Transaction {
  id: string;
  name: string;
  meta: string;
  amount: number;
  time: string;
}

@Component({
  selector: 'app-transactions-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions-page.html',
  styleUrl: './transactions-page.scss'
})
export class TransactionsPage implements OnInit {
  private authService = inject(AuthService);
  private walletService = inject(WalletService);

  searchQuery = signal('');
  filter = signal<'ALL' | 'SENT' | 'RECEIVED'>('ALL');
  
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;
  
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
    this.fetchTransactions();
  }

  nextPage() {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update(p => p + 1);
      this.fetchTransactions();
    }
  }

  prevPage() {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.fetchTransactions();
    }
  }

  fetchTransactions() {
    this.walletService.getTransactions(this.currentPage(), this.pageSize).subscribe({
      next: (res: any) => {
        this.totalPages.set(res.totalPages || 0);
        this.totalElements.set(res.totalElements || 0);
        const mapped = res.content.map((t: any) => {
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

  openTransaction(t: Transaction) {
    this.selectedTransaction.set(t);
  }
  
  closeTransaction() {
    this.selectedTransaction.set(null);
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
