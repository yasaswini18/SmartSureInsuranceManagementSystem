import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="mx-auto flex min-h-[60vh] max-w-3xl items-center justify-center px-6">
      <div class="card w-full p-10 text-center">
        <h1 class="font-display text-4xl font-bold text-slate-950">Unauthorized</h1>
        <p class="mt-3 text-sm leading-7 text-slate-600">Your account does not have permission to open this page.</p>
        <a routerLink="/" class="btn-primary mx-auto mt-6 inline-flex">Back to Home</a>
      </div>
    </div>
  `,
})
export class UnauthorizedComponent {}
