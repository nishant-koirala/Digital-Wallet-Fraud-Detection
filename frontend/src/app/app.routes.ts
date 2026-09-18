import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout';
import { Dashboard } from './pages/dashboard/dashboard';
import { QrScreen } from './pages/qr-screen/qr-screen';
import { FraudReview } from './pages/fraud-review/fraud-review';
import { MerchantReview } from './pages/merchant-review/merchant-review';
import { AdminAnalytics } from './pages/admin-analytics/admin-analytics';
import { AdminReports } from './pages/admin-reports/admin-reports';
import { PayBills } from './pages/pay-bills/pay-bills';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { MerchantOnboard } from './pages/merchant-onboard/merchant-onboard';
import { AuditLogs } from './pages/audit-logs/audit-logs';
import { Profile } from './pages/profile/profile';
import { FraudSettingsComponent } from './pages/fraud-settings/fraud-settings';
import { KycSubmitComponent } from './pages/kyc-submit/kyc-submit';
import { OtpVerify } from './pages/otp-verify/otp-verify';
import { KycReviewComponent } from './pages/kyc-review/kyc-review';
import { TransactionsPage } from './pages/transactions-page/transactions-page';
import { authGuard } from './auth.guard';
import { adminGuard } from './admin.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'verify-otp', component: OtpVerify },
  { path: 'register', component: Register },
  { path: '', component: Dashboard, canActivate: [authGuard] },
  { path: 'transactions', component: TransactionsPage, canActivate: [authGuard] },
  { path: 'qr', component: QrScreen, canActivate: [authGuard] },
  { path: 'pay-bills', component: PayBills, canActivate: [authGuard] },
  { path: 'kyc', component: KycSubmitComponent, canActivate: [authGuard] },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard, adminGuard],
    children: [
      { path: 'analytics', component: AdminAnalytics },
      { path: 'fraud-review', component: FraudReview },
      { path: 'fraud-settings', component: FraudSettingsComponent },
      { path: 'kyc-review', component: KycReviewComponent },
      { path: 'merchants', component: MerchantReview },
      { path: 'audit-logs', component: AuditLogs },
      { path: 'reports', component: AdminReports },
      { path: '', redirectTo: 'analytics', pathMatch: 'full' }
    ]
  },
  { path: 'merchant/onboard', component: MerchantOnboard, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
