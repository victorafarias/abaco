import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AccordionModule } from 'primeng/accordion';
import { SharedModule } from '../shared/shared.module';
import { DashboardComponent } from './dashboard.component';
import { dashboardRoute } from './dashboard.route';

@NgModule({
  declarations: [DashboardComponent],
  imports: [
    SharedModule,
    AccordionModule,
    CommonModule,
    RouterModule.forRoot(dashboardRoute, { useHash: true }),
  ], providers: []
})
export class DashboardModule { }
