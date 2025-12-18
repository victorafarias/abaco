import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthenticationService } from '@nuvem/angular-base';
import { MenuComponent, MenusService } from '@nuvem/primeng-components';
import { MenuItem, MenuItemContent } from 'primeng';
import { Organizacao, OrganizacaoService } from 'src/app/organizacao';
import { UploadService } from 'src/app/upload/upload.service';
import { UserService } from 'src/app/user';
import { AuthService } from 'src/app/util/auth.service';
import { AppComponent } from '../../app.component';
import { User } from '../../user/user.model';
import { NovidadeVersaoDTO } from '../novidades-versao/novidades-versao-dto';
import { NovidadesVersaoService } from '../novidades-versao/novidades-versao-service';



@Component({
	selector: 'app-topbar',
	templateUrl: './app.topbar.component.html',
	styleUrls: ['./app.topbar.component.css']
})

export class AppTopbarComponent implements OnInit {


	organizacao: Organizacao = new Organizacao();
	src: String = "";

	novidadesDaVersao: NovidadeVersaoDTO[] = [];
	novidadeVersao: NovidadeVersaoDTO = new NovidadeVersaoDTO();
	versaoAtual: string = "";
	mostrarNovidades: boolean = false;
	items: MenuItem[] = [];
	menuItens: MenuItem[] = [];
	mostrarDialogNovidadesVersao: boolean = false;

	canEditarConfiguracao: boolean = false;

	// Flag para indicar se a reindexação está em andamento
	isReindexando: boolean = false;

	@ViewChild("menuNV") menu: any;

	constructor(public app: AppComponent,
		private orgService: OrganizacaoService,
		private novidadesVersaoService: NovidadesVersaoService,
		private userService: UserService,
		private authService: AuthService,
		private uploadService: UploadService,
		private readonly _authentication: AuthenticationService<User>) {
	}

	ngOnInit(): void {
		this.setarLogoOrganizacao();
		if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "CONFIGURACAO_EDITAR") == true) {
			this.canEditarConfiguracao = true;
		}
	}

	public setarLogoOrganizacao() {
		if (this.isAuthenticated() == true && this._authentication.getUser() != null) {
			this.orgService.searchActiveOrganizations().subscribe(r => {
				if (r.length === 1) {
					let logo;
					if (r[0].logoId !== undefined && r[0].logoId != null) {
						this.uploadService.getLogo(r[0].logoId).subscribe(response => {
							logo = response.logo;
							this.src = "data:image/png;base64," + logo;
						});
					}
				}
			})
		}
	}

	get usuario() {
		return this._authentication.getUser();
	}

	isAuthenticated() {
		return this._authentication.isAuthenticated();
	}
	AuthenticationService() {
		return this._authentication.isAuthenticated();
	}
	authenticatedUserFirstName(): string {
		const storageUser = this._authentication.getUser();
		if (!storageUser) {
			return null;
		}

		return storageUser.firstName;
	}

	authenticatedUserFullName(): string {
		const storageUser = this._authentication.getUser();
		if (!storageUser) {
			return;
		}
		return storageUser.firstName + ' ' + storageUser.lastName;
	}

	mostrarNovidade() {
		this.visualizarNovidadesDaVersao();
	}

	visualizarNovidadesDaVersao() {
		this.userService.findCurrentUser().subscribe(u => { this.mostrarNovidades = !u.mostrarNovidades; })
		this.novidadesVersaoService.getAll().subscribe(response => {
			if (response.length > 0) {
				this.mostrarDialogNovidadesVersao = true;
				this.novidadesDaVersao = response;
				this.novidadeVersao = this.novidadesDaVersao[this.novidadesDaVersao.length - 1];
				this.versaoAtual = this.novidadeVersao.versao;

				this.menuItens = [];
				this.items = [];
				this.menu = new MenuComponent(new MenusService());
				this.novidadesDaVersao.forEach(nv => {
					this.menuItens.push(
						{
							label: nv.versao, icon: "pi pi-caret-right", command: () => {
								this.alterarConteudo(nv);
							}
						});
				})
				this.items = [{
					label: 'Versões',
					icon: 'pi pi-fw pi-spinner',
					items: this.menuItens
				}]
			}

		});
	}
	alterarConteudo(nv: NovidadeVersaoDTO) {
		this.novidadeVersao = nv;
	}

	verificarOpcaoMostrarNovidades() {
		if (this.mostrarNovidades === true) {
			this.novidadesVersaoService.desabilitarNovidadesUsuario().subscribe(r => { });
		} else {
			this.novidadesVersaoService.habilitarNovidadesUsuario().subscribe(r => { });
		}
	}

	reinit(event?) {
		let element = document.getElementById("menu-dialog");
		let menu = element.children[0];
		menu.setAttribute("style", "opacity:1!important");
	}

	/**
	 * Reindexar Todos
	 * Este método chama a API de reindexação do backend para todas as entidades
	 * validadas. A lista de indexadores foi testada e todos retornaram 200 OK.
	 */
	reindexarTodos(event?: Event): void {
		// Previne comportamento padrão do link
		if (event) {
			event.preventDefault();
		}

		// Ativa o loader
		this.isReindexando = true;

		console.log('🚀 Iniciando reindexação com a lista 100% validada...');

		// Lista de indexadores validados
		const lstIndexadores = 'ANALISE,CONTRATO,ESFORCO_FASE,FATOR_AJUSTE,FUNCAO_DADOS,FUNCAO_TRANSACAO,FUNCIONALIDADE,MANUAL,MANUAL_CONTRATO,MODULO,NOMENCLATURA,ORGANIZACAO,PERFIL,SISTEMA,STATUS,TIPO_EQUIPE,USER';

		// Obtém o token de autenticação do localStorage
		const authToken = localStorage.getItem('authenticationToken');

		fetch(
			'/api/reindexar?lstIndexadores=' + lstIndexadores,
			{
				method: 'GET',
				headers: {
					'Authorization': 'Bearer ' + authToken,
					'Content-Type': 'application/json'
				}
			}
		)
			.then(res => {
				// Desativa o loader
				this.isReindexando = false;

				// Quando o servidor responder, executa isso
				if (res.status === 200) {
					console.log('✅ Sucesso! Base reindexada com a lista final (Status: 200).', res);
					alert('✅ Reindexação concluída com sucesso!');
				} else {
					console.error('❌ Erro ao reindexar a lista final (Status: ' + res.status + ').', res);
					alert('❌ Erro ao reindexar. Status: ' + res.status);
				}
			})
			.catch(err => {
				// Desativa o loader em caso de erro
				this.isReindexando = false;

				// Se a conexão falhar
				console.error('❌ Erro de conexão ao tentar reindexar.', err);
				alert('❌ Erro de conexão ao tentar reindexar.');
			});
	}
}

