import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { PageNotificationService } from '@nuvem/primeng-components';
import { SelectItem } from 'primeng';
import { Subscription } from 'rxjs';
import { AnaliseService, Analise } from 'src/app/analise';
import { AnaliseSharedDataService } from 'src/app/shared/analise-shared-data.service';
import { StatusService } from 'src/app/status';
import { Status } from 'src/app/status/status.model';
import { MessageUtil } from 'src/app/util/message.util';
import { DivergenciaService } from '..';

@Component({
  selector: 'app-divergencia-view',
  templateUrl: './divergencia-view.component.html',
  styleUrls: ['./divergencia-view.component.css']
})
export class DivergenciaViewComponent implements OnInit {
  
  motivosAnalise: SelectItem[] = [
    {label: "Contagem BASIS menor. Maior parte dos erros FME", value: 'CONT_BASIS_MENOR_MAIOR_ERRO_FME'},
    {label: "Contagem BASIS menor. Maior parte dos erros BASIS", value: 'CONT_BASIS_MENOR_MAIOR_ERRO_BASIS'},
    {label: "Contagem BASIS maior. Maior parte dos erros FME", value: 'CONT_BASIS_MAIOR_MAIOR_ERRO_FME'},
    {label: "Contagem BASIS maior. Maior parte dos erros BASIS", value: 'CONT_BASIS_MAIOR_MAIOR_ERRO_BASIS'}
  ]
  
  
  analise = new Analise();
  
  statusCombo: Status[] = [];
  
  
  disableFuncaoTrasacao = true;
  disableAba: boolean;
  private routeSub: Subscription;
  
  constructor(private router: Router,
    private route: ActivatedRoute,
    private analiseService: AnaliseService,
    private analiseSharedDataService: AnaliseSharedDataService,
    private pageNotificationService: PageNotificationService,
    private statusService: StatusService,
    private divergenciaService: DivergenciaService,) { 
    }
    
    ngOnInit(): void {
      this.analiseSharedDataService.init();
      this.getAnalise();
      this.carregarStatus();
    }
    
    carregarStatus() {
      this.statusService.listaAtivoDivergencia().subscribe(
        lstStatus => {
          this.statusCombo = lstStatus;
        });
      }
      
      getLabel(label) {
        return label;
      }
      
      getAnalise() {
        this.routeSub = this.route.params.subscribe(params => {
          if (params['id']) {
            this.divergenciaService.find(params['id']).subscribe(analise => {
              this.analise = analise;
            });
            } else {
              this.analise = new Analise();
              this.analise.status = new Status();
              this.analise.esforcoFases = [];
              this.analise.enviarBaseline = true;
            }
          });
        }
        
        handleChange(e) {
          const index = e.index;
          let link;
          switch (index) {
            case 0:
            return;
            case 1:
            link = ['/divergencia/' + this.analise.id + '/funcao-dados/view'];
            break;
            case 2:
            link = ['/divergencia/' + this.analise.id + '/funcao-transacao/view'];
            break;
            case 3:
            link = ['/divergencia/' + this.analise.id + '/resumo/view'];
            break;
          }
          this.router.navigate(link);
        }
      }
      