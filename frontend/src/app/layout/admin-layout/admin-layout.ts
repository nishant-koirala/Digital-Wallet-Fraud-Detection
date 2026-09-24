import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LucideAngularModule, BarChart, ShieldAlert, Settings, ClipboardCheck, Store, FileSignature, Folder, LogOut } from 'lucide-angular';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss'
})
export class AdminLayoutComponent {
  authService = inject(AuthService);
  
  // Lucide Icons
  readonly BarChart = BarChart;
  readonly ShieldAlert = ShieldAlert;
  readonly Settings = Settings;
  readonly ClipboardCheck = ClipboardCheck;
  readonly Store = Store;
  readonly FileSignature = FileSignature;
  readonly Folder = Folder;
  readonly LogOut = LogOut;

  logout() {
    this.authService.logout();
  }
}
