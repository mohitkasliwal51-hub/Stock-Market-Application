import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AppDialogService {
  private readonly currentSubject = new BehaviorSubject<AppDialogState | null>(null);
  private readonly queue: Array<{ request: AppDialogState; resolve: (value: AppDialogResult) => void }> = [];
  private activeResolve?: (value: AppDialogResult) => void;

  readonly dialog$ = this.currentSubject.asObservable();

  alert(message: string, options: Partial<AppDialogRequest> = {}): Promise<void> {
    return this.enqueue({
      ...options,
      kind: 'alert',
      title: options.title || 'Notice',
      message,
      confirmLabel: options.confirmLabel || 'OK'
    }).then(() => undefined);
  }

  confirm(message: string, options: Partial<AppDialogRequest> = {}): Promise<boolean> {
    return this.enqueue({
      ...options,
      kind: 'confirm',
      title: options.title || 'Confirm Action',
      message,
      confirmLabel: options.confirmLabel || 'Confirm',
      cancelLabel: options.cancelLabel || 'Cancel'
    }).then(result => result.confirmed);
  }

  prompt(message: string, options: Partial<AppDialogRequest> = {}): Promise<string | null> {
    return this.enqueue({
      ...options,
      kind: 'prompt',
      title: options.title || 'Input Required',
      message,
      confirmLabel: options.confirmLabel || 'Submit',
      cancelLabel: options.cancelLabel || 'Cancel',
      inputValue: options.inputValue || '',
      inputPlaceholder: options.inputPlaceholder || ''
    }).then(result => result.value ?? null);
  }

  close(result: AppDialogResult): void {
    this.currentSubject.next(null);
    this.activeResolve?.(result);
    this.activeResolve = undefined;
    this.processQueue();
  }

  private enqueue(request: AppDialogRequest): Promise<AppDialogResult> {
    return new Promise<AppDialogResult>((resolve) => {
      this.queue.push({ request: this.normalize(request), resolve });
      this.processQueue();
    });
  }

  private processQueue(): void {
    if (this.currentSubject.value || !this.queue.length) {
      return;
    }

    const nextItem = this.queue.shift();
    if (!nextItem) {
      return;
    }

    this.activeResolve = nextItem.resolve;
    this.currentSubject.next(nextItem.request);
  }

  private normalize(request: AppDialogRequest): AppDialogState {
    return {
      ...request,
      duration: request.duration ?? 0
    };
  }
}

export type AppDialogKind = 'alert' | 'confirm' | 'prompt';

export interface AppDialogRequest {
  kind: AppDialogKind;
  title: string;
  message: string;
  confirmLabel: string;
  cancelLabel?: string;
  inputValue?: string;
  inputPlaceholder?: string;
  duration?: number;
}

export interface AppDialogState extends AppDialogRequest {
  duration: number;
}

export interface AppDialogResult {
  confirmed: boolean;
  value?: string;
}
