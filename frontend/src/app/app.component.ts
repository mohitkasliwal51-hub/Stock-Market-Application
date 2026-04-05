import { CommonModule } from '@angular/common';
import { AfterViewChecked, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppDialogService } from './services/app-dialog.service';
import { LiveAnnouncerService, NotificationButton, NotificationItem } from './services/live-announcer.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  standalone: true,
  imports: [CommonModule, RouterOutlet]
})
export class AppComponent {
  title = 'Stock Market Application';
  readonly notification$;
  readonly statusLiveAnnouncement$;
  readonly successLiveAnnouncement$;
  readonly errorLiveAnnouncement$;
  readonly dialog$;
  @ViewChild('dialogWindow') dialogWindow?: ElementRef<HTMLElement>;
  private dialogSubscription: Subscription;
  private pendingDialogFocus = false;
  private previousFocusedElement: HTMLElement | null = null;

  constructor(private liveAnnouncer: LiveAnnouncerService, private appDialog: AppDialogService) {
    this.notification$ = this.liveAnnouncer.notification$;
    this.statusLiveAnnouncement$ = this.liveAnnouncer.statusLiveAnnouncement$;
    this.successLiveAnnouncement$ = this.liveAnnouncer.successLiveAnnouncement$;
    this.errorLiveAnnouncement$ = this.liveAnnouncer.errorLiveAnnouncement$;
    this.dialog$ = this.appDialog.dialog$;
    this.dialogSubscription = this.dialog$.subscribe(dialog => {
      if (dialog) {
        this.previousFocusedElement = document.activeElement as HTMLElement | null;
        this.pendingDialogFocus = true;
        return;
      }

      if (this.previousFocusedElement && typeof this.previousFocusedElement.focus === 'function') {
        setTimeout(() => this.previousFocusedElement?.focus(), 0);
      }
      this.previousFocusedElement = null;
    });
  }

  ngAfterViewChecked(): void {
    if (!this.pendingDialogFocus || !this.dialogWindow?.nativeElement) {
      return;
    }

    const root = this.dialogWindow.nativeElement;
    const focusTarget = (root.querySelector('[data-dialog-focus]') as HTMLElement | null)
      || (root.querySelector('input, button, [tabindex]:not([tabindex="-1"])') as HTMLElement | null)
      || root;

    focusTarget.focus();
    this.pendingDialogFocus = false;
  }

  ngOnDestroy(): void {
    this.dialogSubscription.unsubscribe();
  }

  closeNotification(): void {
    this.liveAnnouncer.dismissCurrent();
  }

  triggerButton(button: NotificationButton, notification: NotificationItem): void {
    this.liveAnnouncer.invokeButtonAction(notification, button);
  }

  closeDialog(confirmed: boolean): void {
    this.appDialog.close({ confirmed });
  }

  submitPrompt(input: HTMLInputElement): void {
    this.appDialog.close({ confirmed: true, value: input.value });
  }
}
