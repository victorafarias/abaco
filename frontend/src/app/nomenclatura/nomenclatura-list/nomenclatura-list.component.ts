import { Component, OnInit, ViewChild } from '@angular/core';
import { DatatableComponent, PageNotificationService, DatatableClickEvent } from '@nuvem/primeng-components';
import { ElasticQuery } from 'src/app/shared/elastic-query';
import { Router } from '@angular/router';
import { ConfirmationService } from 'primeng';
import { NomenclaturaService } from '../nomenclatura.service';
import { Nomenclatura, SearchGroup } from '../nomenclatura.model';
import { PageConfigService } from 'src/app/shared/page-config.service';
import { AuthService } from 'src/app/util/auth.service';

@Component({
    selector: 'app-nomenclatura-list',
    templateUrl: './nomenclatura-list.component.html'
})
export class NomenclaturaListComponent implements OnInit {


    @ViewChild(DatatableComponent) datatable: DatatableComponent;

    searchUrl: string = this.nomenclaturaService.searchUrl;

    paginationParams = { contentIndex: null };

    elasticQuery: ElasticQuery = new ElasticQuery();

    rows = 20;
    rowsPerPageOptions: number[] = [5, 10, 20];

    valueFiltroCampo: string;

    nomenclaturaSelecionada: Nomenclatura;

    nomenclaturaFiltro: SearchGroup;

    canPesquisar: boolean = false;
    canCadastrar: boolean = false;
    canEditar: boolean = false;
    canConsultar: boolean = false;
    canDeletar: boolean = false;

    constructor(
        private router: Router,
        private nomenclaturaService: NomenclaturaService,
        private confirmationService: ConfirmationService,
        private pageNotificationService: PageNotificationService,
        private authService: AuthService,
        private pageConfigService: PageConfigService
    ) { }

    getLabel(label) {
        return label;
    }

    valueFiltro(valuefiltro: string) {
        this.valueFiltroCampo = valuefiltro;
        this.datatable.refresh(valuefiltro);
    }

    public ngOnInit() {
        // Log de inicialização
        console.log('[NomenclaturaList] Inicializando componente');

        // Recuperar configuração de rows salva
        const savedRows = this.pageConfigService.getConfig('nomenclatura_rows');
        console.log('[NomenclaturaList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });

        // Validar e aplicar configuração de rows
        if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
            this.rows = savedRows;
            console.log('[NomenclaturaList] Aplicando rows válido:', this.rows);
        } else if (savedRows) {
            console.warn(`[NomenclaturaList] Valor inválido (${savedRows}) não está em rowsPerPageOptions. Usando padrão: ${this.rows}`);
            this.pageConfigService.saveConfig('nomenclatura_rows', this.rows);
        } else {
            console.log('[NomenclaturaList] Nenhuma config salva. Usando padrão:', this.rows);
        }

        // Recuperar filtro salvo
        const savedFilter = this.pageConfigService.getConfig('nomenclatura_filter');
        if (savedFilter) {
            this.elasticQuery.value = savedFilter;
        }
        if (this.datatable) {
            this.datatable.pDatatableComponent.onRowSelect.subscribe((event) => {
                this.nomenclaturaSelecionada = event.data;
            });
            this.datatable.pDatatableComponent.onRowUnselect.subscribe((event) => {
                this.nomenclaturaSelecionada = undefined;
            });
        }
        this.nomenclaturaFiltro = new SearchGroup();
        this.verificarPermissoes();
    }

    verificarPermissoes() {
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "NOMENCLATURA_EDITAR") == true) {
            this.canEditar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "NOMENCLATURA_EXCLUIR") == true) {
            this.canDeletar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "NOMENCLATURA_CONSULTAR") == true) {
            this.canConsultar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "NOMENCLATURA_PESQUISAR") == true) {
            this.canPesquisar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "NOMENCLATURA_CADASTRAR") == true) {
            this.canCadastrar = true;
        }
    }

    public datatableClick(event: DatatableClickEvent) {
        if (!event.selection) {
            return;
        }
        switch (event.button) {
            case 'edit':
                this.router.navigate(['/nomenclatura', event.selection.id, 'edit']);
                break;
            case 'delete':
                this.confirmDelete(event.selection.id);
                break;
            case 'view':
                this.router.navigate(['/nomenclatura', event.selection.id, 'view']);
                break;
        }
    }

    public onRowDblclick(event) {
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "NOMENCLATURA_EDITAR") == false) {
            return false;
        }
        if (event.target.nodeName === 'TD') {
            this.abrirEditar();
        } else if (event.target.parentNode.nodeName === 'TD') {
            this.abrirEditar();
        }
    }

    abrirEditar() {
        this.router.navigate(['/nomenclatura', this.nomenclaturaSelecionada.id, 'edit']);
    }

    public confirmDelete(id: any) {
        this.confirmationService.confirm({
            message: this.getLabel('Tem certeza que deseja excluir o registro?'),
            accept: () => {
                this.nomenclaturaService.delete(id).subscribe(() => {
                    this.recarregarDataTable();
                    this.pageNotificationService.addDeleteMsg();
                }, error => {
                    if (error.status === 403) {
                        this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
                    }
                    if (error.status === 500) {
                        this.pageNotificationService.addErrorMessage(this.getLabel('Falha ao excluir registro, verifique se a equipe não está vinculada a algum usuário!'));
                    }
                });
            }
        });
    }

    public limparPesquisa() {
        this.elasticQuery.reset();
        this.pageConfigService.saveConfig('nomenclatura_filter', '');
        this.recarregarDataTable();
    }

    public recarregarDataTable() {
        this.pageConfigService.saveConfig('nomenclatura_filter', this.elasticQuery.value);
        this.datatable.refresh(this.elasticQuery.query);
        this.nomenclaturaFiltro.nome = this.elasticQuery.query;
    }

    public selectNomenclatura() {
        if (this.datatable && this.datatable.selectedRow) {
            if (this.datatable.selectedRow && this.datatable.selectedRow) {
                this.nomenclaturaSelecionada = this.datatable.selectedRow;
            }
        }
    }

    criarNomenclatura() {
        this.router.navigate(["/nomenclatura/new"])
    }

    onPageChange(event) {
        console.log('[NomenclaturaList] Evento de mudança de página:', event);
        this.rows = event.rows;
        this.pageConfigService.saveConfig('nomenclatura_rows', this.rows);
        console.log('[NomenclaturaList] Nova configuração salva:', this.rows);
    }
}
