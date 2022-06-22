import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PageNotificationService } from '@nuvem/primeng-components';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Configuracao } from './model/configuracao.model';


@Injectable()
export class ConfiguracaoService {

    resourceUrl = environment.apiUrl + '/configuracao';

    constructor(
        private http: HttpClient,
        private pageNotificationService: PageNotificationService,
        ) {}


    buscarConfiguracao(): Observable<Configuracao> {
        return this.http.get<Configuracao>(this.resourceUrl).pipe(catchError((error: any) => {
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage('Você não possui permissão!');
                return Observable.throw(new Error(error.status));
            }
        }));
    }

	salvarConfiguracao(configuracao: Configuracao): Observable<Configuracao> {
        return this.http.patch<any>(this.resourceUrl, configuracao).pipe(catchError((error: any) => {
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage('Você não possui permissão!');
                return Observable.throw(new Error(error.status));
            }
        }));
    }
}
