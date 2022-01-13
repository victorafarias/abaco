import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-brown-button',
  templateUrl: './brown-button.component.html',
  styleUrls: ['./brown-button.component.css']
})
export class BrownButtonComponent implements OnInit {

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
