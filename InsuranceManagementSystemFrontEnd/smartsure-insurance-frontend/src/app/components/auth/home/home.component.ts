import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
})
export class HomeComponent {
  stats = [
    { value: '52K+', label: 'Policies managed' },
    { value: '94%', label: 'Claims processed on time' },
    { value: '180+', label: 'Cities served' },
    { value: '24/7', label: 'Advisor support' },
  ];

  plans = [
    {
      tag: 'Health',
      name: 'Health Shield',
      desc: 'Family floater plans with cashless hospital network, annual checkups, and smooth renewal journeys.',
      accent: 'bg-emerald-100 text-emerald-700',
      price: 'From Rs 499/month',
    },
    {
      tag: 'Term',
      name: 'Life Secure',
      desc: 'High-cover term plans with nominee-first benefits, rider add-ons, and faster underwriting support.',
      accent: 'bg-sky-100 text-sky-700',
      price: 'From Rs 329/month',
    },
    {
      tag: 'Motor',
      name: 'Drive Protect',
      desc: 'Private car and bike cover with instant IDV visibility, add-ons, and claims tracking in one place.',
      accent: 'bg-amber-100 text-amber-700',
      price: 'From Rs 219/month',
    },
    {
      tag: 'Savings',
      name: 'Future Income',
      desc: 'Long-term wealth and retirement plans built for predictable maturity value and disciplined savings.',
      accent: 'bg-violet-100 text-violet-700',
      price: 'From Rs 799/month',
    },
  ];

  tools = [
    {
      title: 'Plan comparison',
      desc: 'View coverage, premium, duration, and status side by side before you buy.',
      accent: 'border-sky-200 bg-sky-50',
    },
    {
      title: 'Claim tracker',
      desc: 'Track every submitted claim from first notice through approval and settlement.',
      accent: 'border-emerald-200 bg-emerald-50',
    },
    {
      title: 'Renewal desk',
      desc: 'Keep policies active with reminders, customer support notes, and status checks.',
      accent: 'border-amber-200 bg-amber-50',
    },
  ];

  steps = [
    {
      num: '01',
      title: 'Explore products',
      desc: 'Browse health, life, motor, and savings categories with benefit-led summaries.',
    },
    {
      num: '02',
      title: 'Apply in minutes',
      desc: 'Register, select coverage, and complete policy purchase from one dashboard.',
    },
    {
      num: '03',
      title: 'Manage confidently',
      desc: 'Monitor claims, renewals, policy status, and support history without switching portals.',
    },
  ];

  trustPoints = [
    'Policy comparison inspired by leading Indian insurance aggregators',
    'White-first, bank-grade customer portal experience',
    'Separate admin and customer journeys with claim visibility',
  ];
}
