import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

import { Sistema } from './sistema.model';
import { HttpClient } from '@angular/common/http';
import { PageNotificationService } from '@nuvem/primeng-components';
import { Observable } from 'rxjs';
import { ResponseWrapper } from '../shared';
import { catchError } from 'rxjs/operators';
import { ExportacaoUtilService } from '../components/abaco-buttons/export-button/export-button.service';
import { ExportacaoUtil } from '../components/abaco-buttons/export-button/export-button.util';

@Injectable()
export class SistemaService {



    resourceUrl = environment.apiUrl + '/sistemas';

    findByOrganizacaoUrl = this.resourceUrl + '/organizacao';

    searchUrl = environment.apiUrl + '/_search/sistemas';

    fieldSearchSiglaUrl = environment.apiUrl + '/_searchSigla/sistemas';

    fieldSearchSistemaUrl = environment.apiUrl + '/_searchSistema/sistemas';

    fieldSearchOrganizacaoUrl = environment.apiUrl + '/_searchOrganizacao/sistemas';

    constructor(private http: HttpClient, private pageNotificationService: PageNotificationService) {
    }

    getLabel(label) {
        return label;
    }

    create(sistema: Sistema): Observable<Sistema> {
        const copy = this.convert(sistema);
        return this.http.post<Sistema>(this.resourceUrl, copy).pipe(catchError((error: any) => {
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
                return Observable.throw(new Error(error.status));
            }
        }));
    }

    update(sistema: Sistema): Observable<Sistema> {
        const copy = this.convert(sistema);
        return this.http.put<Sistema>(this.resourceUrl, copy).pipe(catchError((error: any) => {
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
                return Observable.throw(new Error(error.status));
            }
        }));
    }

    find(id: number): Observable<Sistema> {
        return this.http.get<Sistema>(`${this.resourceUrl}/${id}`).pipe(catchError((error: any) => {
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
                return Observable.throw(new Error(error.status));
            }
        }));
    }

    findAllByOrganizacaoId(orgId: number): Observable<ResponseWrapper> {
        const url = `${this.findByOrganizacaoUrl}/${orgId}`;
        return this.http.get<ResponseWrapper>(url).pipe(catchError((error: any) => {
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
                return Observable.throw(new Error(error.status));
            }
        }));
    }

    findAllSystemOrg(orgId: number): Observable<Sistema[]> {
        const url = `${this.findByOrganizacaoUrl}/${orgId}`;
        return this.http.get<Sistema[]>(url).pipe(catchError((error: any) => {
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
                return Observable.throw(new Error(error.status));
            }
        }));
    }

    dropDown(): Observable<Sistema[]> {
        return this.http.get<Sistema[]>(this.resourceUrl + '/drop-down').pipe(catchError((error: any) => {
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
                return Observable.throw(new Error(error.status));
            }
        }));
    }

    delete(id: number): Observable<Response> {
        return this.http.delete<Response>(`${this.resourceUrl}/${id}`).pipe(catchError((error: any) => {
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
                return Observable.throw(new Error(error.status));
            }
            if (error) {
                const msgError = error.headers.get('x-abacoapp-error');
                if (msgError === 'error.not_found_system') {
                    this.pageNotificationService.addErrorMessage(this.getLabel(
                        'Cadastros.Sistema.Mensagens.msgOcorreuErroNaExclusaoDoSistema')
                    );
                } else if (msgError === 'error.analise_exists') {
                    this.pageNotificationService.addErrorMessage(this.getLabel(
                        'Cadastros.Sistema.Mensagens.msgSistemaVinculadoNaoPodeSerExcluido')
                    );
                }
                return Observable.throw(new Error(error.status));
            }
        }));
    }



    // private convertResponse(res: Response): ResponseWrapper {
    //     const jsonResponse = res.json();
    //     const result = [];
    //     for (let i = 0; i < jsonResponse.length; i++) {
    //         result.push(this.convertItemFromServer(jsonResponse[i]));
    //     }
    //     return new ResponseWrapper(res.headers, result, res.status);
    // }

    /**
     * Convert a returned JSON object to Sistema.
     */
    private convertItemFromServer(json: any): Sistema {
        const entity: Sistema = Sistema.fromJSON(json);
        return entity;
    }

    /**
     * Convert a Sistema to a JSON which can be sent to the server.
     */
    private convert(sistema: Sistema): Sistema {
        const copy: Sistema = Object.assign(Sistema, sistema);
        return Sistema.toNonCircularJson(copy);
    }

    exportar(tipoRelatorio: string, lista: any[], resourceName: string) {
        ExportacaoUtilService.exportReport(tipoRelatorio, this.http, resourceName, null, resourceName == "modulos" ? { modulos: lista } : { funcionalidades: lista })
            .subscribe((res: Blob) => {
                const file = new Blob([res], { type: tipoRelatorio });
                const url = URL.createObjectURL(file);
                ExportacaoUtil.download(url, resourceName + ExportacaoUtilService.getExtension(tipoRelatorio));
            });
    }

    imprimir(lista: any[], resourceName: string) {
        const obj = resourceName == "modulos" ? { modulos: lista } : { funcionalidades: lista };
        ExportacaoUtilService.imprimir(this.http, resourceName, null, obj).subscribe(res => {
            var file = new Blob([res], { type: 'application/pdf' });
            var fileURL = URL.createObjectURL(file);
            window.open(fileURL);
        })
    }

    /**
     * Busca todas as funções distintas (combinação única de Módulo, Funcionalidade e Nome)
     * de todas as análises associadas a um sistema.
     * 
     * @param sistemaId ID do sistema
     * @returns Observable com Set de funções distintas
     */
    getFuncoesDistintas(sistemaId: number): Observable<FuncaoDistinta[]> {
        const url = `${this.resourceUrl}/${sistemaId}/funcoes-distintas`;
        console.log('[SistemaService] Buscando funções distintas para sistema:', sistemaId);
        return this.http.get<FuncaoDistinta[]>(url).pipe(catchError((error: any) => {
            console.error('[SistemaService] Erro ao buscar funções distintas:', error);
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
            }
            return Observable.throw(new Error(error.status));
        }));
    }

    /**
     * Renomeia funções em lote. Atualiza todas as ocorrências de funções
     * que correspondem ao conjunto Módulo-Funcionalidade-NomeAtual.
     * 
     * @param sistemaId ID do sistema
     * @param renomeacoes Lista de renomeações a serem aplicadas
     * @returns Observable com número de funções atualizadas
     */
    renomearFuncoes(sistemaId: number, renomeacoes: RenomearFuncao[]): Observable<number> {
        const url = `${this.resourceUrl}/${sistemaId}/renomear-funcoes`;
        console.log('[SistemaService] Renomeando funções para sistema:', sistemaId, renomeacoes);
        return this.http.put<number>(url, renomeacoes).pipe(catchError((error: any) => {
            console.error('[SistemaService] Erro ao renomear funções:', error);
            if (error.status === 403) {
                this.pageNotificationService.addErrorMessage(this.getLabel('Você não possui permissão!'));
            }
            return Observable.throw(new Error(error.status));
        }));
    }

}

/**
 * Interface para representar uma função distinta.
 * Combinação única de Módulo, Funcionalidade e Nome da Função.
 */
export interface FuncaoDistinta {
    nomeModulo: string;
    nomeFuncionalidade: string;
    nomeFuncao: string;
    tipo?: string;
}

/**
 * Interface para requisição de alteração de função.
 * Os campos "novo*" são opcionais - se não informados, o campo correspondente não será alterado.
 */
export interface RenomearFuncao {
    nomeModulo: string;
    nomeFuncionalidade: string;
    nomeAtual: string;
    novoNome?: string;
    novoModulo?: string;
    novaFuncionalidade?: string;
}

