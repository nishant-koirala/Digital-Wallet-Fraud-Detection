import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info';

export interface Toast {
  id: string;
  message: string;
  type: ToastType;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  messages = signal<Toast[]>([]);

  show(message: string, type: ToastType = 'info') {
    const toast: Toast = { id: crypto.randomUUID(), message, type };
    this.messages.update(t => [...t, toast]);
    setTimeout(() => {
      this.remove(toast.id);
    }, 3000);
  }

  success(message: string) {
    this.show(message, 'success');
  }

  error(message: string) {
    this.show(message, 'error');
  }

  info(message: string) {
    this.show(message, 'info');
  }

  remove(id: string) {
    this.messages.update(t => t.filter(x => x.id !== id));
  }
}
