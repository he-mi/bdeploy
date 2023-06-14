import { Component } from '@angular/core';
import { SettingsService } from 'src/app/modules/core/services/settings.service';

@Component({
  selector: 'app-auth0-tab',
  templateUrl: './auth0-tab.component.html',
})
export class Auth0TabComponent {
  constructor(public settings: SettingsService) {}
}