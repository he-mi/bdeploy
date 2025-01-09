import { Component, Input } from '@angular/core';
import { LDAPSettingsDto } from 'src/app/models/gen.dtos';
import { BdPanelButtonComponent } from '../../../../../../core/components/bd-panel-button/bd-panel-button.component';

@Component({
    selector: 'app-ldap-check-action',
    templateUrl: './ldap-check-action.component.html',
    imports: [BdPanelButtonComponent]
})
export class LdapCheckActionComponent {
  @Input() record: LDAPSettingsDto;
}
