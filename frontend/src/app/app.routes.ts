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
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: '', component: Dashboard, canActivate: [authGuard] },
  { path: 'qr', component: QrScreen, canActivate: [authGuard] },
  { path: 'pay-bills', component: PayBills, canActivate: [authGuard] },
  { path: 'admin', component: FraudReview, canActivate: [authGuard] },
  { path: 'admin/analytics', component: AdminAnalytics, canActivate: [authGuard] },
  { path: 'merchant-review', component: MerchantReview, canActivate: [authGuard] },
  { path: 'merchant/onboard', component: MerchantOnboard, canActivate: [authGuard] },
  { path: 'admin/audit-logs', component: AuditLogs, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
