import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-yellow-button',
  templateUrl: './yellow-button.component.html',
  styleUrls: ['./yellow-button.component.css']
})
export class YellowButtonComponent implements OnInit {

  @Input()
  public buttonLabel: string;
  @Input()
  public buttonIcon: string;

  @Input()
  public isDisabled: boolean;

  constructor() { }

  ngOnInit() {
    (this.isDisabled === undefined) ? (this.isDisabled = false) : (this.isDisabled = this.isDisabled);
  }

}
