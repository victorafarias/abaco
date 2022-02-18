import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { SecurityModule, VersionTagModule } from '@nuvem/angular-base';
import { BlockUiModule, BreadcrumbModule, ErrorStackModule, MenuModule, PageNotificationModule } from '@nuvem/primeng-components';
import { environment } from '../environments/environment';
import { AnaliseModule } from './analise/analise.module';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BaselineModule } from './baseline/baseline.module';
import { AbacoButtonsModule } from './components/abaco-buttons/abaco-buttons.module';
import { DiarioErrosComponent } from './components/diario-erros/diario-erros.component';
import { AppFooterComponent } from './components/footer/app.footer.component';
import { NovidadesVersaoService } from './components/novidades-versao/novidades-versao-service';
import { AppInlineProfileComponent } from './components/profile/app.profile.component';
import { AppRightpanelComponent } from './components/rightpanel/app.rightpanel.component';
import { AppTopbarComponent } from './components/topbar/app.topbar.component';
import { ConfiguracaoBaselineModule } from './configuracao-baseline/configuracao-baseline.module';
import { ContratoModule } from './contrato/contrato.module';
import { DashboardModule } from './dashboard/dashboard.module';
import { DivergenciaModule } from './divergencia/divergencia.module';
import { EsforcoFaseModule } from './esforco-fase';
import { FaseModule } from './fase/fase.module';
import { FuncaoDadosModule } from './funcao-dados/funcao-dados.module';
import { FuncaoTransacaoModule } from './funcao-transacao/funcao-transacao.module';
import { FuncionalidadeModule } from './funcionalidade/funcionalidade.module';
import { IndexadorModule } from './indexador/indexador.module';
import { LoginModule } from './login/login.module';
import { ManualModule } from './manual/manual.module';
import { ModuloModule } from './modulo/modulo.module';
import { OrganizacaoModule } from './organizacao/organizacao.module';
import { PesquisarFuncaoTransacaoModule } from './pesquisar-ft/pesquisar-ft.module';
import { SenhaModule } from './senha/senha.module';
import { SharedModule } from './shared/shared.module';
import { SistemaModule } from './sistema/sistema.module';
import { StatusModule } from './status/status.module';
import { NomenclaturaModule } from './nomenclatura/nomenclatura.module';
import { VisaopfModule } from './visao-pf/visao-pf.module';
import { VisaopfModelModule } from './visao-pf-model/visao-pf-model.module';
import { VisaopfListsModelsModule } from './visaopf-list-models/visao-pf-list-models.module';
import { VisaopfExportModelModule } from './visao-pf-export-model/visao-pf-export-model.module';

import { PerfilModule } from './perfil/perfil.module';
import { TipoEquipeModule } from './tipo-equipe/tipo-equipe.module';
import { UploadService } from './upload/upload.service';
import { UserModule } from './user/user.module';
import { AuthGuardService } from './util/auth.guard.service';
import { AuthService } from './util/auth.service';

import { FuncaoDadosVisaoPfComponent } from './funcao-dados-visao-pf/funcao-dados-visao-pf.component';
import { FuncaoTransacaoVisaoPfComponent } from './funcao-transacao-visao-pf/funcao-transacao-visao-pf.component';
import { FuncaoTransacaoDeteccaoComponent } from './funcao-transacao-deteccao/funcao-transacao-deteccao.component';

import { TableModule } from 'primeng/table';
import { AccordionModule } from 'primeng/accordion';

@NgModule({
    declarations: [
        AppComponent,
        AppTopbarComponent,
        AppFooterComponent,
        AppRightpanelComponent,
        AppInlineProfileComponent,
        DiarioErrosComponent,
        FuncaoTransacaoVisaoPfComponent,
        FuncaoDadosVisaoPfComponent,
        FuncaoTransacaoDeteccaoComponent,

    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        AppRoutingModule,
        SharedModule,
        HttpClientModule,
        PageNotificationModule,
        BreadcrumbModule,
        ErrorStackModule,
        VersionTagModule.forRoot(environment),
        SecurityModule.forRoot(environment.auth),
        MenuModule,
        FaseModule,
        IndexadorModule,
        AbacoButtonsModule,
        ManualModule,
        EsforcoFaseModule,
        OrganizacaoModule,
        ContratoModule,
        FuncionalidadeModule,
        SistemaModule,
        ModuloModule,
        FuncionalidadeModule,
        TipoEquipeModule,
        UserModule,
        AnaliseModule,
        FuncaoDadosModule,
        FuncaoTransacaoModule,
        BaselineModule,
        SenhaModule,
        LoginModule,
        DashboardModule,
        PesquisarFuncaoTransacaoModule,
        BlockUiModule,
        StatusModule,
        NomenclaturaModule,
        DivergenciaModule,
        ConfiguracaoBaselineModule,
        VisaopfModule,
        VisaopfModelModule,
        VisaopfListsModelsModule,
        VisaopfExportModelModule,
        PerfilModule,
        TableModule,
        AccordionModule
    ],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
        UploadService, AuthService, AuthGuardService, NovidadesVersaoService
    ],
    bootstrap: [AppComponent],
    exports: [ RouterModule]
})
export class AppModule { }
