import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    selector: 'app-color-select',
    templateUrl: './color-select.component.html',
    styleUrls: ['./color-select.component.css'],
    standalone: false
})
export class ColorSelectComponent {
  @Input() name: string;
  @Input() fg: string;
  @Input() bg: string;

  @Input() selected: boolean;
  @Output() selectedChanged = new EventEmitter<ColorSelectComponent>();

  protected onClick() {
    this.selectedChanged.emit(this);
  }
}
