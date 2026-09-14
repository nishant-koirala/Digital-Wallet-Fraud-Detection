import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { QRCodeComponent } from 'angularx-qrcode';
import jsQR from 'jsqr';

@Component({
  imports: [CommonModule, QRCodeComponent],
  selector: 'app-qr-screen',
  styleUrl: './qr-screen.scss',
  templateUrl: './qr-screen.html',
})
export class QrScreen implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  myWalletId = signal<string>('');
  errorMsg = signal<string>('');

  ngOnInit() {
    this.authService.currentUser$.subscribe({
      next: (user: any) => {
        if (user && user.walletId) {
          this.myWalletId.set(user.walletId);
        }
      }
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const image = new Image();
        image.onload = () => {
          const canvas = document.createElement('canvas');
          canvas.width = image.width;
          canvas.height = image.height;
          const context = canvas.getContext('2d');
          if (context) {
            context.drawImage(image, 0, 0, image.width, image.height);
            const imageData = context.getImageData(0, 0, image.width, image.height);
            // Ignore type checking for jsQR to ensure compatibility whether it's default or namespace
            const code = (jsQR as any)(imageData.data, imageData.width, imageData.height);
            
            if (code && code.data) {
              this.errorMsg.set('');
              // Success! Navigate to dashboard with transferTo parameter
              this.router.navigate(['/'], { queryParams: { transferTo: code.data } });
            } else {
              this.errorMsg.set('No QR code found in the image. Please try again.');
            }
          }
        };
        image.src = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }
}
