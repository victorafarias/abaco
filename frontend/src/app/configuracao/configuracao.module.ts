import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BlockUiService } from '@nuvem/angular-base';
import { DatatableModule, PageNotificationService } from '@nuvem/primeng-components';
import { CardModule, FieldsetModule, MultiSelectModule, PickListModule, RadioButtonModule, SharedModule, TableModule } from 'primeng';
import { AbacoButtonsModule } from '../components/abaco-buttons/abaco-buttons.module';
import { SistemaService } from '../sistema';
import { TipoEquipeService } from '../tipo-equipe';
import { ConfiguracaoComponent } from './configuracao.component';
import { ConfiguracaoService } from './configuracao.service';
import {InputSwitchModule} from 'primeng/inputswitch';


@NgModule({
  declarations: [ConfiguracaoComponent],
  imports: [
    CommonModule,
        HttpClientModule,
        FormsModule,
        BrowserModule,
        AbacoButtonsModule,
        SharedModule,
        CommonModule,
        SharedModule,
        DatatableModule,
        MultiSelectModule,
        TableModule,
        PickListModule,
		InputSwitchModule,
		CardModule,
		FieldsetModule,
		RadioButtonModule
  ],
  providers: [
    ConfiguracaoService,
    SistemaService,
    TipoEquipeService,
    PageNotificationService,
    BlockUiService
],
})
export class ConfiguracaoModule { }
