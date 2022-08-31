import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Sort, SortDirection } from '@angular/material/sort';
import { BdDataColumn, BdDataColumnDisplay } from 'src/app/models/data';

@Component({
  selector: 'app-bd-data-sorting',
  templateUrl: './bd-data-sorting.component.html',
})
export class BdDataSortingComponent<T> {
  /**
   * The columns to select for sorting
   */
  /* template */ _columns: BdDataColumn<T>[];

  @Input() set columns(cols: BdDataColumn<T>[]) {
    this._columns = cols.filter(
      (col) =>
        !col.display ||
        col.display === BdDataColumnDisplay.BOTH ||
        col.display === BdDataColumnDisplay.CARD
    );
  }

  @Input() sort: Sort;
  @Output() sortChange = new EventEmitter<Sort>();

  @Input() disabled = false;

  /* template */ direction: SortDirection;
  /* template */ get selectedColumn(): BdDataColumn<T> {
    return this._columns?.find((col) => col.id === this.sort?.active);
  }

  updateSortColumn(col: BdDataColumn<T>): void {
    this.sort = { active: col.id, direction: this.sort?.direction };
    this.sortChange.emit(this.sort);
  }

  updateSortDirection(direction: SortDirection): void {
    this.sort = { active: this.sort?.active, direction };
    this.sortChange.emit(this.sort);
  }
}