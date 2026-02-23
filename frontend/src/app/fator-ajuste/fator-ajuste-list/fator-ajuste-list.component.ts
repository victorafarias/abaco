import { Component, ViewChild, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { DatatableClickEvent, DatatableComponent } from '@nuvem/primeng-components';
import { ConfirmationService } from 'primeng';
import { FatorAjusteService } from '../fator-ajuste.service';
import { PageConfigService } from 'src/app/shared/page-config.service';


@Component({
  selector: 'app-fator-ajuste-list',
  templateUrl: './fator-ajuste-list.component.html'
})
export class FatorAjusteListComponent implements OnInit {

  rows = 20;
  rowsPerPageOptions: number[] = [5, 10, 20, 50, 100];
  filter: string;

  @ViewChild(DatatableComponent) datatable: DatatableComponent;

  searchUrl: string = this.fatorAjusteService.searchUrl;

  constructor(
    private router: Router,
    private fatorAjusteService: FatorAjusteService,
    private confirmationService: ConfirmationService,
    private pageConfigService: PageConfigService
  ) { }

  ngOnInit() {
    // Log de inicialização
    console.log('[FatorAjusteList] Inicializando componente');

    // Recuperar configuração de rows salva
    const savedRows = this.pageConfigService.getConfig('fator_ajuste_rows');
    console.log('[FatorAjusteList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });

    // Validar e aplicar configuração de rows
    if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
      this.rows = savedRows;
      console.log('[FatorAjusteList] Aplicando rows válido:', this.rows);
    } else if (savedRows) {
      console.warn(`[FatorAjusteList] Valor inválido (${savedRows}) não está em rowsPerPageOptions. Usando padrão: ${this.rows}`);
      this.pageConfigService.saveConfig('fator_ajuste_rows', this.rows);
    } else {
      console.log('[FatorAjusteList] Nenhuma config salva. Usando padrão:', this.rows);
    }

    // Recuperar filtro salvo
    const savedFilter = this.pageConfigService.getConfig('fator_ajuste_filter');
    if (savedFilter) {
      this.filter = savedFilter;
    }
  }

  onPageChange(event) {
    console.log('[FatorAjusteList] Evento de mudança de página:', event);
    this.rows = event.rows;
    this.pageConfigService.saveConfig('fator_ajuste_rows', this.rows);
    console.log('[FatorAjusteList] Nova configuração salva:', this.rows);
  }

  performSearch(value: string) {
    this.filter = value;
    this.pageConfigService.saveConfig('fator_ajuste_filter', this.filter);
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
        this.router.navigate(['/fatorAjuste', event.selection.id, 'edit']);
        break;
      case 'delete':
        this.confirmDelete(event.selection.id);
        break;
      case 'view':
        this.router.navigate(['/fatorAjuste', event.selection.id]);
        break;
    }
  }

  confirmDelete(id: any) {
    this.confirmationService.confirm({
      message: this.getLabel('Tem certeza que deseja excluir o registro?'),
      accept: () => {
        this.fatorAjusteService.delete(id).subscribe(() => {
          this.datatable.refresh(undefined);
        });
      }
    });
  }
}
