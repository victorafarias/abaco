import { Component, ViewChild, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConfirmationService } from 'primeng';
import { DatatableComponent, DatatableClickEvent } from '@nuvem/primeng-components';
import { FuncionalidadeService } from '../funcionalidade.service';
import { PageConfigService } from 'src/app/shared/page-config.service';

@Component({
  selector: 'jhi-funcionalidade',
  templateUrl: './funcionalidade-list.component.html'
})
export class FuncionalidadeListComponent implements OnInit {

  rows = 20;
  rowsPerPageOptions: number[] = [5, 10, 20, 50, 100];
  filter: string;

  @ViewChild(DatatableComponent) datatable: DatatableComponent;

  searchUrl: string = this.funcionalidadeService.searchUrl;

  constructor(
    private router: Router,
    private funcionalidadeService: FuncionalidadeService,
    private confirmationService: ConfirmationService,
    private pageConfigService: PageConfigService
  ) { }

  ngOnInit() {
    const savedRows = this.pageConfigService.getConfig('funcionalidade_rows');
    if (savedRows) {
      this.rows = savedRows;
    }
    const savedFilter = this.pageConfigService.getConfig('funcionalidade_filter');
    if (savedFilter) {
      this.filter = savedFilter;
    }
  }

  onPageChange(event) {
    this.rows = event.rows;
    this.pageConfigService.saveConfig('funcionalidade_rows', this.rows);
  }

  performSearch(value: string) {
    this.filter = value;
    this.pageConfigService.saveConfig('funcionalidade_filter', this.filter);
    this.datatable.refresh(this.filter);
  }

  datatableClick(event: DatatableClickEvent) {
    if (!event.selection) {
      return;
    }
    switch (event.button) {
      case 'edit':
        this.router.navigate(['/funcionalidade', event.selection.id, 'edit']);
        break;
      case 'delete':
        this.confirmDelete(event.selection.id);
        break;
      case 'view':
        this.router.navigate(['/funcionalidade', event.selection.id]);
        break;
    }
  }

  getLabel(label) {
    return label;
  }

  confirmDelete(id: any) {
    this.confirmationService.confirm({
      message: this.getLabel('Tem certeza que deseja excluir o registro?'),
      accept: () => {
        this.funcionalidadeService.delete(id).subscribe(() => {
          this.datatable.refresh(undefined);
        });
      }
    });
  }
}
