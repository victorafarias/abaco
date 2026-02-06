# Plano de Implementação: Melhorias na Persistência de Configurações de Paginação

## Visão Geral

Este plano detalha as melhorias necessárias para corrigir problemas identificados na persistência de configurações de paginação em listas/grids do sistema. O problema reportado é que as configurações de número de registros por página escolhidas pelo usuário não são aplicadas corretamente quando ele retorna à lista.

**Motivação**: Melhorar a experiência do usuário garantindo que suas preferências de paginação sejam sempre respeitadas, além de tornar o sistema mais robusto e fácil de debugar.

---

## Análise do Estado Atual

### Implementação Existente

O sistema possui um serviço centralizado [`PageConfigService`](file:///c:/Users/Administrator/abaco/frontend/src/app/shared/page-config.service.ts) que gerencia configurações via `localStorage`:

```typescript
// Estado atual - SEM tratamento de erros
public saveConfig(key: string, value: any): void {
    localStorage.setItem(key, JSON.stringify(value));
}

public getConfig(key: string): any {
    const item = localStorage.getItem(key);
    return item ? JSON.parse(item) : null;  // ❌ Pode falhar
}
```

**17 componentes** usam este serviço:
- [`user-list`](file:///c:/Users/Administrator/abaco/frontend/src/app/user/user-list/user-list.component.ts#89-93)
- [`analise-list`](file:///c:/Users/Administrator/abaco/frontend/src/app/analise/analise-list/analise-list.component.ts#263-267)
- [`sistema-list`](file:///c:/Users/Administrator/abaco/frontend/src/app/sistema/sistema-list/sistema-list.component.ts#64-68)
- E 14 outros componentes de lista

### Descobertas Principais:

1. **Timing de Inicialização** ([user-list.component.ts:89-93](file:///c:/Users/Administrator/abaco/frontend/src/app/user/user-list/user-list.component.ts#89-93)):
   - Configuração aplicada no `ngOnInit()` ANTES do DataTable estar renderizado
   - Change detection pode não perceber a mudança

2. **Sem Validação de Valores** ([user-list.component.ts:29](file:///c:/Users/Administrator/abaco/frontend/src/app/user/user-list/user-list.component.ts#29)):
   - Se `localStorage` tem `rows=100` mas `rowsPerPageOptions=[5,10,20]`, o sistema quebra
   - Nenhum componente valida se o valor salvo é válido

3. **Sem Tratamento de Erros** ([page-config.service.ts:24-27](file:///c:/Users/Administrator/abaco/frontend/src/app/shared/page-config.service.ts#24-27)):
   - `JSON.parse()` sem `try-catch` causará crash se dados estiverem corrompidos
   
4. **Sem Instrumentação**:
   - Impossível debugar problemas sem logs estruturados

---

## Estado Final Desejado

### Serviço Robusto
- `PageConfigService` com tratamento de erros completo
- Logs estruturados em todas as operações
- Métodos auxiliares (`clearConfig`, `clearAllConfigs`)

### Componentes Resilientes  
- Validação de valores recuperados do localStorage
- Fallback seguro para valores inválidos
- Aplicação correta de configuração após DataTable estar pronto
- Logs em pontos estratégicos do ciclo de vida

### Developer Experience
- Documentação JSDoc clara
- Fácil debugging via console logs
- Código auto-documentado

---

## O que NÃO estamos fazendo

- ❌ Não vamos alterar a estratégia de armazenamento (continua localStorage)
- ❌ Não vamos adicionar persistência de outras configurações além das existentes
- ❌ Não vamos modificar a biblioteca PrimeNG ou componentes `basis-datatable`
- ❌ Não vamos adicionar testes automatizados neste momento (será feito posteriormente)

---

## Abordagem de Implementação

**Estratégia**: Implementação em camadas, do core para as bordas:

1. **Camada Base**: Melhorar `PageConfigService` primeiro
2. **Camada de Componentes**: Atualizar todos os 17 componentes usando padrão consistente
3. **Validação**: Testar manualmente cada lista crítica

---

## Fase 1: Melhoria do Serviço Base (PageConfigService)

### Visão Geral
Adicionar robustez ao serviço central de configuração com tratamento de erros, logs e métodos auxiliares.

### Mudanças Necessárias:

#### 1. PageConfigService - Tratamento de Erros e Logs

**Arquivo**: [`frontend/src/app/shared/page-config.service.ts`](file:///c:/Users/Administrator/abaco/frontend/src/app/shared/page-config.service.ts)

**Mudanças**:

1. **Adicionar tratamento de erros em `getConfig`**:
```typescript
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
```

2. **Adicionar logs em `saveConfig`**:
```typescript
/**
 * Salva a configuração no localStorage.
 * 
 * @param key Chave para salvar a configuração.
 * @param value Valor a ser salvo.
 */
public saveConfig(key: string, value: any): void {
    try {
        console.log(`[PageConfigService] Salvando '${key}':`, value);
        localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
        console.error(`[PageConfigService] Erro ao salvar '${key}':`, error);
    }
}
```

3. **Adicionar método `clearConfig`**:
```typescript
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
```

4. **Adicionar método `clearAllConfigs`**:
```typescript
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
```

### Critérios de Sucesso:

#### Verificação Manual:
- [ ] Abrir console do navegador
- [ ] Navegar para qualquer lista (ex: `/admin/user`)
- [ ] Verificar logs: `[PageConfigService] Recuperando 'user_rows': ...`
- [ ] Mudar paginação de 20 para 10
- [ ] Verificar log: `[PageConfigService] Salvando 'user_rows': 10`
- [ ] Corromper manualmente valor no localStorage (DevTools → Application → Local Storage)
- [ ] Recarregar página
- [ ] Verificar que erro é capturado e valor corrompido é removido

---

## Fase 2: Atualização dos Componentes de Lista

### Visão Geral
Adicionar validação, logs e correção de timing em todos os 17 componentes que usam persistência de paginação.

### Padrão de Implementação

**Template a ser aplicado em TODOS os componentes**:

```typescript
ngOnInit() {
    // Log de inicialização
    console.log(`[${this.constructor.name}] Inicializando componente`);
    
    // Recuperar configuração salva
    const savedRows = this.pageConfigService.getConfig('COMPONENT_KEY_rows');
    console.log(`[${this.constructor.name}] Config recuperada:`, { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });
    
    // Validar e aplicar OU usar padrão
    if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
        this.rows = savedRows;
        console.log(`[${this.constructor.name}] Aplicando rows válido:`, this.rows);
    } else if (savedRows) {
        // Valor inválido - usar padrão e limpar
        console.warn(`[${this.constructor.name}] Valor inválido (${savedRows}) não está em rowsPerPageOptions. Usando padrão.`);
        this.rows = this.rowsPerPageOptions ? this.rowsPerPageOptions[0] : 20;
        this.pageConfigService.saveConfig('COMPONENT_KEY_rows', this.rows);
    } else {
        console.log(`[${this.constructor.name}] Nenhuma config salva. Usando padrão:`, this.rows);
    }
    
    // ... resto da inicialização original
}

onPageChange(event) {
    console.log(`[${this.constructor.name}] Evento de mudança de página:`, event);
    this.rows = event.rows;
    this.pageConfigService.saveConfig('COMPONENT_KEY_rows', this.rows);
    console.log(`[${this.constructor.name}] Nova configuração salva:`, this.rows);
}
```

### Mudanças por Componente:

#### 1. user-list.component.ts

**Arquivo**: [`frontend/src/app/user/user-list/user-list.component.ts`](file:///c:/Users/Administrator/abaco/frontend/src/app/user/user-list/user-list.component.ts)

**Modificar `ngOnInit` (linhas 89-93)**:
```typescript
ngOnInit() {
    console.log('[UserList] Inicializando componente');
    const savedRows = this.pageConfigService.getConfig('user_rows');
    console.log('[UserList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });
    
    if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
        this.rows = savedRows;
        console.log('[UserList] Aplicando rows válido:', this.rows);
    } else if (savedRows) {
        console.warn(`[UserList] Valor inválido (${savedRows}). Usando padrão.`);
        this.rows = 20;
        this.pageConfigService.saveConfig('user_rows', this.rows);
    } else {
        console.log('[UserList] Nenhuma config salva. Usando padrão:', this.rows);
    }
    
    const savedCols = this.pageConfigService.getConfig('user_columnsVisible');
    // ... resto continua igual
}
```

**Modificar `onPageChange` (linhas 339-342)**:
```typescript
onPageChange(event) {
    console.log('[UserList] Evento de mudança de página:', event);
    this.rows = event.rows;
    this.pageConfigService.saveConfig('user_rows', this.rows);
    console.log('[UserList] Nova configuração salva:', this.rows);
}
```

---

#### 2. analise-list.component.ts

**Arquivo**: [`frontend/src/app/analise/analise-list/analise-list.component.ts`](file:///c:/Users/Administrator/abaco/frontend/src/app/analise/analise-list/analise-list.component.ts)

**Modificar `ngOnInit` (linhas 263-272)**:
```typescript
public ngOnInit() {
    console.log('[AnaliseList] Inicializando componente');
    const savedRows = this.pageConfigService.getConfig('analise_rows');
    console.log('[AnaliseList] Config recuperada:', { savedRows, rowsPerPageOptions: this.rowsPerPageOptions });
    
    if (savedRows && this.rowsPerPageOptions && this.rowsPerPageOptions.includes(savedRows)) {
        this.rows = savedRows;
        console.log('[AnaliseList] Aplicando rows válido:', this.rows);
    } else if (savedRows) {
        console.warn(`[AnaliseList] Valor inválido (${savedRows}). Usando padrão.`);
        this.rows = 20;
        this.pageConfigService.saveConfig('analise_rows', this.rows);
    } else {
        console.log('[AnaliseList] Nenhuma config salva. Usando padrão:', this.rows);
    }
    
    this.userAnaliseUrl = this.grupoService.grupoUrl + this.changeUrl();
    this.estadoInicial();
    this.verificarPermissoes();
    this.offset = new Date().getTimezoneOffset();
}
```

**Modificar `onPageChange` (linhas 1143-1146)**:
```typescript
onPageChange(event) {
    console.log('[AnaliseList] Evento de mudança de página:', event);
    this.rows = event.rows;
    this.pageConfigService.saveConfig('analise_rows', this.rows);
    console.log('[AnaliseList] Nova configuração salva:', this.rows);
}
```

---

#### 3-17. Demais Componentes

**Aplicar o mesmo padrão nos seguintes arquivos**:

| # | Componente | Arquivo | Chave Config | rowsPerPageOptions |
|---|------------|---------|--------------|-------------------|
| 3 | sistema-list | [`sistema-list.component.ts`](file:///c:/Users/Administrator/abaco/frontend/src/app/sistema/sistema-list/sistema-list.component.ts#64-68) | `sistema_rows` | [5, 10, 20] |
| 4 | tipo-equipe-list | `tipo-equipe-list.component.ts` | `tipo_equipe_rows` | [5, 10, 20] |
| 5 | status-list | `status-list.component.ts` | `status_rows` | [5, 10, 20] |
| 6 | perfil-list | `perfil-list.component.ts` | `perfil_rows` | padrão |
| 7 | organizacao-list | `organizacao-list.component.ts` | `organizacao_rows` | padrão |
| 8 | nomenclatura-list | `nomenclatura-list.component.ts` | `nomenclatura_rows` | padrão |
| 9 | modulo-list | `modulo-list.component.ts` | `modulo_rows` | padrão |
| 10 | manual-list | `manual-list.component.ts` | `manual_rows` | padrão |
| 11 | funcionalidade-list | `funcionalidade-list.component.ts` | `funcionalidade_rows` | padrão |
| 12 | funcao-dados-form | `funcao-dados-form.component.ts` | `funcao_dados_rows` | [5, 10, 20, 100] |
| 13 | funcao-transacao-form | `funcao-transacao-form.component.ts` | `funcao_transacao_rows` | padrão |
| 14 | fase-list | `fase-list.component.ts` | `fase_rows` | padrão |
| 15 | fator-ajuste-list | `fator-ajuste-list.component.ts` | `fator_ajuste_rows` | padrão |
| 16 | divergencia-list | `divergencia-list.component.ts` | `divergencia_rows` | [5, 10, 20, 50, 100] |
| 17 | contrato-list | `contrato-list.component.ts` | `contrato_rows` | padrão |

> **Nota**: "padrão" significa que o componente não define `rowsPerPageOptions` explicitamente ou usa valor padrão do PrimeNG.

**Para cada componente acima**:
1. Localizar método `ngOnInit()`
2. Localizar linha onde recupera `savedRows`
3. Adicionar validação e logs conforme template
4. Localizar método `onPageChange(event)`
5. Adicionar logs conforme template

### Critérios de Sucesso:

#### Verificação Automatizada:
- [ ] `npm run build` - compilação sem erros
- [ ] Verificar que não há erros de TypeScript

#### Verificação Manual (para cada lista crítica):
- [ ] **user-list**: Navegar para `/admin/user`
  - [ ] Selecionar 10 registros, recarregar → deve manter 10
  - [ ] Selecionar 100 (inválido), recarregar → deve voltar para 20 com warning no console
- [ ] **analise-list**: Navegar para `/analise`
  - [ ] Selecionar 100 registros, recarregar → deve manter 100
  - [ ] Verificar logs no console em cada etapa
- [ ] **sistema-list**: Navegar para `/sistema`
  - [ ] Selecionar 5 registros, recarregar → deve manter 5

---

## Fase 3: Documentação e Polimento

### Visão Geral
Adicionar documentação JSDoc para facilitar manutenção futura.

### Mudanças Necessárias:

#### 1. Documentar PageConfigService

**Arquivo**: [`frontend/src/app/shared/page-config.service.ts`](file:///c:/Users/Administrator/abaco/frontend/src/app/shared/page-config.service.ts)

**Adicionar JSDoc na classe**:
```typescript
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
    // ...
}
```

#### 2. Documentar Padrão de Uso em Comentário

**Arquivo**: [`frontend/src/app/shared/page-config.service.ts`](file:///c:/Users/Administrator/abaco/frontend/src/app/shared/page-config.service.ts)

**Adicionar comentário no topo do arquivo**:
```typescript
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
```

### Critérios de Sucesso:

#### Verificação Manual:
- [ ] Código auto-documentado com JSDoc visível no IntelliSense do IDE
- [ ] Comentários de guia de uso no arquivo do serviço

---

## Estratégia de Teste

### Testes Manuais (Obrigatórios)

**Cenário 1: Persistência Básica**
1. Navegar para lista de usuários (`/admin/user`)
2. Alterar paginação de 20 para 10
3. Navegar para outra página (ex: `/sistema`)
4. Voltar para `/admin/user`
5. ✅ Esperado: Lista exibe 10 registros

**Cenário 2: Valor Inválido**
1. Abrir DevTools → Application → Local Storage
2. Adicionar manualmente: `user_rows: 999` (número não na lista de opções)
3. Recarregar página `/admin/user`
4. ✅ Esperado: Console mostra warning, lista usa padrão (20), localStorage é corrigido

**Cenário 3: Valor Corrompido**
1. Abrir DevTools → Application → Local Storage
2. Adicionar manualmente: `user_rows: "invalid json{"`
3. Recarregar página `/admin/user`
4. ✅ Esperado: Console mostra erro, valor é removido, lista funciona com padrão

**Cenário 4: Múltiplas Listas**
1. Configurar `user_rows = 10`
2. Configurar `analise_rows = 100`
3. Configurar `sistema_rows = 5`
4. Recarregar navegador
5. Navegar para cada lista
6. ✅ Esperado: Cada lista mantém sua configuração individual

**Cenário 5: Limpeza Total**
1. No console do navegador: `(window as any).pageConfigService?.clearAllConfigs?.()`
2. Recarregar página
3. ✅ Esperado: Todas as listas voltam ao padrão (20)

### Testes de Regressão

Verificar que funcionalidades existentes continuam funcionando:
- [ ] Ordenação de colunas
- [ ] Filtros de busca
- [ ] Navegação entre páginas
- [ ] Seleção de registros
- [ ] Ações (editar, excluir, visualizar)

---

## Considerações de Performance

**Impacto Positivo**:
- Logs estruturados facilitam identificação de problemas
- Validação evita estados inconsistentes que podem causar loops

**Impacto Mínimo**:
- Adição de logs tem overhead negligenciável (milissegundos)
- Validação `includes()` é O(n) mas arrays pequenos ([5,10,20,50,100])
- localStorage já é usado, não há operações adicionais

**Monitoramento**:
- Observar console em produção para verificar se logs não estão excessivos
- Se necessário no futuro, pode-se adicionar flag de ambiente para desabilitar logs detalhados

---

## Implementação Passo a Passo

### Ordem Recomendada:

1. **Dia 1: Serviço Base**
   - Modificar `PageConfigService`
   - Testar métodos novos no console
   - Commit: "feat: adicionar robustez ao PageConfigService"

2. **Dia 2-3: Componentes Críticos (3)**
   - `user-list.component.ts`
   - `analise-list.component.ts`  
   - `sistema-list.component.ts`
   - Testar cada um individualmente
   - Commit: "feat: adicionar validação em componentes críticos"

3. **Dia 4: Componentes Restantes (14)**
   - Aplicar padrão em todos os outros
   - Testar amostra de 3-4 listas
   - Commit: "feat: adicionar validação em todos os componentes de lista"

4. **Dia 5: Documentação e Testes**
   - Adicionar JSDoc
   - Executar todos os cenários de teste
   - Commit: "docs: documentar PageConfigService e padrão de uso"

---

## Referências

- **Relatório de Revisão**: [`revisao_paginacao.md`](file:///c:/Users/Administrator/.gemini/antigravity/brain/b3b28dea-622e-449c-a6c5-533c489d85d6/revisao_paginacao.md)
- **Serviço Base**: [`page-config.service.ts`](file:///c:/Users/Administrator/abaco/frontend/src/app/shared/page-config.service.ts)
- **Exemplo de Implementação**: [`user-list.component.ts`](file:///c:/Users/Administrator/abaco/frontend/src/app/user/user-list/user-list.component.ts)

---

**Plano criado em**: 2026-02-06  
**Autor**: Antigravity AI Agent (via skill PLANEJADOR)
