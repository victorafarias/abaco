import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class PageConfigService {

    constructor() { }

    /**
     * Salva a configuração no localStorage.
     * @param key Chave para salvar a configuração.
     * @param value Valor a ser salvo.
     */
    public saveConfig(key: string, value: any): void {
        localStorage.setItem(key, JSON.stringify(value));
    }

    /**
     * Recupera a configuração do localStorage.
     * @param key Chave da configuração.
     * @return Valor recuperado ou null.
     */
    public getConfig(key: string): any {
        const item = localStorage.getItem(key);
        return item ? JSON.parse(item) : null;
    }
}
