import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  message: string;
  type: 'success' | 'error';
  id: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  messages = signal<ToastMessage[]>([]);
  private idCounter = 0;

  show(message: string, type: 'success' | 'error' = 'error', durationMs = 4000) {
    const id = this.idCounter++;
    this.messages.update(msgs => [...msgs, { message, type, id }]);
    
    setTimeout(() => {
      this.remove(id);
    }, durationMs);
  }

  remove(id: number) {
    this.messages.update(msgs => msgs.filter(m => m.id !== id));
  }
}
