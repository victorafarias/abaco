import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '@nuvem/angular-base';
import { MenuComponent, MenusService, PageNotificationService } from '@nuvem/primeng-components';
import { MenuItem } from 'primeng';
import { Observable, Subscription } from 'rxjs';
import { environment } from 'src/environments/environment.prod';
import { NovidadeVersaoDTO } from '../components/novidades-versao/novidades-versao-dto';
import { NovidadesVersaoService } from '../components/novidades-versao/novidades-versao-service';
import { AppTopbarComponent } from '../components/topbar/app.topbar.component';
import { User, UserService } from '../user';
import { AuthService } from '../util/auth.service';

@Component({
	selector: 'app-dashboard',
	templateUrl: './dashboard.component.html',
	styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, AfterViewInit {

	username: string;
	password: string;

	novidadesDaVersao: NovidadeVersaoDTO[] = [];
	novidadeVersao: NovidadeVersaoDTO = new NovidadeVersaoDTO();
	versaoAtual: string = "";
	mostrarNovidades: boolean = false;
	items: MenuItem[] = [];
	mostrarDialogNovidadesVersao: boolean = false;

	authenticated = false;

	private routeSub: Subscription;
	@ViewChild(MenuComponent, { static: true }) menu: MenuComponent;


	constructor(
		private authService: AuthenticationService<User>,
		private http: HttpClient,
		private novidadesVersaoService: NovidadesVersaoService,
		private userService: UserService,
		private menuService: MenusService,
		private authAbacoService: AuthService,
		private router: Router
		) { }

		ngAfterViewInit(): void {
			this.carregarMenu();
		}

		getLabel(label) {
			return label;
		}

		ngOnInit() {
			this.visualizarNovidadesDaVersao();
		}

		carregarMenu() {
			this.authAbacoService.getRoles().subscribe(res => {
				this.authenticated = this.authService.isAuthenticated();
				if (!this.authenticated) {
					return this.router.navigate(["/login"]);
				}
				for (let index = 0; index < this.menuService.itens.length; index++) {
					const menu = this.menuService.itens[index];
					if (menu.label == 'Análise' && this.authAbacoService.possuiAlgumaRoles([AuthService.PREFIX_ROLE + 'ANALISE_ACESSAR',
					AuthService.PREFIX_ROLE + 'BASELINE_ACESSAR',
					AuthService.PREFIX_ROLE + 'VALIDACAO_ACESSAR'])) {
						menu.visible = true;
					} else if (menu.label == 'Cadastros' && this.authAbacoService.possuiAlgumaRoles([AuthService.PREFIX_ROLE + 'FASE_ACESSAR',
					AuthService.PREFIX_ROLE + 'MANUAL_ACESSAR',
					AuthService.PREFIX_ROLE + 'ORGANIZACAO_ACESSAR',
					AuthService.PREFIX_ROLE + 'SISTEMA_ACESSAR',
					AuthService.PREFIX_ROLE + 'TIPO_EQUIPE_ACESSAR',
					AuthService.PREFIX_ROLE + 'USUARIO_ACESSAR',
					AuthService.PREFIX_ROLE + 'STATUS_ACESSAR',
					AuthService.PREFIX_ROLE + 'NOMENCLATURA_ACESSAR',
					AuthService.PREFIX_ROLE + 'PERFIL_ACESSAR'])) {
						menu.visible = true;
					}
					for (let index = 0; index < menu.items.length; index++) {
						const submenu = menu.items[index];
						if (submenu.label == 'Fase') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'FASE_ACESSAR');
						} else if (submenu.label == 'Manual') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'MANUAL_ACESSAR');
						} else if (submenu.label == 'Organização') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'ORGANIZACAO_ACESSAR');
						} else if (submenu.label == 'Sistema') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'SISTEMA_ACESSAR');
						} else if (submenu.label == 'Tipo Equipe') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'TIPO_EQUIPE_ACESSAR');
						} else if (submenu.label == 'Usuários') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'USUARIO_ACESSAR');
						} else if (submenu.label == 'Status') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'STATUS_ACESSAR');
						} else if (submenu.label == 'Nomenclatura') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'NOMENCLATURA_ACESSAR');
						} else if (submenu.label == 'Perfil') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'PERFIL_ACESSAR');
						} else if (submenu.label == 'Análise') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'ANALISE_ACESSAR');
						} else if (submenu.label == 'Baseline') {
							submenu.visible = this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'BASELINE_ACESSAR');
						} else if (submenu.label == 'Validação' && this.authAbacoService.possuiRole(AuthService.PREFIX_ROLE + 'VALIDACAO_ACESSAR')) {
							submenu.visible = true;
						}
					}
				}
			});
		}

		ngOnDestroy() {
		}

		protected getUserDetails(): Observable<any> {
			return this.http.get<any>(`${environment.auth.detailsUrl}`);
		}

		authenticatedUserFullName(): string {
			const storageUser = this.authService.getUser();
			if (!storageUser) {
				return;
			}
			return storageUser.firstName + ' ' + storageUser.lastName;
		}


		visualizarNovidadesDaVersao() {
			this.userService.findCurrentUser().subscribe(u => {
				if(u.mostrarNovidades === true){
					this.mostrarNovidades = !u.mostrarNovidades;
					this.novidadesVersaoService.getAll().subscribe(response => {
						if(response.length > 0){
							this.mostrarDialogNovidadesVersao = true;
							this.novidadesDaVersao = response;
							this.novidadeVersao = this.novidadesDaVersao[this.novidadesDaVersao.length-1];
							this.versaoAtual = this.novidadeVersao.versao;

							let menuItens: MenuItem[] = [];
							this.novidadesDaVersao.forEach(nv => {
								menuItens.push(
									{label: nv.versao, icon: "pi pi-caret-right", command: () => {
										this.alterarConteudo(nv);
									}});
								})
								this.items = [{
									label: 'Versões',
									icon: 'pi pi-fw pi-spinner',
									items: menuItens}]
								}
							});
						}
					})

				}
				alterarConteudo(nv: NovidadeVersaoDTO) {
					this.novidadeVersao = nv;
				}

				verificarOpcaoMostrarNovidades(){
					if(this.mostrarNovidades === true){
						this.novidadesVersaoService.desabilitarNovidadesUsuario().subscribe(r=>{});
					}else{
						this.novidadesVersaoService.habilitarNovidadesUsuario().subscribe(r=>{});
					}
				}

			}
