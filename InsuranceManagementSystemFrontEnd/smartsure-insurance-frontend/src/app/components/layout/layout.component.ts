import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

interface NavItem {
  label: string;
  route: string;
  icon: string;
  adminOnly?: boolean;
  customerOnly?: boolean;
}

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './layout.component.html',
})
export class LayoutComponent implements OnInit {
  sidebarOpen = true;
  mobileMenuOpen = false;
  userName = '';
  userRole = '';
  userInitials = '';

  customerNav: NavItem[] = [
    { label: 'Dashboard', route: '/customer/dashboard', icon: 'home' },
    { label: 'Browse Plans', route: '/customer/policies/browse', icon: 'shield' },
    { label: 'My Policies', route: '/customer/policies/my-policies', icon: 'document' },
    { label: 'My Claims', route: '/customer/claims/my-claims', icon: 'clipboard' },
    { label: 'File a Claim', route: '/customer/claims/new', icon: 'plus-circle' },
  ];

  adminNav: NavItem[] = [
    { label: 'Admin Dashboard', route: '/admin/dashboard', icon: 'chart' },
    { label: 'Manage Claims', route: '/admin/claims', icon: 'clipboard' },
    { label: 'Policy Products', route: '/admin/policies', icon: 'shield' },
    { label: 'All Policies', route: '/admin/purchased-policies', icon: 'document' },
    { label: 'Users', route: '/admin/users', icon: 'users' },
    { label: 'Reports', route: '/admin/reports', icon: 'chart-bar' },
  ];

  get navItems(): NavItem[] {
    return this.auth.isAdmin() ? this.adminNav : this.customerNav;
  }

  constructor(public auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(user => {
      if (user) {
        this.userName = user.name || user.email || 'User';
        this.userRole = user.role || 'USER';
        this.userInitials = this.userName.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);
      }
    });
  }

  logout(): void {
    this.auth.logout();
  }
}
