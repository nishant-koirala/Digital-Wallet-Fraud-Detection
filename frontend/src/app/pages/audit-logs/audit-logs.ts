import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditService, AuditLog } from '../../services/audit.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-logs.html',
  styleUrl: './audit-logs.scss'
})
export class AuditLogs implements OnInit {
  private auditService = inject(AuditService);
  private toastService = inject(ToastService);

  logs = signal<AuditLog[]>([]);
  totalElements = signal(0);
  totalPages = signal(0);
  
  currentPage = signal(0);
  pageSize = signal(10);
  searchQuery = signal('');
  
  isLoading = signal(true);

  ngOnInit() {
    this.fetchLogs();
  }

  fetchLogs() {
    this.isLoading.set(true);
    this.auditService.getLogs(this.currentPage(), this.pageSize(), this.searchQuery()).subscribe({
      next: (page) => {
        this.logs.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.toastService.error('Failed to load audit logs');
        this.isLoading.set(false);
      }
    });
  }

  onSearch() {
    this.currentPage.set(0);
    this.fetchLogs();
  }

  nextPage() {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update(p => p + 1);
      this.fetchLogs();
    }
  }

  prevPage() {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.fetchLogs();
    }
  }
}
