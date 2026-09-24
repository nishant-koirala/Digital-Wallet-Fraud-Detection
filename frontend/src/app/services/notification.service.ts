import { environment } from '../../environments/environment';
import { Injectable, inject } from '@angular/core';
import { Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { AuthService } from './auth.service';

export interface NotificationMessage {
  message: string;
  transactionId: string;
  amount: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private client: Client;
  private authService = inject(AuthService);
  
  public notifications = new Subject<NotificationMessage>();

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      onConnect: () => {
        const walletId = this.authService.walletId;
        if (walletId) {
          this.client.subscribe(`/topic/notifications/${walletId}`, (message) => {
            if (message.body) {
              this.notifications.next(JSON.parse(message.body));
            }
          });
        }
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      }
    });
  }

  connect() {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  disconnect() {
    if (this.client.active) {
      this.client.deactivate();
    }
  }
}

