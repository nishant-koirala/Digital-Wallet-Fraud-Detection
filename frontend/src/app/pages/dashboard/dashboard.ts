import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
  displayBalance = signal(0);
  targetBalance = 12450.00;
  
  searchQuery = signal('');
  filter = signal<'ALL' | 'SENT' | 'RECEIVED'>('ALL');
  
  selectedTransaction = signal<Transaction | null>(null);

  transactions: Transaction[] = [
    { id: 'tx_1', name: 'Ramesh Sharma', meta: '9841234567', amount: -1200.00, time: '2m ago' },
    { id: 'tx_2', name: 'Salary Deposit', meta: 'ACME Corp', amount: 50000.00, time: 'Yesterday' },
    { id: 'tx_3', name: 'Coffee Shop', meta: 'POS Purchase', amount: -250.50, time: '2 days ago' },
  ];

  filteredTransactions = computed(() => {
    let filtered = this.transactions;
    if (this.filter() === 'SENT') filtered = filtered.filter(t => t.amount < 0);
    if (this.filter() === 'RECEIVED') filtered = filtered.filter(t => t.amount > 0);
    
    if (this.searchQuery()) {
      const lowerQ = this.searchQuery().toLowerCase();
      filtered = filtered.filter(t => t.name.toLowerCase().includes(lowerQ) || t.meta.toLowerCase().includes(lowerQ));
    }
    return filtered;
  });

  ngOnInit() {
    this.animateBalance();
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
}
