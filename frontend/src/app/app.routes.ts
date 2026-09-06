import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard/dashboard';
import { QrScreen } from './pages/qr-screen/qr-screen';
import { FraudReview } from './pages/fraud-review/fraud-review';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: '', component: Dashboard, canActivate: [authGuard] },
  { path: 'qr', component: QrScreen, canActivate: [authGuard] },
  { path: 'admin', component: FraudReview, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
