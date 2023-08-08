import { Component, OnDestroy, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import {
  BehaviorSubject,
  Observable,
  Subscription,
  combineLatest,
  finalize,
  map,
} from 'rxjs';
import { SoftwareRepositoryConfiguration } from 'src/app/models/gen.dtos';
import { BdDialogComponent } from 'src/app/modules/core/components/bd-dialog/bd-dialog.component';
import { DirtyableDialog } from 'src/app/modules/core/guards/dirty-dialog.guard';
import { NavAreasService } from 'src/app/modules/core/services/nav-areas.service';
import { GroupsService } from 'src/app/modules/primary/groups/services/groups.service';
import { RepositoriesService } from 'src/app/modules/primary/repositories/services/repositories.service';

@Component({
  selector: 'app-add-repository',
  templateUrl: './add-repository.component.html',
})
export class AddRepositoryComponent implements OnDestroy, DirtyableDialog {
  /* template */ saving$ = new BehaviorSubject<boolean>(false);
  /* template */ repository: Partial<SoftwareRepositoryConfiguration> = {};
  /* template */ usedNames: string[] = [];

  private subscription: Subscription;

  @ViewChild(BdDialogComponent) dialog: BdDialogComponent;
  @ViewChild('form') public form: NgForm;

  constructor(
    private repositories: RepositoriesService,
    private groups: GroupsService,
    private areas: NavAreasService
  ) {
    this.subscription = areas.registerDirtyable(this, 'panel');

    combineLatest([
      this.groups.groups$.pipe(
        map((g) => g?.map((x) => x.instanceGroupConfiguration.name))
      ),
      this.repositories.repositories$.pipe(map((r) => r?.map((y) => y.name))),
    ])
      .pipe(map(([g, r]) => [...(g && g), ...(r && r)]))
      .subscribe((n) => {
        this.usedNames = n;
      });
  }

  isDirty(): boolean {
    return this.form.dirty;
  }

  canSave(): boolean {
    return this.form.valid;
  }

  /* template */ onSave() {
    this.saving$.next(true);
    this.doSave().subscribe(() => {
      this.areas.closePanel();
      this.subscription.unsubscribe();
    });
  }

  public doSave(): Observable<void> {
    this.saving$.next(true);
    return this.repositories.create(this.repository).pipe(
      finalize(() => {
        this.saving$.next(false);
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
