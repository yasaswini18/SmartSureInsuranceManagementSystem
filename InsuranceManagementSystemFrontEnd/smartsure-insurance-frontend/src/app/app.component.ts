import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { InactivityService } from './services/inactivity.service';
import { FooterComponent } from './components/shared/footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, FooterComponent],
  template: `
    <div class="flex flex-col min-h-screen">
      <div class="flex-grow flex flex-col">
        <router-outlet></router-outlet>
      </div>
      <app-footer></app-footer>
    </div>
  `,
})
export class AppComponent {
  constructor(private inactivityService: InactivityService) {
    this.inactivityService.startWatching();
  }
}
