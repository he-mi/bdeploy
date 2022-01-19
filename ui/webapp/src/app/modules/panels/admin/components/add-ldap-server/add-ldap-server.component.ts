import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { LDAPSettingsDto } from 'src/app/models/gen.dtos';
import { SettingsService } from 'src/app/modules/core/services/settings.service';

@Component({
  selector: 'add-ldap-server',
  templateUrl: './add-ldap-server.component.html',
  styleUrls: ['./add-ldap-server.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddLdapServerComponent implements OnInit {
  /* template */ tempServer: Partial<LDAPSettingsDto>;

  constructor(private settings: SettingsService) {}

  ngOnInit(): void {
    this.tempServer = {
      server: 'ldaps://',
      accountPattern: '(objectCategory=person)',
      accountUserName: 'sAMAccountName',
      accountFullName: 'displayName',
      accountEmail: 'mail',
    };
  }

  /* template */ onSave() {
    this.settings.addLdapServer(this.tempServer);
  }
}