import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {DashBoard2} from "./dashboard2.model";

@Injectable({
    providedIn: 'root'
})
export class Dashboard2Service {

    resourceUrl = environment.apiUrl + '/dashboard2';
    clientesUrl = this.resourceUrl + '/clientes';
    historicoUrl = this.resourceUrl + '/historico';
    demandasUrl = this.resourceUrl + '/totalDemandas';
    diferencaGlobalUrl = this.resourceUrl + '/pfDiferencaGlobal'


    constructor(private http: HttpClient) { }

    public ObterMotivos(): Observable<DashBoard2[]> {
        return this.http.get<DashBoard2[]>(`${this.resourceUrl}`);
    }

    public ObterClientes(): Observable<DashBoard2[]> {
        return this.http.get<DashBoard2[]>(`${this.clientesUrl}`)
    }

    public ObterDiferencaPf(): Observable<DashBoard2[]> {
        return this.http.get<DashBoard2[]>(`${this.historicoUrl}`)
    }

    public ObterTotalDemandas(): Observable<DashBoard2[]> {
        return this.http.get<DashBoard2[]>(`${this.demandasUrl}`)
    }

    public ObterPfDiferencaGlobal(): Observable<DashBoard2[]> {
        return this.http.get<DashBoard2[]>(`${this.diferencaGlobalUrl}`)
    }
}
