import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { takeLast } from 'rxjs/operators';
import { Visaopf } from '../visao-pf/visao-pf.model';
import { VisaoPfService } from '../visao-pf/visao-pf.service';
import { FuncaoTransacaoVisaoPfService } from './funcao-transacao-visao-pf.service';
@Component({
  selector: 'app-funcao-transacao-visao-pf',
  templateUrl: './funcao-transacao-visao-pf.component.html'
})
export class FuncaoTransacaoVisaoPfComponent implements OnInit {

  routeState: any;
  visaopf: Visaopf = new Visaopf();

  constructor(private router: Router, private visaoPfService: VisaoPfService, private service: FuncaoTransacaoVisaoPfService) {
    if (this.router.getCurrentNavigation().extras.state) {
      this.routeState = this.router.getCurrentNavigation().extras.state;
    }
  }

  ngOnInit(): void {
  }

  finalizarContagem($event) {
    var state;
    var telas = $event.cenario.telasResult;
    this.tipo_de_tela(telas)
    if ($event.cenario.telasResult.length > 0) {
      state = {
        isEdit: this.routeState.isEdit,
        idAnalise: this.routeState.idAnalise,
        currentFuncaoTransacao: this.routeState.currentFuncaoTransacao,
        telasResult: JSON.stringify($event.cenario.telasResult),
        visaopf: $event,
        telas: telas
      }
    } else {
      state = {
        isEdit: this.routeState.isEdit,
        idAnalise: this.routeState.idAnalise,
        currentFuncaoTransacao: this.routeState.currentFuncaoTransacao
      }
    }

    this.router.navigate([`analise/${this.getUrl(-3, true)}/funcao-transacao-deteccao`], {
      state: state
    })
  }

  tipo_de_tela(telas) {
    for (let item of telas) {
      this.service.sendTipoTela(item.id, item.tipos, this.getUrl(-3, true), "FT").subscribe((resp: any) => {
      })
    }
  }

  getUrl(indice: number, tst: boolean = false) {
    var url = window.location.href.split("/")
    if (indice >= 0) {
      var resultado = url[indice]
    } else {
      var resultado = url[url.length + indice]
    }
    if (tst) {
      var analise_id = parseInt(resultado);
      return analise_id
    }
    return resultado
  }
}
