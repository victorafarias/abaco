import { Component, OnInit } from '@angular/core';
import { ConfiguracaoService } from './configuracao.service';
import { Configuracao } from './model/configuracao.model';

@Component({
  selector: 'app-configuracao',
  templateUrl: './configuracao.component.html',
  styleUrls: ['./configuracao.component.css']
})
export class ConfiguracaoComponent implements OnInit {

	configuracao: Configuracao = new Configuracao();

  	constructor(private configuracaoService: ConfiguracaoService) {

    }

	ngOnInit(): void {
		this.carregarConfiguracao();
	}

	alterarHabilitarCamposFuncao(habilitar: boolean){
		this.configuracao.habilitarCamposFuncao = habilitar;
		this.configuracaoService.salvarConfiguracao(this.configuracao).subscribe(() => {this.carregarConfiguracao()});
	}

	carregarConfiguracao(){
		this.configuracaoService.buscarConfiguracao().subscribe(config => {
			this.configuracao = config;
		})
	}


}
