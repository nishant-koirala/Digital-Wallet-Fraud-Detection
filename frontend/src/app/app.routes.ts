import { Routes } from '@angular/router';
import { authGuard } from './auth.guard';
import { adminGuard } from './admin.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login').then(m => m.Login) },
  { path: 'verify-otp', loadComponent: () => import('./pages/otp-verify/otp-verify').then(m => m.OtpVerify) },
  { path: 'register', loadComponent: () => import('./pages/register/register').then(m => m.Register) },
  { path: '', loadComponent: () => import('./pages/dashboard/dashboard').then(m => m.Dashboard), canActivate: [authGuard] },
  { path: 'transactions', loadComponent: () => import('./pages/transactions-page/transactions-page').then(m => m.TransactionsPage), canActivate: [authGuard] },
  { path: 'qr', loadComponent: () => import('./pages/qr-screen/qr-screen').then(m => m.QrScreen), canActivate: [authGuard] },
  { path: 'pay-bills', loadComponent: () => import('./pages/pay-bills/pay-bills').then(m => m.PayBills), canActivate: [authGuard] },
  { path: 'kyc', loadComponent: () => import('./pages/kyc-submit/kyc-submit').then(m => m.KycSubmitComponent), canActivate: [authGuard] },
  {
    path: 'admin',
    loadComponent: () => import('./layout/admin-layout/admin-layout').then(m => m.AdminLayoutComponent),
    canActivate: [authGuard, adminGuard],
    children: [
      { path: 'analytics', loadComponent: () => import('./pages/admin-analytics/admin-analytics').then(m => m.AdminAnalytics) },
      { path: 'fraud-review', loadComponent: () => import('./pages/fraud-review/fraud-review').then(m => m.FraudReview) },
      { path: 'fraud-settings', loadComponent: () => import('./pages/fraud-settings/fraud-settings').then(m => m.FraudSettingsComponent) },
      { path: 'kyc-review', loadComponent: () => import('./pages/kyc-review/kyc-review').then(m => m.KycReviewComponent) },
      { path: 'merchants', loadComponent: () => import('./pages/merchant-review/merchant-review').then(m => m.MerchantReview) },
      { path: 'audit-logs', loadComponent: () => import('./pages/audit-logs/audit-logs').then(m => m.AuditLogs) },
      { path: 'reports', loadComponent: () => import('./pages/admin-reports/admin-reports').then(m => m.AdminReports) },
      { path: '', redirectTo: 'analytics', pathMatch: 'full' }
    ]
  },
  { path: 'merchant/onboard', loadComponent: () => import('./pages/merchant-onboard/merchant-onboard').then(m => m.MerchantOnboard), canActivate: [authGuard] },
  { path: 'profile', loadComponent: () => import('./pages/profile/profile').then(m => m.Profile), canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
