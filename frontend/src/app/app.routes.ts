import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard/dashboard';
import { QrScreen } from './pages/qr-screen/qr-screen';
import { FraudReview } from './pages/fraud-review/fraud-review';

export const routes: Routes = [
  { path: '', component: Dashboard },
  { path: 'qr', component: QrScreen },
  { path: 'admin', component: FraudReview },
  { path: '**', redirectTo: '' }
];
