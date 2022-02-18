import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Visaopf } from '../visao-pf/visao-pf.model';
import { VisaoPfService } from '../visao-pf/visao-pf.service';
import { FuncaoDadosVisaoPfService } from './funcao-dados-visao-pf.service';

@Component({
  selector: 'app-funcao-dados-visao-pf',
  templateUrl: './funcao-dados-visao-pf.component.html'
})

export class FuncaoDadosVisaoPfComponent implements OnInit {

  routeState: any;
  visaopf: Visaopf = new Visaopf();

  constructor(private router: Router, private visaoPfService: VisaoPfService, private service: FuncaoDadosVisaoPfService) {
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
        seletedFuncaoDados: this.routeState.seletedFuncaoDados,
        telasResult: JSON.stringify($event.cenario.telasResult),
        visaopf: $event,
        telas: telas
      }
    } else {
      state = {
        isEdit: this.routeState.isEdit,
        idAnalise: this.routeState.idAnalise,
        seletedFuncaoDados: this.routeState.seletedFuncaoDados
      }
    }
    this.router.navigate([`analise/${this.getUrl(-3, true)}/funcao-dados`], {
      state: state
    })
  }

  tipo_de_tela(telas) {
    var analise_id = this.getUrl(-3, true)
    for (let item of telas) {
      this.service.sendTipoTela(item.id, item.tipos, analise_id, "FD").subscribe((resp: any) => {
        if (resp) console.log(resp)
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

