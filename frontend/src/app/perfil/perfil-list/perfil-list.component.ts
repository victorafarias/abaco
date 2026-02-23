import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { DatatableClickEvent, DatatableComponent, PageNotificationService } from '@nuvem/primeng-components';
import { ConfirmationService } from 'primeng';
import { ElasticQuery } from 'src/app/shared/elastic-query';
import { AuthService } from 'src/app/util/auth.service';
import { Perfil } from '../perfil.model';
import { PerfilService } from '../perfil.service';
import { PageConfigService } from 'src/app/shared/page-config.service';

@Component({
    selector: 'app-perfil',
    templateUrl: './perfil-list.component.html',
})
export class PerfilListComponent implements OnInit {

    @ViewChild(DatatableComponent) datatable: DatatableComponent;

    elasticQuery: ElasticQuery = new ElasticQuery();
    searchUrl: string = this.perfilService.searchUrl;

    perfilSelecionado: Perfil;
    perfil: Perfil;

    rows = 20;
    rowsPerPageOptions: number[] = [5, 10, 20];

    canPesquisar: boolean = false;
    canCadastrar: boolean = false;
    canEditar: boolean = false;
    canConsultar: boolean = false;
    canDeletar: boolean = false;

    constructor(
        private perfilService: PerfilService,
        private router: Router,
        private confirmationService: ConfirmationService,
        private pageNotificationService: PageNotificationService,
        public authService: AuthService,
        private pageConfigService: PageConfigService
    ) {
    }

    public ngOnInit() {
        // Log de inicialização
        console.log('[PerfilList] Inicializando componente');

        // Recuperar configuração de rows salva
        const savedRows = this.pageConfigService.getConfig('perfil_rows');
        console.log('[PerfilList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });

        // Validar e aplicar configuração de rows
        if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
            this.rows = savedRows;
            console.log('[PerfilList] Aplicando rows válido:', this.rows);
        } else if (savedRows) {
            console.warn(`[PerfilList] Valor inválido (${savedRows}) não está em rowsPerPageOptions. Usando padrão: ${this.rows}`);
            this.pageConfigService.saveConfig('perfil_rows', this.rows);
        } else {
            console.log('[PerfilList] Nenhuma config salva. Usando padrão:', this.rows);
        }

        // Recuperar filtro salvo
        const savedFilter = this.pageConfigService.getConfig('perfil_filter');
        if (savedFilter) {
            this.elasticQuery.value = savedFilter;
        }
        this.verificarPermissoes();
    }

    verificarPermissoes() {
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "PERFIL_PESQUISAR") == true) {
            this.canPesquisar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "PERFIL_EDITAR") == true) {
            this.canEditar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "PERFIL_CONSULTAR") == true) {
            this.canConsultar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "PERFIL_EXCLUIR") == true) {
            this.canDeletar = true;
        }
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "PERFIL_CADASTRAR") == true) {
            this.canCadastrar = true;
        }
    }

    public recarregarDataTable() {
        this.pageConfigService.saveConfig('perfil_filter', this.elasticQuery.value);
        this.datatable.refresh(this.elasticQuery.query);
    }

    public limparPesquisa() {
        this.elasticQuery.reset();
        this.pageConfigService.saveConfig('perfil_filter', '');
        this.recarregarDataTable();
    }

    public onRowDblclick(event) {
        if (this.authService.possuiRole(AuthService.PREFIX_ROLE + "PERFIL_EDITAR") == false) {
            return false;
        }
        if (event.target.nodeName === 'TD') {
            this.abrirEditar(this.perfilSelecionado);
        } else if (event.target.parentNode.nodeName === 'TD') {
            this.abrirEditar(this.perfilSelecionado);
        }
    }

    abrirEditar(perfil: Perfil) {
        this.router.navigate(['/perfil', perfil.id, 'edit']);
    }

    abrirVisualizar(perfil: Perfil) {
        this.router.navigate(['/perfil', perfil.id, 'view']);
    }

    public selectPerfil() {
        if (this.datatable && this.datatable.selectedRow) {
            if (this.datatable.selectedRow && this.datatable.selectedRow) {
                this.perfilSelecionado = this.datatable.selectedRow;
            }
        }
    }

    onClick(event: DatatableClickEvent) {
        if (!event.selection) {
            return;
        }
        switch (event.button) {
            case 'edit': {
                this.abrirEditar(event.selection);
                break;
            }
            case 'view': {
                this.abrirVisualizar(event.selection);
                break;
            }
            case 'delete': {
                this.confirmDelete(event.selection);
                break;
            }
            default: {
                break;
            }
        }
    }

    public criarPerfil() {
        this.router.navigate(["/perfil/new"])
    }

    public confirmDelete(perfil: Perfil) {
        this.confirmationService.confirm({
            message: 'Tem certeza que deseja excluir o registro?',
            accept: () => {
                this.perfilService.delete(perfil.id).subscribe((response) => {
                    this.recarregarDataTable();
                    this.pageNotificationService.addSuccessMessage('Registro excluído com sucesso!');

                });
            }
        });
    }

    onPageChange(event) {
        console.log('[PerfilList] Evento de mudança de página:', event);
        this.rows = event.rows;
        this.pageConfigService.saveConfig('perfil_rows', this.rows);
        console.log('[PerfilList] Nova configuração salva:', this.rows);
    }
}
