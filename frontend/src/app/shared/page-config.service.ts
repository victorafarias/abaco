import { Injectable } from '@angular/core';

/**
 * # PageConfigService - Guia de Uso
 *
 * ## Padrão Recomendado para Componentes de Lista:
 *
 * ```typescript
 * export class MyListComponent implements OnInit {
 *     rows = 20;
 *     rowsPerPageOptions = [5, 10, 20, 50];
 *
 *     constructor(private pageConfigService: PageConfigService) {}
 *
 *     ngOnInit() {
 *         // 1. Recuperar valor salvo
 *         const savedRows = this.pageConfigService.getConfig('mylist_rows');
 *
 *         // 2. Validar contra opções permitidas
 *         if (savedRows && this.rowsPerPageOptions.includes(savedRows)) {
 *             this.rows = savedRows;
 *         } else if (savedRows) {
 *             // Valor inválido - resetar
 *             this.pageConfigService.saveConfig('mylist_rows', this.rows);
 *         }
 *     }
 *
 *     onPageChange(event) {
 *         // 3. Salvar quando usuário mudar
 *         this.rows = event.rows;
 *         this.pageConfigService.saveConfig('mylist_rows', this.rows);
 *     }
 * }
 * ```
 *
 * ## Troubleshooting:
 *
 * - **Problema**: Configuração não persiste
 *   - Verificar console → deve ter logs `[PageConfigService] Salvando ...`
 *   - Verificar DevTools → Application → Local Storage → deve ter chave
 *
 * - **Problema**: Erro ao carregar página
 *   - Verificar console → se há erro `[PageConfigService] Erro ao recuperar...`
 *   - Limpar localStorage com `pageConfigService.clearAllConfigs()`
 */

/**
 * Serviço centralizado para gerenciar configurações de interface do usuário.
 *
 * Utiliza localStorage para persistir preferências como:
 * - Número de registros por página em listas (chaves terminadas em '_rows')
 * - Colunas visíveis em tabelas (chaves terminadas em '_columnsVisible')
 * - Parâmetros de busca (chaves terminadas em '_searchParams')
 *
 * @example
 * // Salvar preferência de paginação
 * this.pageConfigService.saveConfig('user_rows', 50);
 *
 * // Recuperar preferência
 * const rows = this.pageConfigService.getConfig('user_rows'); // retorna 50 ou null
 *
 * @remarks
 * Todos os métodos incluem tratamento de erros e logs estruturados.
 * Valores corrompidos são automaticamente removidos do localStorage.
 */
@Injectable({
    providedIn: 'root'
})
export class PageConfigService {

    constructor() { }

    /**
     * Salva a configuração no localStorage.
     *
     * @param key Chave para salvar a configuração.
     * @param value Valor a ser salvo (será serializado com JSON.stringify).
     */
    public saveConfig(key: string, value: any): void {
        try {
            console.log(`[PageConfigService] Salvando '${key}':`, value);
            localStorage.setItem(key, JSON.stringify(value));
        } catch (error) {
            console.error(`[PageConfigService] Erro ao salvar '${key}':`, error);
        }
    }

    /**
     * Recupera a configuração do localStorage.
     *
     * @param key Chave da configuração.
     * @return Valor recuperado ou null se não existir ou estiver corrompido.
     */
    public getConfig(key: string): any {
        try {
            const item = localStorage.getItem(key);
            const value = item ? JSON.parse(item) : null;
            console.log(`[PageConfigService] Recuperando '${key}':`, value);
            return value;
        } catch (error) {
            console.error(`[PageConfigService] Erro ao recuperar configuração '${key}':`, error);
            // Limpar valor corrompido automaticamente
            this.clearConfig(key);
            return null;
        }
    }

    /**
     * Remove uma configuração específica do localStorage.
     *
     * @param key Chave da configuração a ser removida.
     */
    public clearConfig(key: string): void {
        try {
            console.log(`[PageConfigService] Removendo '${key}'`);
            localStorage.removeItem(key);
        } catch (error) {
            console.error(`[PageConfigService] Erro ao remover '${key}':`, error);
        }
    }

    /**
     * Remove todas as configurações de paginação do localStorage.
     *
     * Identifica e remove chaves que terminam com:
     * - '_rows' (configuração de paginação)
     * - '_columnsVisible' (colunas visíveis)
     * - '_searchParams' (parâmetros de busca)
     */
    public clearAllConfigs(): void {
        try {
            console.log('[PageConfigService] Limpando todas as configurações');
            const keysToRemove: string[] = [];

            // Identificar chaves de configuração
            for (let i = 0; i < localStorage.length; i++) {
                const key = localStorage.key(i);
                if (key && (key.endsWith('_rows') ||
                    key.endsWith('_columnsVisible') ||
                    key.endsWith('_searchParams'))) {
                    keysToRemove.push(key);
                }
            }

            // Remover chaves identificadas
            keysToRemove.forEach(key => localStorage.removeItem(key));
            console.log(`[PageConfigService] ${keysToRemove.length} configurações removidas`);
        } catch (error) {
            console.error('[PageConfigService] Erro ao limpar configurações:', error);
        }
    }
}
