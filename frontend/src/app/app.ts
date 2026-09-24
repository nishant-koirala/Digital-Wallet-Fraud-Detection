import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { TopBar } from './layout/top-bar/top-bar';
import { SimulatorPanelComponent } from './components/simulator-panel/simulator-panel.component';
import { ToastComponent } from './components/toast/toast.component';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, TopBar, SimulatorPanelComponent, ToastComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App {
  title = 'fraud-detection-wallet';
  isProduction = environment.production;
}
