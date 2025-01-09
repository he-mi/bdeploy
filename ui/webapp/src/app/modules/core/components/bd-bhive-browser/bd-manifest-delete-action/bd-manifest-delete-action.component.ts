import { Component, forwardRef, inject, Input } from '@angular/core';
import { HiveEntryDto } from 'src/app/models/gen.dtos';
import { HiveService } from 'src/app/modules/primary/admin/services/hive.service';
import { AuthenticationService } from '../../../services/authentication.service';
import { BdBHiveBrowserComponent } from '../bd-bhive-browser.component';
import { BdButtonComponent } from '../../bd-button/bd-button.component';
import { ClickStopPropagationDirective } from '../../../directives/click-stop-propagation.directive';
import { AsyncPipe } from '@angular/common';

@Component({
    selector: 'app-bd-manifest-delete-action',
    templateUrl: './bd-manifest-delete-action.component.html',
    imports: [BdButtonComponent, ClickStopPropagationDirective, AsyncPipe]
})
export class BdManifestDeleteActionComponent {
  private readonly hives = inject(HiveService);
  private readonly parent = inject(forwardRef(() => BdBHiveBrowserComponent));
  protected readonly auth = inject(AuthenticationService);

  @Input() record: HiveEntryDto;

  protected onDelete(): void {
    this.parent.dialog
      .confirm(`Delete ${this.record.name}?`, `This will remove the manifest permanently from the enclosing BHive.`)
      .subscribe((r) => {
        if (r) {
          this.hives
            .delete(this.parent.bhive$.value, this.record.mName, this.record.mTag)
            .subscribe(() => this.parent.load());
        }
      });
  }
}
