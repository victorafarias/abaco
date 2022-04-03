import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { HistoricoDTO } from './historico.dto';


@Injectable()
export class HistoricoService {

  resourceUrl = environment.apiUrl + '/historico';

  constructor(private http: HttpClient) {}

  findAllByAnaliseId(idAnalise: number): Observable<HistoricoDTO[]> {
    return this.http.get<HistoricoDTO[]>(`${this.resourceUrl}/${idAnalise}`);
  }

}
