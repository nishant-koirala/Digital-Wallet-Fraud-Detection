import { Injectable, inject } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ToastService } from './toast.service';

@Injectable({
  providedIn: 'root'
})
export class WsNotificationService {
  private stompClient: Client | null = null;
  private toastService = inject(ToastService);

  public connect(token: string, walletId: string) {
    if (this.stompClient && this.stompClient.active) {
      return;
    }

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('Connected to WebSocket');
        this.stompClient?.subscribe(`/topic/notifications/${walletId}`, (message) => {
          this.toastService.info(message.body);
        });
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      }
    });

    this.stompClient.activate();
  }

  public disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }
}
