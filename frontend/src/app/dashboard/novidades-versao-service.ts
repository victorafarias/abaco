import { HttpClient } from '@angular/common/http';
import {Injectable} from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { NovidadeVersaoDTO } from './novidades-versao-dto';

@Injectable()
export class NovidadesVersaoService {
    
    resourceUrl = environment.apiUrl + '/novidades-versao';

    constructor(private http: HttpClient) {
    }


    getAll():Observable<NovidadeVersaoDTO[]>{
        return this.http.get<NovidadeVersaoDTO[]>(this.resourceUrl);
    }


    desabilitarNovidadesUsuario() {
        return this.http.get<NovidadeVersaoDTO[]>(this.resourceUrl+"/desabilitar-novidades");
    }
}
