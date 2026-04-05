import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LiveAnnouncerService {
  private static readonly STATUS_TO_RESULT_TRANSITION_MS = 220;
  private readonly currentSubject = new BehaviorSubject<NotificationItem | null>(null);
  private readonly statusLiveSubject = new BehaviorSubject<string>('');
  private readonly successLiveSubject = new BehaviorSubject<string>('');
  private readonly errorLiveSubject = new BehaviorSubject<string>('');
  private readonly queue: NotificationItem[] = [];
  private timerId: ReturnType<typeof setTimeout> | null = null;

  readonly notification$ = this.currentSubject.asObservable();
  readonly statusLiveAnnouncement$ = this.statusLiveSubject.asObservable();
  readonly successLiveAnnouncement$ = this.successLiveSubject.asObservable();
  readonly errorLiveAnnouncement$ = this.errorLiveSubject.asObservable();

  showNotification(notification: NotificationRequest): void {
    const next = this.normalize(notification);
    const current = this.currentSubject.value;

    if (current?.type === 'status' && next.type !== 'status') {
      this.shortenCurrentStatusToOneSecond();
    }

    this.queue.push(next);
    this.processQueue();
  }

  announceSuccess(message: string, options: Partial<NotificationRequest> = {}): void {
    this.showNotification({ ...options, type: 'success', message });
  }

  announceStatus(message: string, options: Partial<NotificationRequest> = {}): void {
    this.showNotification({ ...options, type: 'status', message });
  }

  announceError(message: string, options: Partial<NotificationRequest> = {}): void {
    this.showNotification({ ...options, type: 'error', message });
  }

  clearAll(): void {
    this.queue.length = 0;
    this.clearCurrent();
    this.statusLiveSubject.next('');
    this.successLiveSubject.next('');
    this.errorLiveSubject.next('');
  }

  dismissCurrent(): void {
    this.clearCurrent();
    this.processQueue();
  }

  invokeButtonAction(notification: NotificationItem, button: NotificationButton): void {
    button.action?.();
    if (button.closeOnClick !== false) {
      this.dismissCurrent();
    }
  }

  private processQueue(): void {
    if (this.currentSubject.value || !this.queue.length) {
      return;
    }

    const next = this.queue.shift() || null;
    this.currentSubject.next(next);
    if (next) {
      this.publishLiveAnnouncement(next);
    }

    if (!next || next.duration <= 0) {
      return;
    }

    this.startDismissTimer(next.duration);
  }

  private clearCurrent(): void {
    if (this.timerId) {
      clearTimeout(this.timerId);
      this.timerId = null;
    }
    this.currentSubject.next(null);
  }

  private normalize(notification: NotificationRequest): NotificationItem {
    return {
      title: notification.title || this.defaultTitle(notification.type),
      message: notification.message,
      type: notification.type,
      duration: notification.timeout ?? 5000,
      visible: notification.visible ?? true,
      buttons: notification.buttons || []
    };
  }

  private shortenCurrentStatusToOneSecond(): void {
    if (!this.currentSubject.value || this.currentSubject.value.type !== 'status') {
      return;
    }
    this.startDismissTimer(LiveAnnouncerService.STATUS_TO_RESULT_TRANSITION_MS);
  }

  private startDismissTimer(duration: number): void {
    if (this.timerId) {
      clearTimeout(this.timerId);
      this.timerId = null;
    }

    this.timerId = setTimeout(() => {
      this.timerId = null;
      this.dismissCurrent();
    }, duration);
  }

  private defaultTitle(type: NotificationType): string {
    if (type === 'error') {
      return 'Error';
    }
    if (type === 'success') {
      return 'Success';
    }
    return 'Status';
  }

  private publishLiveAnnouncement(notification: NotificationItem): void {
    const announcement = `${notification.title}: ${notification.message}`;
    const subject = notification.type === 'error'
      ? this.errorLiveSubject
      : notification.type === 'success'
        ? this.successLiveSubject
        : this.statusLiveSubject;

    subject.next('');
    setTimeout(() => subject.next(announcement), 0);
  }
}

export type NotificationType = 'success' | 'status' | 'error';

export interface NotificationButton {
  label: string;
  action?: () => void;
  closeOnClick?: boolean;
}

export interface NotificationRequest {
  type: NotificationType;
  message: string;
  title?: string;
  timeout?: number;
  visible?: boolean;
  buttons?: NotificationButton[];
}

export interface NotificationItem {
  type: NotificationType;
  message: string;
  title: string;
  duration: number;
  visible: boolean;
  buttons: NotificationButton[];
}
