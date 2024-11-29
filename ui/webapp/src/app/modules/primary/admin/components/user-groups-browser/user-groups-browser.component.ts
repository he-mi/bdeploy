import { Component, inject } from '@angular/core';
import { combineLatest, map } from 'rxjs';
import { BdDataColumn, BdDataGroupingDefinition } from 'src/app/models/data';
import { UserGroupInfo } from 'src/app/models/gen.dtos';
import { BdDataIconCellComponent } from 'src/app/modules/core/components/bd-data-icon-cell/bd-data-icon-cell.component';
import { BdDataPermissionLevelCellComponent } from 'src/app/modules/core/components/bd-data-permission-level-cell/bd-data-permission-level-cell.component';
import { SettingsService } from 'src/app/modules/core/services/settings.service';
import { UserGroupsColumnsService } from 'src/app/modules/core/services/user-groups-columns.service';
import { getGlobalPermission } from 'src/app/modules/core/utils/permission.utils';
import { UserGroupBulkService } from 'src/app/modules/panels/admin/services/user-group-bulk.service';
import { AuthAdminService } from '../../services/auth-admin.service';

@Component({
    selector: 'app-user-groups-browser',
    templateUrl: './user-groups-browser.component.html',
    styleUrls: ['./user-groups-browser.component.css'],
    standalone: false
})
export class UserGroupsBrowserComponent {
  private readonly groupCols = inject(UserGroupsColumnsService);
  protected readonly authAdmin = inject(AuthAdminService);
  protected readonly settings = inject(SettingsService);
  protected readonly bulk = inject(UserGroupBulkService);

  private readonly colPermLevel: BdDataColumn<UserGroupInfo> = {
    id: 'permLevel',
    name: 'Global Permission',
    data: (r) => getGlobalPermission(r.permissions),
    component: BdDataPermissionLevelCellComponent,
  };

  private readonly colInact: BdDataColumn<UserGroupInfo> = {
    id: 'inactive',
    name: 'Inact.',
    data: (r) => (r.inactive ? 'check_box' : 'check_box_outline_blank'),
    component: BdDataIconCellComponent,
    width: '40px',
  };

  protected readonly columns: BdDataColumn<UserGroupInfo>[] = [
    ...this.groupCols.defaultColumns,
    this.colInact,
    this.colPermLevel,
  ];

  protected loading$ = combineLatest([this.settings.loading$, this.authAdmin.loadingUsers$]).pipe(
    map(([s, a]) => s || a),
  );

  protected getRecordRoute = (row: UserGroupInfo) => {
    return [
      '',
      {
        outlets: { panel: ['panels', 'admin', 'user-group-detail', row.id] },
      },
    ];
  };

  protected grouping: BdDataGroupingDefinition<UserGroupInfo>[] = [
    {
      name: 'Global Permission',
      group: (r) => getGlobalPermission(r.permissions),
      associatedColumn: this.colPermLevel.id,
    },
  ];
}
