import { Injectable, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, Subscription, fromEvent, merge, timer } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class InactivityService {
  private readonly inactivityTimeoutMs = 15 * 60 * 1000;
  private activity$ = new Subject<void>();
  private activitySubscription?: Subscription;
  private timeoutSubscription?: Subscription;
  private started = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService,
    private ngZone: NgZone
  ) {}

  startWatching(): void {
    if (this.started) {
      return;
    }

    this.started = true;

    this.ngZone.runOutsideAngular(() => {
      this.activitySubscription = merge(
        fromEvent(document, 'mousemove'),
        fromEvent(document, 'mousedown'),
        fromEvent(document, 'keydown'),
        fromEvent(document, 'scroll'),
        fromEvent(document, 'touchstart')
      ).pipe(
        debounceTime(300)
      ).subscribe(() => this.activity$.next());
    });

    this.timeoutSubscription = this.activity$.pipe(
      switchMap(() => timer(this.inactivityTimeoutMs))
    ).subscribe(() => {
      if (this.authService.isLoggedIn()) {
        this.ngZone.run(() => {
          this.toastr.info('You were logged out due to inactivity.');
          this.authService.logout();
        });
      }
    });

    this.activity$.next();
  }

  resetTimer(): void {
    this.activity$.next();
  }

  stopWatching(): void {
    this.activitySubscription?.unsubscribe();
    this.timeoutSubscription?.unsubscribe();
    this.started = false;
  }
}
