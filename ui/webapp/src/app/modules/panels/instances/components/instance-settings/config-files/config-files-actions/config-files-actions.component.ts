import {
  Component,
  forwardRef,
  Inject,
  Input,
  OnInit,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { Base64 } from 'js-base64';
import { BehaviorSubject, first, skipWhile } from 'rxjs';
import { FileStatusType } from 'src/app/models/gen.dtos';
import {
  ACTION_CANCEL,
  ACTION_CONFIRM,
  ACTION_OK,
} from 'src/app/modules/core/components/bd-dialog-message/bd-dialog-message.component';
import { BdFormInputComponent } from 'src/app/modules/core/components/bd-form-input/bd-form-input.component';
import { InstanceEditService } from 'src/app/modules/primary/instances/services/instance-edit.service';
import {
  ConfigFile,
  ConfigFilesService,
  ConfigFileStatusType,
} from '../../../../services/config-files.service';
import { ConfigFilesComponent } from '../config-files.component';

@Component({
  selector: 'app-config-files-actions',
  templateUrl: './config-files-actions.component.html',
})
export class ConfigFilesActionsComponent implements OnInit {
  @Input() record: ConfigFile;

  /* template */ isEditAllowed: boolean;
  /* template */ path: string;
  /* template */ isText: boolean;
  /* template */ status: ConfigFileStatusType;

  /* template */ newName: string;
  /* template */ renameAllowed: boolean;

  /* template */ tempFileError: string;
  /* template */ tempFileContentLoading$ = new BehaviorSubject<boolean>(false);
  /* template */ tempFileContent = '';
  private tempFileIsBin = false;

  @ViewChild('renameInput', { static: false })
  private renameInput: BdFormInputComponent;

  constructor(
    private cfgFiles: ConfigFilesService,
    private edit: InstanceEditService,
    @Inject(forwardRef(() => ConfigFilesComponent))
    private parent: ConfigFilesComponent
  ) {}

  ngOnInit(): void {
    this.edit.state$.subscribe(() => {
      this.isText = this.cfgFiles.isText(this.record);
      this.path = this.cfgFiles.getPath(this.record);
      this.status = this.cfgFiles.getStatus(this.record);

      this.isEditAllowed = this.canEdit();
      this.renameAllowed = this.canRename();
    });
  }

  private canEdit(): boolean {
    if (!this.isText) {
      return false; // not a text file, editor would explode.
    }
    if (this.record.modification?.type === FileStatusType.DELETE) {
      return false;
    }

    return true;
  }

  private canRename(): boolean {
    if (this.record.modification?.type === FileStatusType.DELETE) {
      return false;
    }

    return true;
  }

  /* template */ onRename(tpl: TemplateRef<any>): void {
    const oldName = this.path;
    const isBin = !this.isText;
    this.newName = oldName;

    this.parent.dialog
      .message({
        header: `Rename ${oldName}`,
        template: tpl,
        validation: () =>
          !this.renameInput ? false : !this.renameInput.isInvalid(),
        actions: [ACTION_CANCEL, ACTION_CONFIRM],
      })
      .subscribe((r) => {
        if (!r) {
          return;
        }
        this.cfgFiles.load(oldName).subscribe((content) => {
          if (!confirm) {
            return;
          }

          this.cfgFiles.move(oldName, this.newName, content, isBin);
        });
      });
  }

  /* template */ onDelete(): void {
    this.parent.dialog
      .confirm(
        `Delete ${this.path}?`,
        `This will remove the file ${this.path} from the current set of configuration files.`
      )
      .subscribe((r) => {
        if (r) {
          this.cfgFiles.delete(this.path);
        }
      });
  }

  /* template */ doReplaceFile(tpl: TemplateRef<any>): void {
    this.tempFileContent = '';

    this.parent.dialog
      .message({
        header: 'Replace Configuration File',
        icon: 'upload_file',
        template: tpl,
        actions: [ACTION_CANCEL, ACTION_OK],
      })
      .subscribe((r) => {
        if (!r) {
          return;
        }
        this.cfgFiles.edit(
          this.record.persistent?.path
            ? this.record.persistent.path
            : this.record.modification.file,
          this.tempFileContent,
          this.tempFileIsBin
        );
      });
  }

  /* template */ doAddFileContent(file: File) {
    this.tempFileError = null;
    this.tempFileContentLoading$.next(true);

    if (file.size > 1024 * 1024 * 20) {
      this.tempFileContentLoading$.next(false);
      this.tempFileError = 'Selected File is too large, size limit 20MB';
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result.toString();

      // extract the base64 part of the data URL...
      this.tempFileContent = result.substr(result.indexOf(',') + 1);

      const buffer = Base64.toUint8Array(this.tempFileContent);
      let isBinary = false;
      for (let i = 0; i < Math.max(4096, buffer.length); ++i) {
        if (buffer[i] === 0) {
          isBinary = true;
          break;
        }
      }
      this.tempFileIsBin = isBinary;
      this.tempFileContentLoading$.next(false);
    };
    reader.readAsDataURL(file);
  }

  /* template */ doAddFromTemplate() {
    this.edit.state$
      .pipe(
        skipWhile((s) => !s?.config?.config?.product),
        first()
      )
      .subscribe((s) => {
        this.cfgFiles
          .loadTemplate(this.path, s.config.config.product)
          .subscribe((t) => {
            this.cfgFiles.add(this.path, t, !this.isText);
          });
      });
  }

  /* template */ doRestoreFromTemplate() {
    this.edit.state$
      .pipe(
        skipWhile((s) => !s?.config?.config?.product),
        first()
      )
      .subscribe((s) => {
        this.cfgFiles
          .loadTemplate(this.path, s.config.config.product)
          .subscribe((t) => {
            this.cfgFiles.edit(this.path, t, !this.isText);
          });
      });
  }
}