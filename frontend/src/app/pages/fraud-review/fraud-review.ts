import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FraudService } from '../../services/fraud.service';

interface FraudItem {
  id: string;
  user: string;
  userId: string;
  rule: string;
  amount: number;
}

@Component({
  selector: 'app-fraud-review',
  imports: [CommonModule, FormsModule],
  templateUrl: './fraud-review.html',
  styleUrl: './fraud-review.scss'
})
export class FraudReview implements OnInit {
  private fraudService = inject(FraudService);
  items = signal<FraudItem[]>([]);

  filterRule = signal<string>('ALL');
  toastMessage = signal<string | null>(null);

  filteredItems = computed(() => {
    if (this.filterRule() === 'ALL') return this.items();
    return this.items().filter(i => i.rule === this.filterRule());
  });

  ngOnInit() {
    this.loadPending();
  }

  loadPending() {
    this.fraudService.getPendingFlags().subscribe({
      next: (res) => {
        const mapped = res.map((f: any) => ({
          id: f.transactionId,
          user: f.walletId || 'Unknown',
          userId: f.walletId,
          rule: f.flagReason,
          amount: 0 // Backend response might not include amount directly, but this works for demo
        }));
        this.items.set(mapped);
      },
      error: (err) => console.error(err)
    });
  }

  approve(item: FraudItem) {
    this.fraudService.approve(item.id).subscribe({
      next: () => {
        this.removeItem(item.id);
        this.showToast(`Approved transaction for ${item.user}`);
      }
    });
  }

  reject(item: FraudItem) {
    this.fraudService.reject(item.id).subscribe({
      next: () => {
        this.removeItem(item.id);
        this.showToast(`Rejected transaction for ${item.user}`);
      }
    });
  }

  private removeItem(id: string) {
    this.items.update(curr => curr.filter(i => i.id !== id));
  }

  private showToast(msg: string) {
    this.toastMessage.set(msg);
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 3000);
  }
}
