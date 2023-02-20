import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginSuccessComponent } from '@nuvem/angular-base';
import { DiarioErrosComponent } from './components/diario-erros/diario-erros.component';
import { ConfiguracaoComponent } from './configuracao';
import { ConfiguracaoBaselineComponent } from './configuracao-baseline';
import { DashboardComponent } from './dashboard/dashboard.component';
import { IndexadorComponent } from './indexador/indexador.component';
import { LoginComponent } from './login';
import { AuthGuardService } from './util/auth.guard.service';
import {Dashboard2Component} from "./dashboard2/dashboard2.component";




const routes: Routes = [
  { path: '', component: LoginComponent, data: { breadcrumb: 'Login'}},
  { path: 'diario-erros', component: DiarioErrosComponent, canActivate: [AuthGuardService] ,  data: { breadcrumb: 'Diário de Erros'} },
  { path: 'login-success', component: LoginSuccessComponent, data: { breadcrumb: 'Login Sucesso'}},
  { path: 'indexador', component: IndexadorComponent , canActivate: [AuthGuardService] , data: { breadcrumb: 'Reindexar'} },
  { path: 'configuracao-baseline', component: ConfiguracaoBaselineComponent , canActivate: [AuthGuardService] , data: { breadcrumb: 'Configuração Baseline'} },
  { path: 'configuracao', component: ConfiguracaoComponent , canActivate: [AuthGuardService] , data: {
		roleParaVerificar: ['ROLE_ABACO_CONFIGURACAO_EDITAR'],
		breadcrumb: 'Configuração'
	} },
  { path: 'login', component: LoginComponent, data: { breadcrumb: 'Login'}},
	{ path: 'dashboard2', component: Dashboard2Component}
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
