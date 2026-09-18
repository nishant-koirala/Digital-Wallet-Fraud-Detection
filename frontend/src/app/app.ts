import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TopBar } from './layout/top-bar/top-bar';
import { ToastComponent } from './components/toast/toast.component';
import { SimulatorPanelComponent } from './components/simulator-panel/simulator-panel.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TopBar, ToastComponent, SimulatorPanelComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('frontend');
}
