import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { DerChipConverter } from '../analise-shared/der-chips/der-chip-converter';
import { DerChipItem } from '../analise-shared/der-chips/der-chip-item';
import { FuncaoTransacao } from '../funcao-transacao';
import { FuncaoDadosService } from '../funcao-dados/funcao-dados.service';
import { VisaoPfService } from '../visao-pf/visao-pf.service';
import { Sistema, SistemaService } from '../sistema';
import { Modulo } from '../modulo';
import { Analise } from '../analise';
import { AnaliseSharedDataService } from '../shared/analise-shared-data.service';
@Component({
  selector: 'app-funcao-transacao-deteccao',
  templateUrl: './funcao-transacao-deteccao.component.html'
})
export class FuncaoTransacaoDeteccaoComponent implements OnInit {
  teste: boolean = false
  textHeader: string;
  isEdit: boolean
  dersChips: DerChipItem[];
  alrsChips: DerChipItem[];
  resultFuncaoDados: any[] = []
  resultFuncaoTransacao: any[] = []
  tiposDeComponentes: any[] = []
  private idAnalise: Number;
  public currentFuncaoTransacao: FuncaoTransacao = new FuncaoTransacao();
  public routeState: any
  public modulosTeste: Modulo[];
  public analise: Analise;

  constructor(
    private router: Router,
    private vpfService: VisaoPfService,
    private fdService: FuncaoDadosService,
    private sistemaService: SistemaService,
    private analiseSharedDataService: AnaliseSharedDataService
  ) {
    if (this.router.getCurrentNavigation().extras.state) {
      this.routeState = this.router.getCurrentNavigation().extras.state;
    }
  }


  getLabel(label) {
    return label;
  }

  ngOnInit(): void {
    if (this.routeState) this.resultadoDeteccao()
  }

  private desconverterChips() {
    if (this.dersChips != null && this.alrsChips != null) {
      this.currentFuncaoTransacao.ders = DerChipConverter.desconverterEmDers(this.dersChips);
      this.currentFuncaoTransacao.alrs = DerChipConverter.desconverterEmAlrs(this.alrsChips);
    }
  }

  detectarComponentesVpf() {
    var url = window.location.href.split("/")
    var id = url[url.length - 2]
    var routerNavigate = `visaopf/${id}/deteccomponentes/funcao-transacao`
    var funcTransacao = this.currentFuncaoTransacao
    this.desconverterChips()
    this.router.navigate([routerNavigate], {
      state: {
        isEdit: this.isEdit,
        idAnalise: this.idAnalise,
        currentFuncaoTransacao: JSON.stringify(funcTransacao),
      }
    })
  }

  resultadoDeteccao() {
    var count = 1
    for (let item of this.routeState.visaopf.cenario.telasResult) {
      var aux: any[] = []
      for (var elem of item.componentes) {
        if (elem.tipo == "incluir" || elem.tipo == "exportar" || elem.tipo == "editar"
          || elem.tipo == "visualizar" || elem.tipo == "excluir") {
          aux.push({ tela: count, nome: elem.nome, tipo: elem.tipo })

        }
      }
      count++
      if (aux.length > 0) this.resultFuncaoTransacao.push(aux)
      console.log("resultTransação", this.resultFuncaoTransacao)
    }
    this.deteccaoDados()
    
  }

  deteccaoDados() {
    var id = this.getUrl(-2, true)
    this.vpfService.getTelaByAnaliseId(id).subscribe((telaDados: any) => {
      // console.log("telaDados",telaDados)
      var aux: any[] = []
      for (let i = 0; i < telaDados.length; i += 1) {
        if (telaDados[i].tipos == "Formulário") {
          aux.push(telaDados[i].componentes)
        }
      }
      this.resultFuncaoDados.push(aux)
    })

    return this.resultFuncaoDados
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

  carregarModuloSistema() {
    this.sistemaService.find(this.analise.sistema.id).subscribe((sistemaRecarregado: Sistema) => {
        this.modulosTeste = sistemaRecarregado.modulos;
        // this.analise.sistema = sistemaRecarregado;
        // this.analiseSharedDataService.analise.sistema = sistemaRecarregado;
    });
}

  editModulo() {
    console.log("Botão Editar modulo")

  }

  editFuncionalidade() {
    console.log("Botão Editar funcionalidade")

  }
  salvarDeteccao() {

    console.log("Botão salvar")
  }

  cancelarDeteccao() {
    console.log("Botão Cancelar")
  }
}


