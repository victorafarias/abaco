import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class FuncaoTransacaoVisaoPfService {

  constructor(private http: HttpClient) {
  }

  sendTipoTela(id, tipo, analise_id, tipoAnalise) {
    let formData: FormData = new FormData()
    formData.append('id', id)
    formData.append('tipos', tipo)
    formData.append('analise_id', analise_id)
    formData.append('tipo', tipoAnalise)
    return this.http.post('visaopf/api/tela/tipos', formData, { responseType: 'text' })
  }

}
