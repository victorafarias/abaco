import { Component, ViewChild, OnInit, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConfirmationService } from 'primeng';
import { DatatableComponent, PageNotificationService, DatatableClickEvent } from '@nuvem/primeng-components';
import { Organizacao } from 'src/app/organizacao/organizacao.model';
import { OrganizacaoService } from 'src/app/organizacao/organizacao.service';
import { TipoEquipe } from 'src/app/tipo-equipe/tipo-equipe.model';
import { TipoEquipeService } from 'src/app/tipo-equipe/tipo-equipe.service';
import { UserService } from '../user.service';
import { User } from '../user.model';
import { SearchGroup } from '../user.model';
import { AuthService } from 'src/app/util/auth.service';
import { PerfilService } from 'src/app/perfil/perfil.service';
import { PageConfigService } from 'src/app/shared/page-config.service';

@Component({
    selector: 'app-user',
    templateUrl: './user-list.component.html',
    providers: [ConfirmationService]
})
export class UserListComponent implements OnInit, AfterViewInit {

    @ViewChild(DatatableComponent) datatable: DatatableComponent;

    searchUrl: string = this.userService.searchUrl;

    usuarioSelecionado: User;

    rowsPerPageOptions: number[] = [5, 10, 20];
    rows = 20;

    customOptions: Object = {};

    userFiltro: SearchGroup;

    searchParams: any = {
        fullName: undefined,
        login: undefined,
        email: undefined,
        organization: undefined,
        profile: undefined,
        team: undefined,
    };
    query: string;
    organizations: Array<Organizacao>;
    teams: TipoEquipe[];

    allColumnsTable = [
        { value: 'nome', label: 'Nome' },
        { value: 'login', label: 'Login' },
        { value: 'organizacao', label: 'Organização' },
        { value: 'perfil', label: 'Perfil' },
        { value: 'equipe', label: 'Equipe' },
        { value: 'activated', label: 'Ativo' },
    ];

    columnsVisible = [
        'nome',
        'login',
        'organizacao',
        'perfil',
        'equipe',
        'activated',];
    private lastColumn: any[] = [];

    canCadastrar: boolean = false;
    canEditar: boolean = false;
    canConsultar: boolean = false;
    canDeletar: boolean = false;
    canPesquisar: boolean = false;

    constructor(
        private router: Router,
        private userService: UserService,
        private confirmationService: ConfirmationService,
        private organizacaoService: OrganizacaoService,
        private tipoEquipeService: TipoEquipeService,
        private pageNotificationService: PageNotificationService,
        private authService: AuthService,
        private perfilService: PerfilService,
        private pageConfigService: PageConfigService
    ) {
    }

    getLabel(label) {
        return label;
    }

    ngOnInit() {
        // Log de inicialização
        console.log('[UserList] Inicializando componente');

        // Recuperar configuração de rows salva
        const savedRows = this.pageConfigService.getConfig('user_rows');
        console.log('[UserList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });

        // Validar e aplicar configuração de rows
        if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
            this.rows = savedRows;
            console.log('[UserList] Aplicando rows válido:', this.rows);
        } else if (savedRows) {
            console.warn(`[UserList] Valor inválido (${savedRows}) não está em rowsPerPageOptions. Usando padrão: ${this.rows}`);
            this.pageConfigService.saveConfig('user_rows', this.rows);
        } else {
            console.log('[UserList] Nenhuma config salva. Usando padrão:', this.rows);
        }

        // Recuperar configuração de colunas visíveis
        const savedCols = this.pageConfigService.getConfig('user_columnsVisible');
        if (savedCols) {
            this.columnsVisible = savedCols;
        } else {
            this.columnsVisible = this.allColumnsTable.map(c => c.value);
        }

        this.recuperarOrganizacoes();
        this.recuperarEquipe();
        this.recuperarPerfis();
        this.query = this.changeUrl();
        if (this.datatable) {
            this.datatable.pDatatableComponent.onRowSelect.subscribe((event) => {
                this.usuarioSelecionado = event.data;
            });
            this.datatable.pDatatableComponent.onRowUnselect.subscribe((event) => {
                this.usuarioSelecionado = undefined;
            });
        }

        // Recuperar parâmetros de busca salvos
        const savedSearch = this.pageConfigService.getConfig('user_searchParams');
        if (savedSearch) {
            this.searchParams = savedSearch;
        }
        this.userFiltro = new SearchGroup();
        this.userFiltro.columnsVisible = this.columnsVisible;

        this.verificarPermissoes();
    }

    ngAfterViewInit() {
        this.updateVisibleColumns(this.columnsVisible);
    }

    verificarPermissoes() {
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "USUARIO_EDITAR") == true) {
            this.canEditar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "USUARIO_CONSULTAR") == true) {
            this.canConsultar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "USUARIO_EXCLUIR") == true) {
            this.canDeletar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "USUARIO_CADASTRAR") == true) {
            this.canCadastrar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "USUARIO_PESQUISAR") == true) {
            this.canPesquisar = true;
        }
    }

    recuperarOrganizacoes() {
        this.organizacaoService.dropDown().subscribe(response => {
            this.organizations = response;
            this.customOptions['organizacao'] = response.map((item) => {
                return { label: item.nome, value: item.id };
            });
        });
    }

    recuperarPerfis() {
        this.perfilService.dropDown().subscribe(response => {
            this.customOptions['perfil'] = response.map((item) => {
                return { label: item.nome, value: item.id };
            });
        });
    }

    recuperarEquipe() {
        this.tipoEquipeService.dropDown().subscribe(response => {
            this.teams = response;
            const emptyTeam = new TipoEquipe();
            this.customOptions['equipe'] = response.map((item) => {
                return { label: item.nome, value: item.id };
            });
        });
    }

    datatableClick(event: DatatableClickEvent) {
        if (!event.selection) {
            return;
        }
        switch (event.button) {
            case 'edit':
                this.router.navigate(['/admin/user', event.selection.id, 'edit']);
                break;
            case 'delete':
                this.confirmDelete(event.selection);
                break;
            case 'view':
                this.router.navigate(['/admin/user', event.selection.id]);
                break;
        }
    }

    public onRowDblclick(event) {
        if (event.target.nodeName === 'TD') {
            this.abrirEditar();
        } else if (event.target.parentNode.nodeName === 'TD') {
            this.abrirEditar();
        }
    }

    abrirEditar() {
        if (!this.canEditar) {
            return false;
        }
        const id = this.usuarioSelecionado.id;
        if (id > 0) {
            this.router.navigate(['/admin/user', id, 'edit']);
        }
    }

    confirmDelete(user: User) {
        this.confirmationService.confirm({
            message: this.getLabel('Tem certeza que deseja excluir o registro?'),
            accept: () => {
                this.userService.delete(user).subscribe(() => {
                    this.datatable.refresh(this.query);
                    this.pageNotificationService.addDeleteMsg();
                }, (error: Response) => {
                    if (error.status === 400) {
                        const errorType: string = error.headers['x-abacoapp-error'][0];
                        switch (errorType) {
                            case 'error.userexists': {
                                this.pageNotificationService.addErrorMessage(this.getLabel('Cadastros.Usuarios.Mensagens.msgVoceNaoPodeExcluirAdministrador'));
                                break;
                            }
                            case 'error.analiseexists': {
                                this.pageNotificationService.addErrorMessage(this.getLabel('Cadastros.Usuarios.Mensagens.msgVoceNaoPodeExcluirUsuarioEleDonoAnalise'));
                                break;
                            }
                        }
                    }
                });
            }
        });
    }
    performSearch() {
        this.pageConfigService.saveConfig('user_searchParams', this.searchParams);
        this.query = this.changeUrl();
        this.recarregarDataTable();
    }

    limparPesquisa() {
        this.searchParams = {
            fullName: undefined,
            login: undefined,
            email: undefined,
            organization: undefined,
            profile: undefined,
            team: undefined,
        };
        this.pageConfigService.saveConfig('user_searchParams', this.searchParams);
        this.recarregarDataTable();
    }

    recarregarDataTable() {
        this.datatable.url = this.changeUrl();
        this.datatable.reset();
    }

    public preencheFiltro() {
        if (this.datatable.filterParams.nome) {
            this.userFiltro.nome = this.datatable.filterParams.nome;
        }
        if (this.datatable.filterParams.login) {
            this.userFiltro.login = this.datatable.filterParams.login;
        }
        if (this.datatable.filterParams.email) {
            this.userFiltro.email = this.datatable.filterParams.email;
        }
        if (this.datatable.filterParams.organizacao) {
            this.userFiltro.organizacao = this.datatable.filterParams.organizacao;
        }
        if (this.datatable.filterParams.perfil) {
            this.userFiltro.perfil = this.datatable.filterParams.perfil;
        }
        if (this.datatable.filterParams.equipe) {
            this.userFiltro.tipoEquipe = this.datatable.filterParams.equipe;
        }
    }

    public changeUrl() {

        let querySearch = '?nome=';
        querySearch = querySearch.concat((this.searchParams.fullName) ? `*${this.searchParams.fullName}*` : '');

        querySearch = querySearch.concat((this.searchParams.login) ? `&login=*${this.searchParams.login}*` : '');

        querySearch = querySearch.concat((this.searchParams.email) ? `&email=*${this.searchParams.email}*` : '');

        querySearch = querySearch.concat((this.searchParams.organizacao && this.searchParams.organizacao.id) ?
            `&organizacao=${this.searchParams.organizacao.id}` : '');

        querySearch = querySearch.concat((this.searchParams.profile && this.searchParams.profile.name) ?
            `&perfil=${this.searchParams.profile.name}` : '');

        querySearch = querySearch.concat((this.searchParams.team && this.searchParams.team.id) ?
            `&equipe=${this.searchParams.team.id}` : '');

        querySearch = (querySearch === '?') ? '' : querySearch;

        querySearch = (querySearch.endsWith('&')) ? querySearch.slice(0, -1) : querySearch;

        return this.userService.searchUrl + querySearch;
    }
    setParamsLoad() {
        this.datatable.pDatatableComponent.onRowSelect.subscribe((event) => {
            this.usuarioSelecionado = event.data;
        });
        this.preencheFiltro();
    }

    mostrarColunas(event) {
        if (this.columnsVisible.length) {
            this.lastColumn = event.value;
            this.updateVisibleColumns(this.columnsVisible);
            this.pageConfigService.saveConfig('user_columnsVisible', this.columnsVisible);
        } else {
            this.lastColumn.map((item) => this.columnsVisible.push(item));
            this.pageNotificationService.addErrorMessage('Não é possível exibir menos de uma coluna');
        }
    }

    updateVisibleColumns(columns) {
        this.allColumnsTable.forEach(col => {
            if (this.visibleColumnCheck(col.value, columns)) {
                this.datatable.visibleColumns[col.value] = 'table-cell';
            } else {
                this.datatable.visibleColumns[col.value] = 'none';
            }
        });
    }

    visibleColumnCheck(column: string, visibleColumns: any[]) {
        return visibleColumns.some((item: any) => {
            return (item) ? item === column : true;
        });
    }

    criarUsuario() {
        this.router.navigate(["/admin/user/new"])
    }

    onPageChange(event) {
        console.log('[UserList] Evento de mudança de página:', event);
        this.rows = event.rows;
        this.pageConfigService.saveConfig('user_rows', this.rows);
        console.log('[UserList] Nova configuração salva:', this.rows);
    }
}
