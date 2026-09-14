import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let toast of toastService.messages()" 
           class="toast" 
           [ngClass]="toast.type"
           (click)="toastService.remove(toast.id)">
        <span class="icon">{{ toast.type === 'error' ? '!' : '✓' }}</span>
        <span class="message">{{ toast.message }}</span>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 24px;
      right: 24px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    
    .toast {
      display: flex;
      align-items: center;
      gap: 12px;
      min-width: 280px;
      max-width: 400px;
      padding: 16px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 10px 40px rgba(0,0,0,0.12);
      cursor: pointer;
      animation: slideIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      transition: all 0.2s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 12px 45px rgba(0,0,0,0.15);
      }
    }
    
    .toast.error {
      border-left: 4px solid #e31837;
      
      .icon {
        background: rgba(227, 24, 55, 0.1);
        color: #e31837;
      }
    }
    
    .toast.success {
      border-left: 4px solid #10b981;
      
      .icon {
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
      }
    }
    
    .icon {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      font-weight: bold;
      flex-shrink: 0;
    }
    
    .message {
      font-size: 14px;
      font-weight: 500;
      color: #1f2937;
      line-height: 1.4;
    }
    
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `]
})
export class ToastComponent {
  toastService = inject(ToastService);
}
