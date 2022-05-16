import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { PageNotificationService } from "@nuvem/primeng-components";
import { Observable, throwError } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { AbacoMensagens, Mensagem, TipoMensagem } from "../shared/mensagens.dto";


@Injectable()
export class APIInterceptor implements HttpInterceptor{

	constructor(private pageNotification: PageNotificationService){

	}

	intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		return next.handle(req).pipe(
			tap(data => this.handleResponse(data)),
			catchError(e => this.handleError(e))
		);
	}

	handleResponse(data: any){
		if(!data || !data.body) return
		const abacoMensagens  = data?.body?.mensagens;
		if(abacoMensagens){
			if(abacoMensagens.mensagens.length > 0){
				this.ajustarMensagens(abacoMensagens.mensagens);
			}
		}
	}

	handleError(err: HttpErrorResponse): Observable<any>{
		if(err?.error?.mensagens){
			const abacoMensagens = new AbacoMensagens();
			const erros = err?.error?.mensagens instanceof Object ? err?.error?.mensagens : JSON.parse(err?.error?.mensagem);



			erros?.mensagens?.forEach(element => {
				abacoMensagens.mensagens.push(element)
			});

			this.ajustarMensagens(abacoMensagens?.mensagens);
			return throwError(err);
		}

		this.pageNotification.addErrorMessage("Erro "+err.status+": "+err.statusText);
		return throwError(err);
	}

	ajustarMensagens(mensagens: Mensagem[]){
		mensagens.forEach(mensagem => {
			if(mensagem.tipo == TipoMensagem.SUCESSO){
				this.pageNotification.addSuccessMessage(mensagem.mensagem);
			}
			if(mensagem.tipo == TipoMensagem.ERRO){
				this.pageNotification.addErrorMessage(mensagem.mensagem);
			}
			if(mensagem.tipo == TipoMensagem.AVISO){
				this.pageNotification.addInfoMessage(mensagem.mensagem);
			}
		})

	}
}
