import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard/dashboard';
import { QrScreen } from './pages/qr-screen/qr-screen';
import { FraudReview } from './pages/fraud-review/fraud-review';
import { MerchantReview } from './pages/merchant-review/merchant-review';
import { AdminAnalytics } from './pages/admin-analytics/admin-analytics';
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
  { path: 'admin/fraud-review', component: FraudReview, canActivate: [authGuard, adminGuard] },
  { path: 'admin/analytics', component: AdminAnalytics, canActivate: [authGuard, adminGuard] },
  { path: 'admin/fraud-settings', component: FraudSettingsComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/kyc-review', component: KycReviewComponent, canActivate: [authGuard, adminGuard] },
  { path: 'merchant-review', component: MerchantReview, canActivate: [authGuard, adminGuard] },
  { path: 'merchant/onboard', component: MerchantOnboard, canActivate: [authGuard] },
  { path: 'admin/audit-logs', component: AuditLogs, canActivate: [authGuard, adminGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
