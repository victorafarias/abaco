import { Component, ViewChild, OnInit, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConfirmationService } from 'primeng';

import { DomSanitizer } from '@angular/platform-browser';
import { DatatableComponent, PageNotificationService, DatatableClickEvent } from '@nuvem/primeng-components';
import { Organizacao, SearchGroup } from '../organizacao.model';
import { ElasticQuery } from 'src/app/shared/elastic-query';
import { OrganizacaoService } from '../organizacao.service';
import { AuthService } from 'src/app/util/auth.service';
import { AuthenticationService } from '@nuvem/angular-base';
import { User } from 'src/app/user';
import { Perfil, PerfilService } from 'src/app/perfil';
import { PerfilOrganizacao } from 'src/app/perfil/perfil-organizacao.model';
import { PageConfigService } from 'src/app/shared/page-config.service';


@Component({
    selector: 'jhi-organizacao',
    templateUrl: './organizacao-list.component.html',
    providers: [ConfirmationService, OrganizacaoService]
})
export class OrganizacaoListComponent implements OnInit, AfterViewInit {

    @ViewChild(DatatableComponent) datatable: DatatableComponent;

    searchUrl: string = this.organizacaoService.searchUrl;

    organizacaoSelecionada: Organizacao;

    paginationParams = { contentIndex: null };

    elasticQuery: ElasticQuery = new ElasticQuery();

    rows = 20;
    rowsPerPageOptions: number[] = [5, 10, 20];

    organizacaoFiltro: SearchGroup;

    allColumnsTable = [
        { value: 'sigla', label: 'Sigla' },
        { value: 'nome', label: 'Nome' },
        { value: 'cnpj', label: 'CNPJ' },
        { value: 'numeroOcorrencia', label: 'Número da Ocorrência' },
        { value: 'ativo', label: 'Ativo' },
    ];

    columnsVisible = [
        'sigla',
        'nome',
        'cnpj',
        'numeroOcorrencia',
        'ativo'];
    private lastColumn: any[] = [];

    canPesquisar: boolean = false;
    canCadastrar: boolean = false;
    canEditar: boolean = false;
    canConsultar: boolean = false;
    canDeletar: boolean = false;

    perfisOrganizacao: PerfilOrganizacao[] = [];

    constructor(
        public _DomSanitizer: DomSanitizer,
        private router: Router,
        private organizacaoService: OrganizacaoService,
        private confirmationService: ConfirmationService,
        private pageNotificationService: PageNotificationService,
        private authService: AuthService,
        private perfilService: PerfilService,
        private pageConfigService: PageConfigService
    ) { }

    getLabel(label) {
        return label;
    }

    public ngOnInit() {
        // Log de inicialização
        console.log('[OrganizacaoList] Inicializando componente');

        // Recuperar configuração de rows salva
        const savedRows = this.pageConfigService.getConfig('organizacao_rows');
        console.log('[OrganizacaoList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });

        // Validar e aplicar configuração de rows
        if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
            this.rows = savedRows;
            console.log('[OrganizacaoList] Aplicando rows válido:', this.rows);
        } else if (savedRows) {
            console.warn(`[OrganizacaoList] Valor inválido (${savedRows}) não está em rowsPerPageOptions. Usando padrão: ${this.rows}`);
            this.pageConfigService.saveConfig('organizacao_rows', this.rows);
        } else {
            console.log('[OrganizacaoList] Nenhuma config salva. Usando padrão:', this.rows);
        }

        // Recuperar configuração de colunas visíveis
        const savedCols = this.pageConfigService.getConfig('organizacao_columnsVisible');
        if (savedCols) {
            this.columnsVisible = savedCols;
        } else {
            this.columnsVisible = this.allColumnsTable.map(c => c.value);
        }

        // Recuperar filtro salvo
        const savedFilter = this.pageConfigService.getConfig('organizacao_filter');
        if (savedFilter) {
            this.elasticQuery.value = savedFilter;
        }

        if (this.datatable) {
            this.datatable.pDatatableComponent.onRowSelect.subscribe((event) => {
                this.organizacaoSelecionada = event.data;
            });
            this.datatable.pDatatableComponent.onRowUnselect.subscribe((event) => {
                this.organizacaoSelecionada = undefined;
            });
        }
        this.organizacaoFiltro = new SearchGroup();
        this.verificarPermissoes();

        this.perfilService.getPerfilOrganizacaoByUser().subscribe(r => {
            this.perfisOrganizacao = r;
        })
    }

    ngAfterViewInit() {
        this.updateVisibleColumns(this.columnsVisible);
    }

    verificarPermissoes() {
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "ORGANIZACAO_EDITAR") == true) {
            this.canEditar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "ORGANIZACAO_CONSULTAR") == true) {
            this.canConsultar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "ORGANIZACAO_EXCLUIR") == true) {
            this.canDeletar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "ORGANIZACAO_PESQUISAR") == true) {
            this.canPesquisar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "ORGANIZACAO_CADASTRAR") == true) {
            this.canCadastrar = true;
        }
    }

    verificarBotoes(organizacao: Organizacao) {
        this.canEditar = PerfilService.consultarPerfilOrganizacao("ORGANIZACAO", "EDITAR", this.perfisOrganizacao, organizacao);
        this.canConsultar = PerfilService.consultarPerfilOrganizacao("ORGANIZACAO", "CONSULTAR", this.perfisOrganizacao, organizacao);
        this.canDeletar = PerfilService.consultarPerfilOrganizacao("ORGANIZACAO", "EXCLUIR", this.perfisOrganizacao, organizacao);
    }



    onClick(event: DatatableClickEvent) {
        if (!event.selection) {
            return;
        }

        switch (event.button) {
            case 'edit':
                this.router.navigate(['/organizacao', event.selection.id, 'edit']);
                break;
            case 'delete':
                this.confirmDelete(event.selection.id);
                break;
            case 'view':
                this.router.navigate(['/organizacao', event.selection.id]);
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
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "ORGANIZACAO_EDITAR") == false
            || PerfilService.consultarPerfilOrganizacao("ORGANIZACAO", "EDITAR", this.perfisOrganizacao, this.organizacaoSelecionada) == false) {
            return false;
        }
        this.router.navigate(['/organizacao', this.organizacaoSelecionada.id, 'edit']);
    }

    confirmDelete(id: any) {
        this.confirmationService.confirm({
            message: this.getLabel('Tem certeza que deseja excluir o registro?'),
            accept: () => {
                this.organizacaoService.delete(id).subscribe(() => {
                    this.pageNotificationService.addDeleteMsg();
                    this.recarregarDataTable();
                }, error => {
                    if (error.status === 500) {
                        this.pageNotificationService
                            .addErrorMessage(this.getLabel('Cadastros.Organizacao.Mensagens.msgOrganizacaoNaoPodeSerDeletadaPoisEstaAssociadaAContratoEquipeOuAnalise'));
                    }
                });
            }
        });
    }

    limparPesquisa() {
        this.elasticQuery.reset();
        this.pageConfigService.saveConfig('organizacao_filter', '');
        this.recarregarDataTable();
    }

    recarregarDataTable() {
        //Se descrição == CNPJ remove caracteres . - / para fazer pesquisa.
        if (this.elasticQuery.value.length == 18 && this.elasticQuery.value[2] == ".") {
            this.elasticQuery.value = this.elasticQuery.value.replace(".", "");
            this.elasticQuery.value = this.elasticQuery.value.replace(".", "");
            this.elasticQuery.value = this.elasticQuery.value.replace("/", "");
            this.elasticQuery.value = this.elasticQuery.value.replace("-", "");
        }
        this.pageConfigService.saveConfig('organizacao_filter', this.elasticQuery.value);
        this.datatable.refresh(this.elasticQuery.query);
        this.organizacaoFiltro.nome = this.elasticQuery.value;
    }
    public selectOrganizacao() {
        if (this.datatable && this.datatable.selectedRow) {
            if (this.datatable.selectedRow && this.datatable.selectedRow) {
                this.organizacaoSelecionada = this.datatable.selectedRow;
                this.verificarBotoes(this.organizacaoSelecionada);
            }
        }
    }

    mostrarColunas(event) {
        if (this.columnsVisible.length) {
            this.lastColumn = event.value;
            this.updateVisibleColumns(this.columnsVisible);
            this.pageConfigService.saveConfig('organizacao_columnsVisible', this.columnsVisible);
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
    criarOrganizacao() {
        this.router.navigate(["/organizacao/new"])
    }

    onPageChange(event) {
        console.log('[OrganizacaoList] Evento de mudança de página:', event);
        this.rows = event.rows;
        this.pageConfigService.saveConfig('organizacao_rows', this.rows);
        console.log('[OrganizacaoList] Nova configuração salva:', this.rows);
    }
}
