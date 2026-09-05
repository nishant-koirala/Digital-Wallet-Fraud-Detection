import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
export class FraudReview {
  items = signal<FraudItem[]>([
    { id: 'f_1', user: 'Ramesh Sharma', userId: 'id_user9921', rule: 'Velocity (High)', amount: 45000.00 },
    { id: 'f_2', user: 'Acme Corp', userId: 'id_merchant11', rule: 'Amount Threshold', amount: 900000.00 },
    { id: 'f_3', user: 'John Doe', userId: 'id_user443', rule: 'Velocity (High)', amount: 2000.00 },
  ]);

  filterRule = signal<string>('ALL');
  
  toastMessage = signal<string | null>(null);

  filteredItems = computed(() => {
    if (this.filterRule() === 'ALL') return this.items();
    return this.items().filter(i => i.rule === this.filterRule());
  });

  approve(item: FraudItem) {
    this.removeItem(item.id);
    this.showToast(`Approved transaction for ${item.user}`);
  }

  reject(item: FraudItem) {
    this.removeItem(item.id);
    this.showToast(`Rejected transaction for ${item.user}`);
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
