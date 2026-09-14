import { Component, inject, OnInit, signal, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService, AnalyticsResponse } from '../../services/analytics.service';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-admin-analytics',
  imports: [CommonModule],
  templateUrl: './admin-analytics.html',
  styleUrl: './admin-analytics.scss'
})
export class AdminAnalytics implements OnInit, AfterViewInit {
  private analyticsService = inject(AnalyticsService);
  
  stats = signal<AnalyticsResponse | null>(null);
  isLoading = signal(true);

  @ViewChild('volumeChart') volumeChartRef!: ElementRef;
  @ViewChild('fraudChart') fraudChartRef!: ElementRef;

  private volumeChartInstance: any;
  private fraudChartInstance: any;

  ngOnInit() {
    this.analyticsService.getDashboardStats().subscribe({
      next: (res) => {
        this.stats.set(res);
        this.isLoading.set(false);
        this.renderCharts();
      },
      error: (err) => {
        console.error('Failed to load stats', err);
        this.isLoading.set(false);
      }
    });
  }

  ngAfterViewInit() {
    // If stats are already loaded (unlikely, but possible), render charts
    if (this.stats() && !this.volumeChartInstance) {
      this.renderCharts();
    }
  }

  renderCharts() {
    if (!this.stats() || !this.volumeChartRef || !this.fraudChartRef) return;
    
    // Destroy previous instances if any
    if (this.volumeChartInstance) this.volumeChartInstance.destroy();
    if (this.fraudChartInstance) this.fraudChartInstance.destroy();

    const data = this.stats()!;

    // Volume Chart (Line)
    const labels = Object.keys(data.volumeLast7Days);
    const volumes = Object.values(data.volumeLast7Days);

    this.volumeChartInstance = new Chart(this.volumeChartRef.nativeElement, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: 'Transaction Volume (Rs)',
          data: volumes,
          borderColor: '#4f46e5',
          backgroundColor: 'rgba(79, 70, 229, 0.1)',
          fill: true,
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: false },
          title: { display: true, text: 'Transaction Volume (Last 7 Days)' }
        }
      }
    });

    // Fraud Chart (Doughnut)
    this.fraudChartInstance = new Chart(this.fraudChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Safe', 'Flagged/Rejected'],
        datasets: [{
          data: [data.safeCount, data.flaggedCount],
          backgroundColor: ['#10b981', '#ef4444'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        plugins: {
          title: { display: true, text: 'Transaction Safety' }
        }
      }
    });
  }
}
