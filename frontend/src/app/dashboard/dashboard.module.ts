import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardComponent } from './dashboard.component';
import { RouterModule } from '@angular/router';
import { dashboardRoute } from './dashboard.route'
import { NovidadesVersaoService } from './novidades-versao-service';
import {AccordionModule} from 'primeng/accordion';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [DashboardComponent],
  imports: [
    SharedModule,
    AccordionModule,
    CommonModule,
    RouterModule.forRoot(dashboardRoute, { useHash: true }),
  ], providers: [NovidadesVersaoService]
})
export class DashboardModule { }
