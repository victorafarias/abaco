import { Component, ViewChild, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConfirmationService } from 'primeng';
import { DatatableComponent, DatatableClickEvent } from '@nuvem/primeng-components';
import { ContratoService } from '../contrato.service';
import { PageConfigService } from 'src/app/shared/page-config.service';


@Component({
  selector: 'jhi-contrato',
  templateUrl: './contrato-list.component.html'
})
export class ContratoListComponent implements OnInit {

  @ViewChild(DatatableComponent) datatable: DatatableComponent;

  rowsPerPageOptions: number[] = [5, 10, 20, 50, 100];
  rows = 20;
  filter: string;

  searchUrl: string = this.contratoService.searchUrl;

  constructor(
    private router: Router,
    private contratoService: ContratoService,
    private confirmationService: ConfirmationService,
    private pageConfigService: PageConfigService
  ) { }

  ngOnInit() {
    // Log de inicialização
    console.log('[ContratoList] Inicializando componente');

    // Recuperar configuração de rows salva
    const savedRows = this.pageConfigService.getConfig('contrato_rows');
    console.log('[ContratoList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });

    // Validar e aplicar configuração de rows
    if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
      this.rows = savedRows;
      console.log('[ContratoList] Aplicando rows válido:', this.rows);
    } else if (savedRows) {
      console.warn(`[ContratoList] Valor inválido (${savedRows}) não está em rowsPerPageOptions. Usando padrão: ${this.rows}`);
      this.pageConfigService.saveConfig('contrato_rows', this.rows);
    } else {
      console.log('[ContratoList] Nenhuma config salva. Usando padrão:', this.rows);
    }

    // Recuperar filtro salvo
    const savedFilter = this.pageConfigService.getConfig('contrato_filter');
    if (savedFilter) {
      this.filter = savedFilter;
    }
  }

  performSearch(value: string) {
    this.filter = value;
    this.pageConfigService.saveConfig('contrato_filter', this.filter);
    this.datatable.refresh(this.filter);
  }

  onPageChange(event) {
    console.log('[ContratoList] Evento de mudança de página:', event);
    this.rows = event.rows;
    this.pageConfigService.saveConfig('contrato_rows', this.rows);
    console.log('[ContratoList] Nova configuração salva:', this.rows);
  }

  getLabel(label) {
    let str: any;
    return str;
  }

  datatableClick(event: DatatableClickEvent) {
    if (!event.selection) {
      return;
    }
    switch (event.button) {
      case 'edit':
        this.router.navigate(['/contrato', event.selection.id, 'edit']);
        break;
      case 'delete':
        this.confirmDelete(event.selection.id);
        break;
      case 'view':
        this.router.navigate(['/contrato', event.selection.id]);
        break;
    }
  }

  confirmDelete(id: any) {
    this.confirmationService.confirm({
      message: 'Tem certeza que deseja excluir o registro?',
      accept: () => {
        this.contratoService.delete(id).subscribe(() => {
          this.datatable.refresh(undefined);
        });
      }
    });
  }
}
