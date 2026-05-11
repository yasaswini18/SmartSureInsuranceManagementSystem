export type UserRole = 'USER' | 'ADMIN' | 'CUSTOMER';
export type ClaimStatus = 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'SETTLED';
export type ClaimType = 'FULL_LOSS' | 'PARTIAL_LOSS' | 'MEDICAL' | 'THEFT' | 'ACCIDENTAL_DAMAGE';

export interface RegisterRequest {
  fullName: string;
  name?: string;
  email: string;
  password: string;
  phone: string;
  address: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  message?: string;
  accessToken?: string;
  refreshToken?: string;
  token?: string;
  tokenType?: string;
  accessTokenExpiresIn?: number;
  refreshTokenExpiresIn?: number;
  userId?: number;
  role?: UserRole;
  name?: string;
  email?: string;
}

export interface User {
  id?: number;
  name?: string;
  fullName?: string;
  email: string;
  phone?: string;
  address?: string;
  role: UserRole;
  enabled?: boolean;
  createdAt?: string;
  policyCount?: number;
  claimCount?: number;
}

export interface PolicyProduct {
  id: number;
  name: string;
  description: string;
  type: string;
  basePremium: number;
  coverageAmount: number;
  durationMonths: number;
  minAge: number;
  maxAge: number;
  isActive: boolean;
  createdAt?: string;
}

export interface PolicyProductRequest {
  name: string;
  description: string;
  type: string;
  basePremium: number;
  coverageAmount: number;
  durationMonths: number;
  minAge: number;
  maxAge: number;
}

export interface PurchasedPolicy {
  id: number;
  policyNumber: string;
  productName: string;
  policyType: string;
  premiumPaid: number;
  coverageAmount: number;
  startDate: string;
  endDate: string;
  status: 'CREATED' | 'ACTIVE' | 'EXPIRED' | 'CANCELLED';
  customerName?: string;
  customerEmail?: string;
  daysRemaining?: number;
  extraDetailsJson?: string;
}

export interface PurchasePolicyRequest {
  productId?: number;
  policyTypeId?: number;
  age: number;
  extraDetails?: Record<string, unknown>;
}

export interface Claim {
  id: number;
  claimNumber: string;
  policyNumber: string;
  customerEmail?: string;
  productName?: string;
  policyType?: string;
  coverageAmount?: number;
  claimType: ClaimType | string;
  description: string;
  status: ClaimStatus | string;
  incidentDate: string;
  claimedAmount: number;
  approvedAmount?: number;
  createdAt?: string;
  reviewedAt?: string;
  reviewedBy?: string;
  adminRemarks?: string;
  documents?: ClaimDocument[];
  daysSinceFiled?: number;
}

export interface ClaimDocument {
  id?: number;
  fileName: string;
  fileType?: string;
  documentType?: string;
  uploadedAt?: string;
  fileUrl?: string;
}

export interface ClaimRequest {
  policyNumber: string;
  claimType: ClaimType | string;
  description: string;
  incidentDate: string;
  claimedAmount: number;
}

export interface ReviewRequest {
  decision: 'APPROVED' | 'REJECTED';
  adminRemarks: string;
  approvedAmount?: number | null;
}

export interface DashboardReport {
  totalUsers: number;
  totalAdmins?: number;
  totalCustomers?: number;
  totalPolicyProducts?: number;
  activePolicyProducts?: number;
  totalPurchasedPolicies: number;
  activePolicies: number;
  expiredPolicies?: number;
  cancelledPolicies?: number;
  totalClaims: number;
  pendingClaims: number;
  underReviewClaims?: number;
  approvedClaims: number;
  rejectedClaims: number;
  settledClaims?: number;
  totalPremiumCollected: number;
  totalClaimedAmount?: number;
  totalApprovedAmount?: number;
  totalSettledAmount?: number;
  recentAuditActions?: number;
  generatedAt?: string;
}

export interface AuditLog {
  id: number;
  adminEmail: string;
  action: string;
  resourceType: string;
  resourceId: string;
  details: string;
  timestamp: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
}
