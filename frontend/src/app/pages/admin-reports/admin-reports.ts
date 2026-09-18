import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminReportService } from '../../services/admin-report.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-reports.html',
  styleUrl: './admin-reports.scss'
})
export class AdminReports {
  private reportService = inject(AdminReportService);
  private toastService = inject(ToastService);

  isExportingTx = signal(false);
  isExportingUsers = signal(false);

  exportTransactions() {
    this.isExportingTx.set(true);
    this.reportService.downloadTransactionsCsv().subscribe({
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

  exportUsers() {
    this.isExportingUsers.set(true);
    this.reportService.downloadUsersCsv().subscribe({
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
