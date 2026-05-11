import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer class="bg-[#0f172a] text-white py-12">
      <div class="mx-auto max-w-7xl px-4 md:px-6">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <!-- Column 1 -->
          <div class="flex flex-col gap-2">
            <h3 class="font-display text-xl font-bold">SmartSure</h3>
            <p class="text-sm text-slate-400">Trusted digital insurance workspace</p>
          </div>
          
          <!-- Column 2 -->
          <div class="flex flex-col gap-3">
            <h4 class="font-semibold text-slate-200">Products</h4>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">Health Insurance</span>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">Term Life</span>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">Motor Insurance</span>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">Savings Plans</span>
          </div>

          <!-- Column 3 -->
          <div class="flex flex-col gap-3">
            <h4 class="font-semibold text-slate-200">Support</h4>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">File a Claim</span>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">Renew Policy</span>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">Contact Us</span>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">FAQs</span>
          </div>

          <!-- Column 4 -->
          <div class="flex flex-col gap-3">
            <h4 class="font-semibold text-slate-200">Legal</h4>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">Privacy Policy</span>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">Terms of Use</span>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">IRDAI Disclosure</span>
            <span class="text-sm text-slate-400 hover:text-white transition-colors">Grievance Policy</span>
          </div>
        </div>
        
        <div class="mt-12 pt-8 border-t border-slate-700/50">
          <p class="text-xs text-center text-slate-500">&copy; 2025 SmartSure. All rights reserved.</p>
        </div>
      </div>
    </footer>
  `
})
export class FooterComponent {}
