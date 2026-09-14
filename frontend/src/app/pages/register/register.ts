import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: '../login/login.scss'
})
export class Register {
  name = '';
  email = '';
  phone = '';
  password = '';
  loading = false;

  private authService = inject(AuthService);
  private router = inject(Router);

  onSubmit() {
    this.loading = true;
    
    this.authService.register({
      name: this.name,
      email: this.email,
      phone: this.phone,
      password: this.password
    }).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading = false;
      }
    });
  }
}
