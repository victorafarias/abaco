import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { BlockUiService } from '@nuvem/angular-base';
import { DatatableClickEvent, PageNotificationService } from '@nuvem/primeng-components';
import { ConfirmationService, LazyLoadEvent } from 'primeng';
import { Table as DataTable } from 'primeng/table';
import { Subscription } from 'rxjs';
import { Analise, SearchGroup } from 'src/app/analise';
import { HistoricoDTO } from 'src/app/historico/historico.dto';
import { HistoricoService } from 'src/app/historico/historico.service';
import { Organizacao, OrganizacaoService } from 'src/app/organizacao';
import { PerfilOrganizacao } from 'src/app/perfil/perfil-organizacao.model';

import { PerfilService } from 'src/app/perfil/perfil.service';
import { PageConfigService } from 'src/app/shared/page-config.service';
import { Sistema, SistemaService } from 'src/app/sistema';
import { StatusService } from 'src/app/status';
import { Status } from 'src/app/status/status.model';
import { TipoEquipe, TipoEquipeService } from 'src/app/tipo-equipe';
import { User } from 'src/app/user';
import { AuthService } from 'src/app/util/auth.service';
import { Divergencia } from '../divergencia.model';
import { DivergenciaService } from '../divergencia.service';

@Component({
    selector: 'app-divergencia-list',
    templateUrl: './divergencia-list.component.html',
    providers: [ConfirmationService]
})
export class DivergenciaListComponent implements OnInit {


    @ViewChild(DataTable, { static: true }) datatable: DataTable;



    rows = 20;
    rowsPerPageOptions: number[] = [5, 10, 20, 50, 100];

    datasource: Analise[];
    event: LazyLoadEvent;
    lstDivergence;
    selectedDivergence;
    totalRecords;

    perfisOrganizacao: PerfilOrganizacao[] = [];

    cols: any[];

    loading: boolean;

    columnsVisible = [];

    customOptions: Object = {};

    userAnaliseUrl: string = this.divergenciaService.resourceUrl;

    analiseSelecionada: any[] = []
    analiseTableSelecionada: Divergencia = new Divergencia();
    searchDivergence: SearchGroup = new SearchGroup();
    nomeSistemas: Array<Sistema>;
    usuariosOptions: User[] = [];
    organizations: Array<Organizacao>;
    teams: TipoEquipe[];
    equipeShare;
    analiseTemp: Analise = new Analise();
    tipoEquipesLoggedUser: TipoEquipe[] = [];
    tipoEquipesToClone: TipoEquipe[] = [];
    query: String;
    usuarios: String[] = [];
    lstStatus: Status[] = [];
    lstStatusActive: Status[] = [];
    idDivergenceStatus: number;
    public statusToChange?: Status;
    public equipeToClone?: TipoEquipe;

    translateSusbscriptions: Subscription[] = [];

    metsContagens = [
        { label: 'Detalhada', value: 'DETALHADA' },
        { label: 'Indicativa', value: 'INDICATIVA' },
        { label: 'Estimada', value: 'ESTIMADA' }
    ];
    blocked;
    inicial: boolean;
    bloquear = [
        { titulo: 'Sim', valor: true },
        { titulo: 'Não', valor: false }
    ]
    showDialogAnaliseCloneTipoEquipe = false;
    showDialogAnaliseBlock = false;
    mostrarDialog = false;
    enableTable: Boolean = false;
    notLoadFilterTable = false;
    analisesList: any[] = [];
    isLoadFilter = true;
    showDialogDivergenceBlock = false;
    showDialogDivergenceStatus = false;


    canPesquisar: boolean = false;
    canEditar: boolean = false;
    canDeletar: boolean = false;
    canAlterarStatus: boolean = false;
    canBloquearDesbloquear: boolean = false;

    selectModeButtonsMultiple: boolean = false;

    constructor(
        private router: Router,
        private sistemaService: SistemaService,
        private tipoEquipeService: TipoEquipeService,
        private organizacaoService: OrganizacaoService,
        private pageNotificationService: PageNotificationService,
        private equipeService: TipoEquipeService,
        private blockUiService: BlockUiService,
        private statusService: StatusService,
        private confirmationService: ConfirmationService,
        private divergenciaService: DivergenciaService,
        private authService: AuthService,
        private historicoService: HistoricoService,
        private perfilService: PerfilService,
        private pageConfigService: PageConfigService
    ) {
    }

    public ngOnInit() {
        // Log de inicialização
        console.log('[DivergenciaList] Inicializando componente');

        // Recuperar configuração de rows salva
        const savedRows = this.pageConfigService.getConfig('divergencia_rows');
        console.log('[DivergenciaList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });

        // Validar e aplicar configuração de rows
        if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
            this.rows = savedRows;
            console.log('[DivergenciaList] Aplicando rows válido:', this.rows);
        } else if (savedRows) {
            console.warn(`[DivergenciaList] Valor inválido (${savedRows}) não está em rowsPerPageOptions. Usando padrão: ${this.rows}`);
            this.pageConfigService.saveConfig('divergencia_rows', this.rows);
        } else {
            console.log('[DivergenciaList] Nenhuma config salva. Usando padrão:', this.rows);
        }

        // Recuperar parâmetros de busca salvos
        const savedSearch = this.pageConfigService.getConfig('divergencia_searchGroup');
        if (savedSearch) {
            this.searchDivergence = savedSearch;
        }
        this.estadoInicial();
        this.datatable.onLazyLoad.subscribe((event: LazyLoadEvent) => this.loadDirvenceLazy(event));
        this.datatable.lazy = true;
        this.verificarPermissoes();
    }

    getLabel(label) {
        return label;
    }

    verificarPermissoes() {
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "VALIDACAO_EDITAR") == true) {
            this.canEditar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "VALIDACAO_EXCLUIR") == true) {
            this.canDeletar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "VALIDACAO_PESQUISAR") == true) {
            this.canPesquisar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "VALIDACAO_ALTERAR_STATUS") == true) {
            this.canAlterarStatus = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "VALIDACAO_BLOQUEAR_DESBLOQUEAR") == true) {
            this.canBloquearDesbloquear = true;
        }
    }

    verificarBotoes(analise: Analise) {
        this.canEditar = PerfilService.consultarPerfilAnalise("VALIDACAO", "EDITAR", this.perfisOrganizacao, analise);
        this.canDeletar = PerfilService.consultarPerfilAnalise("VALIDACAO", "EXCLUIR", this.perfisOrganizacao, analise);
        this.canAlterarStatus = PerfilService.consultarPerfilAnalise("VALIDACAO", "ALTERAR_STATUS", this.perfisOrganizacao, analise);
        this.canBloquearDesbloquear = PerfilService.consultarPerfilAnalise("VALIDACAO", "BLOQUEAR_DESBLOQUEAR", this.perfisOrganizacao, analise);
    }

    estadoInicial() {
        this.getEquipesFromActiveLoggedUser();
        this.recuperarOrganizacoes();
        this.recuperarEquipe();
        this.recuperarSistema();
        this.recuperarStatus();
        this.inicial = false;
    }

    getEquipesFromActiveLoggedUser() {
        this.equipeService.getEquipesActiveLoggedUser().subscribe(res => {
            this.tipoEquipesLoggedUser = res;
        });
    }

    recuperarOrganizacoes() {
        this.perfilService.getPerfilOrganizacaoByUser().subscribe(r => {
            this.perfisOrganizacao = r;
            let organizacoesPesquisar: Organizacao[] = [];
            this.organizacaoService.dropDown().subscribe(response => {
                response.forEach(organizacao => {
                    if (PerfilService.consultarPerfilOrganizacao("VALIDACAO", "PESQUISAR", this.perfisOrganizacao, organizacao) == true) {
                        organizacoesPesquisar.push(organizacao);
                    }
                })
                this.organizations = organizacoesPesquisar;
                this.customOptions['organizacao.nome'] = organizacoesPesquisar.map((item) => {
                    return { label: item.nome, value: item.id };
                });
            });
        })

    }

    recuperarSistema() {
        this.sistemaService.dropDown().subscribe(response => {
            this.nomeSistemas = response;
            this.customOptions['sistema.nome'] = response.map((item) => {
                return { label: item.nome, value: item.id };
            });
        });
    }

    recuperarEquipe() {
        this.tipoEquipeService.dropDown().subscribe(response => {
            this.teams = response;
            this.tipoEquipesToClone = response;
            const emptyTeam = new TipoEquipe();
            this.tipoEquipesToClone.unshift(emptyTeam);
        });
    }

    recuperarStatus() {
        this.statusService.list().subscribe(response => {
            this.lstStatus = response;
            const emptyStatus = new Status();
            this.lstStatus.unshift(emptyStatus);
        });
        this.statusService.listActive().subscribe(response => {
            this.lstStatusActive = response;
        });
    }

    public editDivergence(analiseDivergence: Analise) {
        if (!analiseDivergence) {
            this.pageNotificationService.addErrorMessage('Nenhuma Validação foi selecionada.');
            return;
        }
        if (analiseDivergence.bloqueiaAnalise) {
            this.pageNotificationService.addErrorMessage('Você não pode editar uma análise bloqueada!');
            return;
        }
        this.inserirHistoricoEditar(analiseDivergence);
        this.router.navigate(['/divergencia', analiseDivergence.id, 'edit']);
    }

    public confirmDeleteDivergence(divergence: Analise) {
        if (!divergence) {
            this.pageNotificationService.addErrorMessage('Nenhuma Validação foi selecionada.');
            return;
        } else {
            this.confirmationService.confirm({
                message: this.getLabel('Tem certeza que deseja excluir o registro ').concat(divergence.identificadorAnalise).concat(' ?'),
                accept: () => {
                    this.blockUiService.show();
                    this.divergenciaService.delete(divergence.id).subscribe(() => {
                        this.pageNotificationService.addDeleteMsg(divergence.identificadorAnalise);
                        this.blockUiService.hide();
                        this.performSearch();
                    });
                }
            });
        }
    }

    public changeUrl() {
        let querySearch = '&isDivergence=true';
        querySearch = querySearch.concat((this.searchDivergence.identificadorAnalise) ?
            `&identificador=*${this.searchDivergence.identificadorAnalise}*` : '');
        querySearch = querySearch.concat((this.searchDivergence.sistema && this.searchDivergence.sistema.id) ?
            `&sistema=${this.searchDivergence.sistema.id}` : '');
        querySearch = querySearch.concat((this.searchDivergence.organizacao && this.searchDivergence.organizacao.id) ?
            `&organizacao=${this.searchDivergence.organizacao.id}` : '');
        querySearch = querySearch.concat((this.searchDivergence.status && this.searchDivergence.status.id) ?
            `&status=${this.searchDivergence.status.id}` : '');
        querySearch = querySearch.concat((this.searchDivergence.bloqueiaAnalise && this.searchDivergence.bloqueiaAnalise.valor !== null) ?
            `&bloqueiaAnalise=${this.searchDivergence.bloqueiaAnalise.valor}` : '');
        return querySearch;
    }

    public limparPesquisa() {
        this.searchDivergence = new SearchGroup();
        this.pageConfigService.saveConfig('divergencia_searchGroup', this.searchDivergence);
        this.event.first = 0;
        this.loadDirvenceLazy(this.event);
    }

    public performSearch() {
        this.enableTable = true;
        this.pageConfigService.saveConfig('divergencia_searchGroup', this.searchDivergence);
        this.event.first = 0;
        this.loadDirvenceLazy(this.event);
    }

    public desabilitarBotaoRelatorio(): boolean {
        return !this.datatable;
    }

    loadDirvenceLazy(event: LazyLoadEvent) {
        this.blockUiService.show();
        this.event = event;
        this.divergenciaService.search(event, event.rows, false, this.changeUrl()).subscribe(response => {
            this.lstDivergence = response.body;
            this.datatable.totalRecords = parseInt(response.headers.get('x-total-count'), 10);
            this.blockUiService.hide();
        });
    }

    public onRowDblclick(event) {
        if (!this.canEditar) {
            return false;
        }
        if (event.target.nodeName === 'TD') {
            this.editDivergence(this.selectedDivergence[0]);
        } else if (event.target.parentNode.nodeName === 'TD') {
            this.editDivergence(this.selectedDivergence[0]);
        }
    }

    public datatableClick(event: DatatableClickEvent) {
        if (!event.selection) {
            return;
        } else if (event.selection.length === 1) {
            event.selection = event.selection[0];
        } else if (event.selection.length > 1 && event.button !== 'generateDivergence') {
            this.pageNotificationService.addErrorMessage('Selecione somente uma Análise para essa ação.');
            return;
        } else if (event.selection.length > 2) {
            this.pageNotificationService.addErrorMessage('Selecione somente duas Análises para gerar a Validação.');
            return;
        }
    }

    /**
    * funcionalidade para bloqueio e mudança de status
    */
    public confirmBlockDivegence(divergence: Analise[]) {
        if (this.selectedDivergence.length === 0) {
            this.pageNotificationService.addErrorMessage('Nenhuma Validação foi selecionada.');
            return;
        }

        if (this.selectedDivergence.some(a => a.bloqueiaAnalise)) {
            this.confirmationService.confirm({
                message: `Tem certeza que deseja desbloquear os registros selecionados?`,
                accept: () => {
                    this.selectedDivergence.forEach(a => this.desbloquearAnalise(a));
                }
            });
        } else {
            this.verificaStatusAprovado();
            this.changeStatusAndBlock();
        }
    }

    public verificaStatusAprovado() {
        let aprovada: Analise[] = this.selectedDivergence.filter(divergence => {
            return divergence.status.nome == "Aprovada"
        });
        if (aprovada.length == this.selectedDivergence.length) {
            this.statusToChange = aprovada[0].status;
        } else {
            this.statusToChange = null;
        }
    }

    public changeStatusAndBlock() {
        this.showDialogDivergenceBlock = true;
    }

    public desbloquearAnalise(divergencia: Analise) {
        this.divergenciaService.block(divergencia).subscribe(() => {
            this.mensagemAnaliseBloqueada(divergencia.bloqueiaAnalise, divergencia.identificadorAnalise);
            this.datatable._filter();
            this.datatable.selection = null;
            this.showDialogDivergenceBlock = false;
        });
    }

    public changeStatus(divergence: Analise) {
        this.statusToChange = divergence.status;
        this.idDivergenceStatus = divergence.id;
        this.showDialogDivergenceStatus = true;
    }

    public divergenceBlock() {
        this.selectedDivergence.forEach(divergencia => {
            this.bloqueiaDivegence(divergencia);
        })
    }

    public selectAnalise() {
        var values = this.datatable.value;
        let ind = 0;
        if (this.datatable && this.datatable.value) {
            this.inicial = true;
            if (this.datatable.value) {
                if (this.datatable?.selection) {
                    ind = values.indexOf(this.selectedDivergence[this.datatable?.selection?.length - 1])
                }
                if (this.datatable?.selection?.length > 1) {
                    this.selectModeButtonsMultiple = true;
                } else {
                    this.selectModeButtonsMultiple = false;
                }
                this.analiseSelecionada = this.datatable.value
                this.blocked = this.datatable?.value[ind]?.bloqueiaAnalise;

                if (this.selectedDivergence && this.selectedDivergence[0]) {
                    this.verificarBotoes(this.selectedDivergence[0]);
                }
            }
        }
    }

    public bloqueiaDivegence(divergencia: Analise) {
        this.analiseTemp = new Analise().copyFromJSON(divergencia);
        let canBlock = false;
        if (this.tipoEquipesLoggedUser) {
            this.tipoEquipesLoggedUser.forEach(equipe => {
                if (equipe.id === this.analiseTemp.equipeResponsavel.id) {
                    canBlock = true;
                }
            });
        }
        if (canBlock) {
            this.alterValidacaoStatusBlock(divergencia);
        } else {
            this.pageNotificationService.addErrorMessage(this.getLabel('Somente membros da equipe responsável podem bloquear esta análise!'));
        }
    }

    public alterValidacaoStatusBlock(divergencia: Analise) {
        if (divergencia && this.statusToChange) {
            this.divergenciaService.changeStatusDivergence(divergencia.id, this.statusToChange).subscribe(formulario => {
                this.idDivergenceStatus = undefined;
                this.statusToChange = undefined;
                const copy = formulario.analise;
                this.divergenciaService.block(copy).subscribe(() => {
                    const nome = copy.identificadorAnalise;
                    const bloqueado = copy.bloqueiaAnalise;
                    this.mensagemAnaliseBloqueada(bloqueado, nome);
                    this.datatable._filter();
                    this.datatable.selection = null;
                    this.showDialogDivergenceBlock = false;
                });
            },
                err => this.pageNotificationService.addErrorMessage('Não foi possível alterar o status da Validação.'));
        } else {
            this.pageNotificationService.addErrorMessage('Selecione um Status para continuar.');
        }
    }

    public alterStatusValidacao() {
        if (this.idDivergenceStatus && this.statusToChange) {
            this.divergenciaService.changeStatusDivergence(this.idDivergenceStatus, this.statusToChange).subscribe(() => {
                this.idDivergenceStatus = undefined;
                this.statusToChange = undefined;
                this.showDialogDivergenceStatus = false;
                this.datatable._filter();
            });
        }
        else {
            this.pageNotificationService.addErrorMessage('Selecione um Status para continuar.');
        }
    }

    private mensagemAnaliseBloqueada(retorno: boolean, nome: string) {
        if (retorno) {
            this.pageNotificationService.addSuccessMessage('Registro  desbloqueado com sucesso!');
        } else {
            this.pageNotificationService.addSuccessMessage('Registro bloqueado com sucesso!');
        }
    }

    viewDivergence(analise) {
        this.router.navigate(['/divergencia', analise.id, 'view']);
    }

    inserirHistoricoEditar(analiseSelecionada: Analise) {
        analiseSelecionada?.analisesComparadas.forEach(analise => {
            let historico: HistoricoDTO = new HistoricoDTO();
            historico.acao = "A validação " + analiseSelecionada.identificadorAnalise + " foi editada";
            historico.analise = analise;
            this.historicoService.inserirHistoricoAnalise(historico).subscribe(response => { });
        });
    }

    onPageChange(event) {
        console.log('[DivergenciaList] Evento de mudança de página:', event);
        this.rows = event.rows;
        this.pageConfigService.saveConfig('divergencia_rows', this.rows);
        console.log('[DivergenciaList] Nova configuração salva:', this.rows);
    }

    // mostrarColunas removed

}
