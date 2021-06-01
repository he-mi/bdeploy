import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CoreModule } from '../../core/core.module';
import { InstancesModule as PrimaryInstancesModule } from '../../primary/instances/instances.module';
import { AddInstanceComponent } from './components/add-instance/add-instance.component';
import { AddProcessComponent } from './components/add-process/add-process.component';
import { AppTemplateNameComponent } from './components/add-process/app-template-name/app-template-name.component';
import { BulkControlComponent } from './components/bulk-control/bulk-control.component';
import { ConfigDescCardsComponent } from './components/config-desc-cards/config-desc-cards.component';
import { ConfigDescElementComponent } from './components/config-desc-element/config-desc-element.component';
import { ConfigProcessHeaderComponent } from './components/edit-process-overview/configure-process/config-process-header/config-process-header.component';
import { ConfigProcessParamGroupComponent } from './components/edit-process-overview/configure-process/config-process-param-group/config-process-param-group.component';
import { ConfigureProcessComponent } from './components/edit-process-overview/configure-process/configure-process.component';
import { CustomEditorComponent } from './components/edit-process-overview/configure-process/custom-editor/custom-editor.component';
import { EditProcessOverviewComponent } from './components/edit-process-overview/edit-process-overview.component';
import { HistoryCompareSelectComponent } from './components/history-compare-select/history-compare-select.component';
import { HistoryCompareComponent } from './components/history-compare/history-compare.component';
import { HistoryDiffFieldComponent } from './components/history-diff-field/history-diff-field.component';
import { HistoryEntryComponent } from './components/history-entry/history-entry.component';
import { HistoryHeaderConfigComponent } from './components/history-header-config/history-header-config.component';
import { HistoryProcessConfigComponent } from './components/history-process-config/history-process-config.component';
import { HistoryViewComponent } from './components/history-view/history-view.component';
import { AttributesComponent } from './components/instance-settings/attributes/attributes.component';
import { BannerComponent } from './components/instance-settings/banner/banner.component';
import { EditConfigComponent } from './components/instance-settings/edit-config/edit-config.component';
import { InstanceSettingsComponent } from './components/instance-settings/instance-settings.component';
import { InstanceTemplatesComponent } from './components/instance-settings/instance-templates/instance-templates.component';
import { TemplateMessageDetailsComponent } from './components/instance-settings/instance-templates/template-message-details/template-message-details.component';
import { MaintenanceComponent } from './components/instance-settings/maintenance/maintenance.component';
import { NodesComponent } from './components/instance-settings/nodes/nodes.component';
import { LocalChangesComponent } from './components/local-changes/local-changes.component';
import { LocalDiffComponent } from './components/local-changes/local-diff/local-diff.component';
import { NodeDetailsComponent } from './components/node-details/node-details.component';
import { ParamDescCardComponent } from './components/param-desc-card/param-desc-card.component';
import { ProcessConsoleComponent } from './components/process-console/process-console.component';
import { ProcessNativesComponent } from './components/process-natives/process-natives.component';
import { ProcessPortsComponent } from './components/process-ports/process-ports.component';
import { ProcessStatusComponent } from './components/process-status/process-status.component';
import { InstancesRoutingModule } from './instances-routing.module';
import { EditCustomUidValidatorDirective } from './validators/edit-custom-uid-validator.directive';
import { EditProcessNameValidatorDirective } from './validators/edit-process-name-validator.directive';
import { EditUniqueValueValidatorDirective } from './validators/edit-unique-value.directive';
import { MoveProcessComponent } from './components/edit-process-overview/move-process/move-process.component';
import { ImportInstanceComponent } from './components/instance-settings/import-instance/import-instance.component';

@NgModule({
  declarations: [
    AddInstanceComponent,
    ProcessStatusComponent,
    NodeDetailsComponent,
    ProcessPortsComponent,
    ProcessNativesComponent,
    ProcessConsoleComponent,
    BulkControlComponent,
    HistoryEntryComponent,
    HistoryCompareSelectComponent,
    HistoryCompareComponent,
    HistoryViewComponent,
    HistoryProcessConfigComponent,
    HistoryDiffFieldComponent,
    ConfigDescCardsComponent,
    HistoryHeaderConfigComponent,
    InstanceSettingsComponent,
    EditConfigComponent,
    MaintenanceComponent,
    AttributesComponent,
    LocalChangesComponent,
    LocalDiffComponent,
    NodesComponent,
    AddProcessComponent,
    AppTemplateNameComponent,
    EditProcessOverviewComponent,
    ConfigureProcessComponent,
    InstanceTemplatesComponent,
    TemplateMessageDetailsComponent,
    ConfigProcessHeaderComponent,
    EditProcessNameValidatorDirective,
    EditCustomUidValidatorDirective,
    EditUniqueValueValidatorDirective,
    ConfigDescElementComponent,
    ConfigProcessParamGroupComponent,
    ParamDescCardComponent,
    CustomEditorComponent,
    BannerComponent,
    MoveProcessComponent,
    ImportInstanceComponent,
  ],
  imports: [
    CommonModule,
    CoreModule,
    InstancesRoutingModule,
    PrimaryInstancesModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatExpansionModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
  ],
})
export class InstancesModule {}
