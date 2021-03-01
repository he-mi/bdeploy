import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';
import {
  BdDataColumn,
  BdDataColumnDisplay,
  BdDataGrouping,
  bdExtractGroups,
  UNMATCHED_GROUP,
} from 'src/app/models/data';
import { LoggingService } from '../../services/logging.service';
import { BdSearchable, SearchService } from '../../services/search.service';

@Component({
  selector: 'app-bd-data-grid',
  templateUrl: './bd-data-grid.component.html',
  styleUrls: ['./bd-data-grid.component.css'],
})
export class BdDataGridComponent<T> implements OnInit, OnDestroy, BdSearchable {
  private log = this.logging.getLogger('BdDataTableComponent');

  /**
   * The columns to display
   */
  /* template */ _columns: BdDataColumn<T>[];
  @Input() set columns(val: BdDataColumn<T>[]) {
    // either unset or CARD is OK, only TABLE is not OK.
    this._columns = val.filter((c) => c.display !== BdDataColumnDisplay.TABLE);
  }

  /**
   * A callback which provides enhanced searching in the grid. The default search will
   * concatenate each value in each row object, regardless of whether it is displayed or not.
   * Then the search string is applied to this single string in a case insensitive manner.
   */
  @Input() searchData: (search: string, data: T[]) => T[];

  /**
   * Whether the data-grid should register itself as a BdSearchable with the global SearchService.
   */
  @Input() searchable = true;

  /**
   * A single grouping definition. The data will be grouped according to this definition.
   * Multiple grouping is not supported by the grid.
   */
  @Input() grouping: BdDataGrouping<T>;

  /**
   * The actual data. Arbitrary data which can be handled by the column definitions.
   */
  @Input() records: T[];

  /**
   * Event fired if a record is clicked.
   */
  @Output() recordClick = new EventEmitter<T>();

  /*template*/ recordsToDisplay$ = new BehaviorSubject<T[]>([]);

  private subscription: Subscription;

  constructor(private logging: LoggingService, private searchService: SearchService) {}

  ngOnInit(): void {
    if (this.searchable) {
      // register this table as "searchable" in the global search service if requested.
      this.subscription = this.searchService.register(this);
    }

    // populate records to display with empty search by default.
    this.bdOnSearch(null);
  }

  ngOnDestroy(): void {
    if (!!this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  bdOnSearch(search: string) {
    this.recordsToDisplay$.next(this.searchData(search, this.records));
  }

  /* template */ getGroupValues() {
    return bdExtractGroups(this.grouping.definition, this.records);
  }

  /* template */ getGroupRecords(group) {
    return this.recordsToDisplay$.value.filter((r) => {
      const grp = this.grouping.definition.group(r);
      if (!grp && group === UNMATCHED_GROUP) {
        return true;
      }
      return grp === group;
    });
  }

  /* template */ isEmpty(group) {
    if (!this.recordsToDisplay$.value?.length) {
      return true;
    }

    if (!!this.grouping && !this.getGroupRecords(group)?.length) {
      return true;
    }

    return false;
  }
}