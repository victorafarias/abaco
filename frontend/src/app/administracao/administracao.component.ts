import { Component, OnInit } from '@angular/core';
import {AdministracaoService} from "./administracao.service";
import {Rotina} from "./rotina.model";

@Component({
  selector: 'app-administracao',
  templateUrl: './administracao.component.html',
  styleUrls: ['./administracao.component.css']
})
export class AdministracaoComponent implements OnInit {

	rotinas: Rotina[] = [];

	constructor(private administracaoService: AdministracaoService) {
	}

	ngOnInit(): void {
		this.administracaoService.obterTodasRotinas().subscribe(rotinas => this.rotinas = rotinas);
	}
}
