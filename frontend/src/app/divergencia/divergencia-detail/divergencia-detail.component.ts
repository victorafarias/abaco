import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Divergencia } from '../divergencia.model';
import { Subscription } from 'rxjs';
import { DivergenciaService } from '../divergencia.service';
import { Analise } from 'src/app/analise';


@Component({
  selector: 'app-divergencia-detail',
  templateUrl: './divergencia-detail.component.html'
  })
export class DivergenciaDetailComponent implements OnInit, OnDestroy {

  analise: Analise;
  private subscription: Subscription;


  constructor(
    private analiseService: DivergenciaService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.subscription = this.route.params.subscribe((params) => {
      this.load(params['id']);
    });
  }

  load(id) {
    this.analiseService.find(id).subscribe((analise) => {
     
      this.analise = analise;
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

}
