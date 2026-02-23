import { Component, ViewChild, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConfirmationService } from 'primeng';

import { environment } from '../../../environments/environment';
import { Modulo } from '../modulo.model';
import { ModuloService } from '../modulo.service';
import { DatatableComponent, DatatableClickEvent } from '@nuvem/primeng-components';
import { PageConfigService } from 'src/app/shared/page-config.service';

@Component({
  selector: 'jhi-modulo',
  templateUrl: './modulo-list.component.html'
})
export class ModuloListComponent implements OnInit {

  rows = 20;
  rowsPerPageOptions: number[] = [5, 10, 20, 50, 100];
  filter: string;

  @ViewChild(DatatableComponent) datatable: DatatableComponent;

  searchUrl: string = this.moduloService.searchUrl;

  constructor(
    private router: Router,
    private moduloService: ModuloService,
    private confirmationService: ConfirmationService,
    private pageConfigService: PageConfigService
  ) { }

  ngOnInit() {
    // Log de inicialização
    console.log('[ModuloList] Inicializando componente');

    // Recuperar configuração de rows salva
    const savedRows = this.pageConfigService.getConfig('modulo_rows');
    console.log('[ModuloList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });

    // Validar e aplicar configuração de rows
    if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
      this.rows = savedRows;
      console.log('[ModuloList] Aplicando rows válido:', this.rows);
    } else if (savedRows) {
      console.warn(`[ModuloList] Valor inválido (${savedRows}) não está em rowsPerPageOptions. Usando padrão: ${this.rows}`);
      this.pageConfigService.saveConfig('modulo_rows', this.rows);
    } else {
      console.log('[ModuloList] Nenhuma config salva. Usando padrão:', this.rows);
    }

    // Recuperar filtro salvo
    const savedFilter = this.pageConfigService.getConfig('modulo_filter');
    if (savedFilter) {
      this.filter = savedFilter;
    }
  }

  onPageChange(event) {
    console.log('[ModuloList] Evento de mudança de página:', event);
    this.rows = event.rows;
    this.pageConfigService.saveConfig('modulo_rows', this.rows);
    console.log('[ModuloList] Nova configuração salva:', this.rows);
  }

  performSearch(value: string) {
    this.filter = value;
    this.pageConfigService.saveConfig('modulo_filter', this.filter);
    this.datatable.refresh(this.filter);
  }

  getLabel(label) {
    return label;
  }

  datatableClick(event: DatatableClickEvent) {
    if (!event.selection) {
      return;
    }
    switch (event.button) {
      case 'edit':
        this.router.navigate(['/modulo', event.selection.id, 'edit']);
        break;
      case 'delete':
        this.confirmDelete(event.selection.id);
        break;
      case 'view':
        this.router.navigate(['/modulo', event.selection.id]);
        break;
    }
  }

  confirmDelete(id: any) {
    this.confirmationService.confirm({
      message: this.getLabel('Tem certeza que deseja excluir o registro?'),
      accept: () => {
        this.moduloService.delete(id).subscribe(() => {
          this.datatable.refresh(undefined);
        });
      }
    });
  }
}
