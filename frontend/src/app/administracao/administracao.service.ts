import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {Rotina} from "./rotina.model";

@Injectable({
  providedIn: 'root'
})
export class AdministracaoService {

	resourceUrl = environment.apiUrl + '/administracao';

	constructor(private http: HttpClient) {
	}

	obterTodasRotinas(): Observable<Rotina[]> {
		return this.http.get<Rotina[]>(this.resourceUrl);
	}

}
