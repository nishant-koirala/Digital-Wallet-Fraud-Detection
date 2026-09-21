import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminReportService, ReportTransaction, ReportUser } from '../../services/admin-report.service';
import { ToastService } from '../../services/toast.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reports.html',
  styleUrl: './admin-reports.scss'
})
export class AdminReports implements OnInit {
  private reportService = inject(AdminReportService);
  private toastService = inject(ToastService);

  activeTab = signal<'TRANSACTIONS' | 'USERS'>('TRANSACTIONS');

  // Transactions State
  transactions = signal<ReportTransaction[]>([]);
  txStatusFilter = signal<string>('ALL');
  txPage = signal(0);
  txTotalPages = signal(0);
  isExportingTx = signal(false);

  // Users State
  users = signal<ReportUser[]>([]);
  userRoleFilter = signal<string>('ALL');
  userPage = signal(0);
  userTotalPages = signal(0);
  isExportingUsers = signal(false);

  ngOnInit() {
    this.loadTransactions();
    this.loadUsers();
  }

  setTab(tab: 'TRANSACTIONS' | 'USERS') {
    this.activeTab.set(tab);
  }

  loadTransactions() {
    this.reportService.getTransactions(this.txStatusFilter(), this.txPage(), 10).subscribe({
      next: (data) => {
        this.transactions.set(data.content);
        this.txTotalPages.set(data.totalPages);
      },
      error: () => this.toastService.error('Failed to load transactions')
    });
  }

  onTxFilterChange() {
    this.txPage.set(0);
    this.loadTransactions();
  }

  nextTxPage() {
    if (this.txPage() < this.txTotalPages() - 1) {
      this.txPage.update(p => p + 1);
      this.loadTransactions();
    }
  }

  prevTxPage() {
    if (this.txPage() > 0) {
      this.txPage.update(p => p - 1);
      this.loadTransactions();
    }
  }

  exportTransactions() {
    this.isExportingTx.set(true);
    this.reportService.downloadTransactionsCsv(this.txStatusFilter()).subscribe({
      next: (blob) => {
        this.reportService.saveBlob(blob, 'system_transactions_report.csv');
        this.toastService.success('Transactions report exported successfully!');
        this.isExportingTx.set(false);
      },
      error: () => {
        this.toastService.error('Failed to export transactions report.');
        this.isExportingTx.set(false);
      }
    });
  }

  loadUsers() {
    this.reportService.getUsers(this.userRoleFilter(), this.userPage(), 10).subscribe({
      next: (data) => {
        this.users.set(data.content);
        this.userTotalPages.set(data.totalPages);
      },
      error: () => this.toastService.error('Failed to load users')
    });
  }

  onUserFilterChange() {
    this.userPage.set(0);
    this.loadUsers();
  }

  nextUserPage() {
    if (this.userPage() < this.userTotalPages() - 1) {
      this.userPage.update(p => p + 1);
      this.loadUsers();
    }
  }

  prevUserPage() {
    if (this.userPage() > 0) {
      this.userPage.update(p => p - 1);
      this.loadUsers();
    }
  }

  exportUsers() {
    this.isExportingUsers.set(true);
    this.reportService.downloadUsersCsv(this.userRoleFilter()).subscribe({
      next: (blob) => {
        this.reportService.saveBlob(blob, 'system_users_report.csv');
        this.toastService.success('Users report exported successfully!');
        this.isExportingUsers.set(false);
      },
      error: () => {
        this.toastService.error('Failed to export users report.');
        this.isExportingUsers.set(false);
      }
    });
  }
}
