import { Injectable } from '@angular/core';
import {
  BdDataColumn,
  BdDataColumnDisplay,
  BdDataColumnTypeHint,
} from 'src/app/models/data';
import { ApplicationConfiguration } from 'src/app/models/gen.dtos';
import { BdDataSvgIconCellComponent } from 'src/app/modules/core/components/bd-data-svg-icon-cell/bd-data-svg-icon-cell.component';
import { getAppOs } from 'src/app/modules/core/utils/manifest.utils';
import { ProcessOutdatedComponent } from '../components/dashboard/process-outdated/process-outdated.component';
import { ProcessStatusIconComponent } from '../components/dashboard/process-status-icon/process-status-icon.component';
import { PortStatusColumnComponent } from '../components/port-status-column/port-status-column.component';
import { ProcessNameAndOsComponent } from '../components/process-name-and-os/process-name-and-os.component';
import { InstancesService } from './instances.service';
import { PortsService } from './ports.service';
import { ProcessesService } from './processes.service';

@Injectable({
  providedIn: 'root',
})
export class ProcessesColumnsService {
  processNameColumn: BdDataColumn<ApplicationConfiguration> = {
    id: 'name',
    name: 'Name',
    hint: BdDataColumnTypeHint.TITLE,
    data: (r) => r.name,
  };

  processNameAndOsColumn: BdDataColumn<ApplicationConfiguration> = {
    id: 'nameAndOs',
    name: 'Name and OS',
    hint: BdDataColumnTypeHint.TITLE,
    data: (r) => r.name,
    component: ProcessNameAndOsComponent,
  };

  processIdColumn: BdDataColumn<ApplicationConfiguration> = {
    id: 'id',
    name: 'ID',
    hint: BdDataColumnTypeHint.DESCRIPTION,
    data: (r) => (r.id ? r.id : 'New Process'),
    isId: true,
    width: '135px',
    showWhen: '(min-width:1000px)',
    classes: (r) => (r.id ? [] : ['bd-description-text']),
  };

  processAvatarColumn: BdDataColumn<ApplicationConfiguration> = {
    id: 'os-avatar',
    name: 'OS',
    hint: BdDataColumnTypeHint.AVATAR,
    data: (r) => `/assets/${getAppOs(r.application).toLowerCase()}.svg`,
    display: BdDataColumnDisplay.CARD,
  };

  processOsColumn: BdDataColumn<ApplicationConfiguration> = {
    id: 'os',
    name: 'OS',
    data: (r) => getAppOs(r.application),
    component: BdDataSvgIconCellComponent,
    width: '30px',
  };

  applicationNameColumn: BdDataColumn<ApplicationConfiguration> = {
    id: 'appName',
    name: 'Application Type',
    data: (r) =>
      this.instances.activeNodeCfgs$.value?.applications?.find(
        (app) => app.name === r.name
      )?.descriptor?.name, // this.edit.getApplicationDescriptor(r.application.name)?.name,
    width: '200px',
    showWhen: '(min-width: 1180px)',
  };

  processActualityColumn: BdDataColumn<ApplicationConfiguration> = {
    id: 'actuality',
    name: 'Actuality',
    description:
      'Whether the process is running from the currently active instance version',
    hint: BdDataColumnTypeHint.DETAILS,
    component: ProcessOutdatedComponent,
    data: (r) => {
      const procTag = ProcessesService.get(
        this.processes.processStates$.value,
        r.id
      )?.instanceTag;
      const instTag = this.instances.active$.value?.instance?.tag;
      return procTag === instTag ? null : procTag;
    }, // for sorting and display on the card.
    icon: () => 'thermostat',
    width: '70px',
  };

  processStatusColumn: BdDataColumn<ApplicationConfiguration> = {
    id: 'status',
    name: 'Status',
    hint: BdDataColumnTypeHint.STATUS,
    component: ProcessStatusIconComponent,
    data: (r) =>
      ProcessesService.get(this.processes.processStates$.value, r.id)
        ?.processState,
    width: '40px',
  };

  processPortRatingColumn: BdDataColumn<ApplicationConfiguration> = {
    id: 'portStates',
    name: 'Ports',
    hint: BdDataColumnTypeHint.STATUS,
    component: PortStatusColumnComponent,
    data: (r) => this.getAllPortsRating(r),
    width: '40px',
  };

  defaultProcessesColumns: BdDataColumn<ApplicationConfiguration>[] = [
    this.processNameColumn,
    this.processIdColumn,
    this.processAvatarColumn,
    this.applicationNameColumn,
    this.processStatusColumn,
    this.processPortRatingColumn,
    this.processActualityColumn,
  ];

  defaultProcessClientColumns: BdDataColumn<ApplicationConfiguration>[] = [
    this.processNameAndOsColumn,
    this.processIdColumn,
    this.processAvatarColumn,
    this.applicationNameColumn,
  ];

  defaultProcessesConfigColumns: BdDataColumn<ApplicationConfiguration>[] = [
    this.processIdColumn,
    this.processAvatarColumn,
    this.applicationNameColumn,
  ];

  constructor(
    private processes: ProcessesService,
    private instances: InstancesService,
    private ports: PortsService
  ) {}

  private getAllPortsRating(r: ApplicationConfiguration) {
    const currentStates = this.ports.activePortStates$.value;
    const processState = ProcessesService.get(
      this.processes.processStates$.value,
      r.id
    );
    if (!currentStates || !processState) {
      return undefined;
    }

    const appPorts = currentStates.filter((p) => p.appId === r.id);
    if (ProcessesService.isRunning(processState.processState)) {
      // process running, all ports should be open.
      return appPorts.every((p) => p.state);
    } else {
      // process not running, all ports should be closed.
      return appPorts.every((p) => !p.state);
    }
  }
}
