import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, UserProfile } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile implements OnInit {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  profile = signal<UserProfile | null>(null);
  isLoading = signal(true);

  // Profile Update Form
  editPhone = signal('');
  isUpdatingProfile = signal(false);

  // Password Update Form
  currentPassword = signal('');
  newPassword = signal('');
  confirmPassword = signal('');
  isUpdatingPassword = signal(false);

  // Account Deletion
  isDeletingAccount = signal(false);

  ngOnInit() {
    this.fetchProfile();
  }

  fetchProfile() {
    this.isLoading.set(true);
    this.userService.getProfile().subscribe({
      next: (res) => {
        this.profile.set(res);
        this.editPhone.set(res.phoneNumber);
        this.isLoading.set(false);
      },
      error: () => {
        this.toastService.error('Failed to load profile');
        this.isLoading.set(false);
      }
    });
  }

  updateProfile() {
    if (!this.editPhone()) return;
    
    this.isUpdatingProfile.set(true);
    this.userService.updateProfile(this.editPhone()).subscribe({
      next: () => {
        this.toastService.success('Profile updated successfully');
        this.isUpdatingProfile.set(false);
        this.fetchProfile();
      },
      error: (err) => {
        this.toastService.error(err.error?.detail || 'Failed to update profile');
        this.isUpdatingProfile.set(false);
      }
    });
  }

  changePassword() {
    if (this.newPassword() !== this.confirmPassword()) {
      this.toastService.error('New passwords do not match');
      return;
    }
    
    this.isUpdatingPassword.set(true);
    this.userService.changePassword(this.currentPassword(), this.newPassword()).subscribe({
      next: () => {
        this.toastService.success('Password changed successfully');
        this.currentPassword.set('');
        this.newPassword.set('');
        this.confirmPassword.set('');
        this.isUpdatingPassword.set(false);
      },
      error: (err) => {
        this.toastService.error(err.error?.detail || 'Failed to change password');
        this.isUpdatingPassword.set(false);
      }
    });
  }

  deleteAccount() {
    if (!confirm('Are you absolutely sure you want to delete your account? This action cannot be undone and will anonymize your data.')) {
      return;
    }
    
    this.isDeletingAccount.set(true);
    this.userService.deleteAccount().subscribe({
      next: () => {
        this.toastService.success('Account deleted successfully');
        this.isDeletingAccount.set(false);
        this.authService.logout();
      },
      error: (err) => {
        this.toastService.error(err.error?.detail || 'Failed to delete account');
        this.isDeletingAccount.set(false);
      }
    });
  }
}
