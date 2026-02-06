# Plano de Implementação: Correção de Persistência de FTRs e DERs em Funções de Transação

## Visão Geral

Este plano corrige o bug onde exclusões de FTRs (ALRs) e DERs não são persistidas ao editar funções de transação. O problema ocorre quando um usuário edita uma função de tipo EE/CE/SE, remove FTRs/DERs existentes, e salva - as exclusões não são refletidas no banco de dados.

**Causa Raiz**: O método `updateFuncaoTransacao()` no backend não sincroniza as coleções gerenciadas pelo Hibernate, fazendo com que órfãos não sejam detectados e removidos.

**Solução**: Utilizar o objeto gerenciado (`funcaoTransacaoOld`) para sincronizar coleções de DERs e ALRs, permitindo que o Hibernate detecte e remova entidades órfãs automaticamente através do `orphanRemoval = true`.

## Análise do Estado Atual

### Problema Identificado
Arquivo: [`FuncaoTransacaoResource.java:163-195`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/web/rest/FuncaoTransacaoResource.java#L163-L195)

O método `updateFuncaoTransacao()`:
1. Busca o objeto antigo do banco (`funcaoTransacaoOld`) na linha 167
2. Converte o DTO em uma **nova** entidade usando `convertToEntity()` na linha 164
3. Salva a nova entidade diretamente sem sincronizar coleções com o objeto gerenciado
4. Hibernate não detecta que DERs/ALRs foram removidos

```java
// ESTADO ATUAL - PROBLEMÁTICO
FuncaoTransacao funcaoTransacao = convertToEntity(funcaoTransacaoDTO); // Novo objeto
FuncaoTransacao funcaoTransacaoOld = funcaoTransacaoRepository.findOne(id); // Objeto gerenciado - NÃO USADO!
// ... configurações ...
FuncaoTransacao result = funcaoTransacaoRepository.save(funcaoTransacao); // Salva objeto novo
```

### Descobertas Principais

- **Configuração JPA Correta**: As anotações `cascade = CascadeType.ALL, orphanRemoval = true` estão corretas em [`FuncaoTransacao.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/domain/FuncaoTransacao.java#L64-L67) (ALRs) e [`FuncaoTransacao.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/domain/FuncaoTransacao.java#L82-L84) (DERs)
- **Objeto Gerenciado Ignorado**: `funcaoTransacaoOld` é buscado mas nunca utilizado
- **Sem Sincronização**: Coleções não são sincronizadas entre objeto gerenciado e novos dados
- **Regra de Negócio**: Funções INM **podem** ter FTRs/DERs - a remoção é opcional e deve ser persistida corretamente

## Estado Final Desejado

Após a correção:
- ✅ Exclusões de FTRs/DERs são persistidas corretamente no banco de dados
- ✅ Hibernate detecta e remove entidades órfãs automaticamente
- ✅ Objeto gerenciado é utilizado para sincronização de coleções
- ✅ Comportamento consistente para todos os tipos de função (EE, CE, SE, INM)

## O que NÃO estamos fazendo

- ❌ Validações ou limpezas automáticas específicas para tipo INM
- ❌ Alterações nas regras de negócio sobre quando FTRs/DERs são permitidos
- ❌ Mudanças no frontend (o problema está apenas no backend)
- ❌ Criação de testes automatizados (apenas testes manuais)

## Abordagem de Implementação

**Estratégia**: Sincronização com Objeto Gerenciado pelo Hibernate

Vamos modificar `updateFuncaoTransacao()` para:
1. Usar o objeto gerenciado (`funcaoTransacaoOld`) como base
2. Copiar todos os campos do DTO para o objeto gerenciado
3. Sincronizar coleções usando `.clear()` + `.addAll()`
4. Deixar o Hibernate gerenciar remoção de órfãos automaticamente

**Vantagens**:
- ✅ Usa corretamente o mecanismo de `orphanRemoval` do Hibernate
- ✅ Código consistente com práticas JPA/Hibernate
- ✅ Não requer deleções explícitas via repository

## Implementação Completa

### Mudanças Necessárias

#### 1. Modificar `updateFuncaoTransacao()`

**Arquivo**: [`FuncaoTransacaoResource.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/web/rest/FuncaoTransacaoResource.java)

**Localização**: Linhas 161-195

**Mudanças**:

1. **Remover criação de novo objeto** - Não usar mais `convertToEntity()` no início
2. **Usar objeto gerenciado** - Trabalhar diretamente com `funcaoTransacaoOld`
3. **Copiar campos do DTO** - Atualizar campos simples do objeto gerenciado
4. **Sincronizar coleções** - Usar `.clear()` e `.addAll()` para DERs e ALRs

**Código Antes** (PROBLEMÁTICO):
```java
@PutMapping(path = "/funcaoTransacao/{id}", consumes = {"multipart/form-data"})
@Timed
public ResponseEntity<FuncaoTransacao> updateFuncaoTransacao(@PathVariable Long id, 
                                                             @RequestPart("funcaoTransacao") FuncaoTransacaoSaveDTO funcaoTransacaoDTO, 
                                                             @RequestPart("files")List<MultipartFile> files) throws URISyntaxException{
    FuncaoTransacao funcaoTransacao = convertToEntity(funcaoTransacaoDTO);

    log.debug("REST request to update FuncaoTransacao : {}", funcaoTransacao);
    FuncaoTransacao funcaoTransacaoOld = funcaoTransacaoRepository.findOne(id);
    Analise analise = analiseRepository.findOneByIdClean(funcaoTransacaoOld.getAnalise().getId());
    funcaoTransacao.getDers().forEach(der -> der.setFuncaoTransacao(funcaoTransacao));
    funcaoTransacao.getAlrs().forEach((alr -> alr.setFuncaoTransacao(funcaoTransacao)));
    funcaoTransacao.setAnalise(analise);

    if (funcaoTransacao.getId() == null) {
        return createFuncaoTransacao(analise.getId(), funcaoTransacaoDTO, files);
    }

    if (funcaoTransacao.getAnalise() == null || funcaoTransacao.getAnalise().getId() == null) {
        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new funcaoTransacao cannot already have an ID")).body(null);
    }
    if(!files.isEmpty()){
        List<UploadedFile> uploadedFiles = funcaoDadosService.uploadFiles(files);
        funcaoTransacao.setFiles(uploadedFiles);
    }
    if(analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)){
        funcaoTransacao.setComplexidade(Complexidade.MEDIA);
    }

    FuncaoTransacao result = funcaoTransacaoRepository.save(funcaoTransacao);

    if(Boolean.TRUE.equals(configuracaoService.buscarConfiguracaoHabilitarCamposFuncao()) && analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
        funcaoTransacaoService.saveVwDersAndVwAlrs(result.getDers(), result.getAlrs(), analise.getSistema().getId(), result.getId());
    }

    return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, funcaoTransacao.getId().toString())).body(result);
}
```

**Código Depois** (CORRIGIDO):
```java
@PutMapping(path = "/funcaoTransacao/{id}", consumes = {"multipart/form-data"})
@Timed
public ResponseEntity<FuncaoTransacao> updateFuncaoTransacao(@PathVariable Long id, 
                                                             @RequestPart("funcaoTransacao") FuncaoTransacaoSaveDTO funcaoTransacaoDTO, 
                                                             @RequestPart("files")List<MultipartFile> files) throws URISyntaxException{
    
    log.debug("REST request to update FuncaoTransacao : {}", funcaoTransacaoDTO);
    
    // ALTERADO: Usar objeto gerenciado do banco
    FuncaoTransacao funcaoTransacaoOld = funcaoTransacaoRepository.findOne(id);
    
    if (funcaoTransacaoOld == null) {
        return ResponseEntity.notFound().build();
    }
    
    Analise analise = analiseRepository.findOneByIdClean(funcaoTransacaoOld.getAnalise().getId());

    // ALTERADO: Copiar campos do DTO para o objeto gerenciado
    atualizarCamposSimples(funcaoTransacaoOld, funcaoTransacaoDTO);
    
    // ALTERADO: Sincronizar coleções de DERs e ALRs
    sincronizarDers(funcaoTransacaoOld, funcaoTransacaoDTO.getDers());
    sincronizarAlrs(funcaoTransacaoOld, funcaoTransacaoDTO.getAlrs());
    
    // Processar arquivos
    if(!files.isEmpty()){
        List<UploadedFile> uploadedFiles = funcaoDadosService.uploadFiles(files);
        funcaoTransacaoOld.setFiles(uploadedFiles);
    }
    
    // Aplicar regras de negócio
    if(analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)){
        funcaoTransacaoOld.setComplexidade(Complexidade.MEDIA);
    }

    // ALTERADO: Salvar objeto gerenciado (Hibernate detectará órfãos automaticamente)
    FuncaoTransacao result = funcaoTransacaoRepository.save(funcaoTransacaoOld);

    if(Boolean.TRUE.equals(configuracaoService.buscarConfiguracaoHabilitarCamposFuncao()) && 
       analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
        funcaoTransacaoService.saveVwDersAndVwAlrs(result.getDers(), result.getAlrs(), 
                                                    analise.getSistema().getId(), result.getId());
    }

    return ResponseEntity.ok()
        .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, result.getId().toString()))
        .body(result);
}
```

#### 2. Criar Método `atualizarCamposSimples()`

**Arquivo**: [`FuncaoTransacaoResource.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/web/rest/FuncaoTransacaoResource.java)

**Localização**: Adicionar após método `convertToEntity()` (após linha 469)

**Propósito**: Copiar campos simples do DTO para o objeto gerenciado

**Código**:
```java
/**
 * Atualiza campos simples (não relacionamentos) do objeto gerenciado com dados do DTO.
 * 
 * @param funcaoTransacao Objeto gerenciado do Hibernate
 * @param dto DTO com novos valores
 */
private void atualizarCamposSimples(FuncaoTransacao funcaoTransacao, FuncaoTransacaoSaveDTO dto) {
    funcaoTransacao.setTipo(dto.getTipo());
    funcaoTransacao.setName(dto.getName());
    funcaoTransacao.setFtrStr(dto.getFtrStr());
    funcaoTransacao.setQuantidade(dto.getQuantidade());
    funcaoTransacao.setImpacto(dto.getImpacto());
    funcaoTransacao.setComplexidade(dto.getComplexidade());
    funcaoTransacao.setPf(dto.getPf());
    funcaoTransacao.setGrossPF(dto.getGrossPF());
    funcaoTransacao.setDetStr(dto.getDetStr());
    funcaoTransacao.setSustantation(dto.getSustantation());
    funcaoTransacao.setOrdem(dto.getOrdem());
    funcaoTransacao.setStatusFuncao(dto.getStatusFuncao());
    
    // Relacionamentos ManyToOne
    if (dto.getFuncionalidadeId() != null) {
        Funcionalidade funcionalidade = new Funcionalidade();
        funcionalidade.setId(dto.getFuncionalidadeId());
        funcaoTransacao.setFuncionalidade(funcionalidade);
    }
    
    if (dto.getFatorAjusteId() != null) {
        FatorAjuste fatorAjuste = new FatorAjuste();
        fatorAjuste.setId(dto.getFatorAjusteId());
        funcaoTransacao.setFatorAjuste(fatorAjuste);
    }
    
    if (dto.getEquipeId() != null) {
        TipoEquipe equipe = new TipoEquipe();
        equipe.setId(dto.getEquipeId());
        funcaoTransacao.setEquipe(equipe);
    }
}
```

#### 3. Criar Método `sincronizarDers()`

**Arquivo**: [`FuncaoTransacaoResource.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/web/rest/FuncaoTransacaoResource.java)

**Localização**: Adicionar após `atualizarCamposSimples()`

**Propósito**: Sincronizar coleção de DERs permitindo que Hibernate detecte órfãos

**Código**:
```java
/**
 * Sincroniza a coleção de DERs do objeto gerenciado.
 * Remove DERs antigos e adiciona novos, permitindo que o Hibernate
 * detecte entidades órfãs através do orphanRemoval=true.
 * 
 * @param funcaoTransacao Objeto gerenciado do Hibernate
 * @param dersDTO Lista de DERs do DTO
 */
private void sincronizarDers(FuncaoTransacao funcaoTransacao, Set<DerFtDTO> dersDTO) {
    // CRÍTICO: Limpar coleção existente - marca DERs atuais como órfãos
    funcaoTransacao.getDers().clear();
    
    // Adicionar novos DERs do DTO
    if (dersDTO != null && !dersDTO.isEmpty()) {
        Set<Der> novosDers = new LinkedHashSet<>();
        dersDTO.forEach(derDto -> {
            Der der = new Der();
            der.setNome(derDto.getNome());
            der.setValor(derDto.getValor());
            der.setFuncaoTransacao(funcaoTransacao);
            novosDers.add(der);
        });
        funcaoTransacao.getDers().addAll(novosDers);
    }
    
    log.debug("Sincronizados {} DERs para FuncaoTransacao ID {}", 
              funcaoTransacao.getDers().size(), funcaoTransacao.getId());
}
```

#### 4. Criar Método `sincronizarAlrs()`

**Arquivo**: [`FuncaoTransacaoResource.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/web/rest/FuncaoTransacaoResource.java)

**Localização**: Adicionar após `sincronizarDers()`

**Propósito**: Sincronizar coleção de ALRs (FTRs) permitindo que Hibernate detecte órfãos

**Código**:
```java
/**
 * Sincroniza a coleção de ALRs (FTRs) do objeto gerenciado.
 * Remove ALRs antigos e adiciona novos, permitindo que o Hibernate
 * detecte entidades órfãs através do orphanRemoval=true.
 * 
 * @param funcaoTransacao Objeto gerenciado do Hibernate
 * @param alrsDTO Lista de ALRs do DTO
 */
private void sincronizarAlrs(FuncaoTransacao funcaoTransacao, Set<AlrDTO> alrsDTO) {
    // CRÍTICO: Limpar coleção existente - marca ALRs atuais como órfãos
    funcaoTransacao.getAlrs().clear();
    
    // Adicionar novos ALRs do DTO
    if (alrsDTO != null && !alrsDTO.isEmpty()) {
        Set<Alr> novosAlrs = new LinkedHashSet<>();
        alrsDTO.forEach(alrDto -> {
            Alr alr = new Alr();
            alr.setNome(alrDto.getNome());
            alr.setValor(alrDto.getValor());
            alr.setFuncaoTransacao(funcaoTransacao);
            novosAlrs.add(alr);
        });
        funcaoTransacao.getAlrs().addAll(novosAlrs);
    }
    
    log.debug("Sincronizados {} ALRs para FuncaoTransacao ID {}", 
              funcaoTransacao.getAlrs().size(), funcaoTransacao.getId());
}
```

### Resumo das Mudanças

| Método | Ação | Linhas Aprox. |
|--------|------|---------------|
| `updateFuncaoTransacao()` | Modificar completamente | 163-195 (33 linhas) |
| `atualizarCamposSimples()` | Criar novo | ~35 linhas |
| `sincronizarDers()` | Criar novo | ~20 linhas |
| `sincronizarAlrs()` | Criar novo | ~20 linhas |
| **Total** | **~110 linhas** | Modificadas/Adicionadas |

## Estratégia de Teste

### Critérios de Sucesso

#### Verificação Manual

##### Cenário 1: Remoção Total de DERs e ALRs
**Objetivo**: Verificar que DERs/ALRs são completamente removidos do banco

**Passos**:
1. [ ] Criar/editar função de transação tipo **EE** com 2 DERs e 2 ALRs
2. [ ] Salvar e verificar no banco que DERs/ALRs foram criados
3. [ ] Editar a função, mudar tipo para **INM**
4. [ ] **Remover TODOS** os DERs e ALRs da interface
5. [ ] Salvar a função
6. [ ] **Verificar no banco**:
   ```sql
   -- Verificar DERs - deve retornar 0 registros
   SELECT * FROM der WHERE funcao_transacao_id = [ID_DA_FUNCAO];
   
   -- Verificar ALRs - deve retornar 0 registros
   SELECT * FROM alr WHERE funcao_transacao_id = [ID_DA_FUNCAO];
   ```
7. [ ] **Resultado Esperado**: Nenhum DER ou ALR associado à função no banco

##### Cenário 2: Remoção Parcial de DERs e ALRs
**Objetivo**: Verificar que apenas DERs/ALRs selecionados são removidos

**Passos**:
1. [ ] Criar função de transação tipo **CE** com 3 DERs e 3 ALRs  
2. [ ] Salvar e verificar no banco (deve ter 3 + 3 = 6 registros)
3. [ ] Editar a função
4. [ ] **Remover** 2 DERs (deixar 1)
5. [ ] **Remover** 2 ALRs (deixar 1)
6. [ ] Salvar
7. [ ] **Verificar no banco**:
   ```sql
   -- Deve retornar 1 DER
   SELECT COUNT(*) FROM der WHERE funcao_transacao_id = [ID_DA_FUNCAO];
   
   -- Deve retornar 1 ALR
   SELECT COUNT(*) FROM alr WHERE funcao_transacao_id = [ID_DA_FUNCAO];
   ```
8. [ ] **Resultado Esperado**: 1 DER e 1 ALR restante no banco

##### Cenário 3: Manter DERs/ALRs em Função INM
**Objetivo**: Verificar que funções INM podem ter DERs/ALRs (regra de negócio)

**Passos**:
1. [ ] Criar função de transação tipo **EE** com 2 DERs e 2 ALRs
2. [ ] Salvar
3. [ ] Editar e mudar tipo para **INM**
4. [ ] **NÃO remover** DERs e ALRs
5. [ ] Salvar
6. [ ] **Verificar no banco**:
   ```sql
   -- Deve retornar 2 DERs
   SELECT * FROM der WHERE funcao_transacao_id = [ID_DA_FUNCAO];
   
   -- Deve retornar 2 ALRs
   SELECT * FROM alr WHERE funcao_transacao_id = [ID_DA_FUNCAO];
   ```
7. [ ] **Resultado Esperado**: 2 DERs e 2 ALRs mantidos no banco

##### Cenário 4: Substituição de DERs/ALRs
**Objetivo**: Verificar que DERs/ALRs são trocados corretamente

**Passos**:
1. [ ] Criar função tipo **SE** com DERs: `["DER-A", "DER-B"]` e ALRs: `["ALR-X", "ALR-Y"]`
2. [ ] Salvar e verificar no banco
3. [ ] Editar a função
4. [ ] **Remover todos** DERs e ALRs antigos
5. [ ] **Adicionar novos** DERs: `["DER-C", "DER-D", "DER-E"]` e ALRs: `["ALR-Z"]`
6. [ ] Salvar
7. [ ] **Verificar no banco**:
   ```sql
   -- Deve retornar DER-C, DER-D, DER-E (3 registros)
   SELECT nome FROM der WHERE funcao_transacao_id = [ID_DA_FUNCAO] ORDER BY nome;
   
   -- Deve retornar ALR-Z (1 registro)
   SELECT nome FROM alr WHERE funcao_transacao_id = [ID_DA_FUNCAO];
   ```
8. [ ] **Resultado Esperado**: DERs antigos removidos, novos DERs criados; ALRs antigos removidos, novo ALR criado

##### Cenário 5: Órfãos Não Devem Existir
**Objetivo**: Verificar que não existem DERs/ALRs órfãos no banco após múltiplas edições

**Passos**:
1. [ ] Criar e editar múltiplas funções de transação, removendo e adicionando DERs/ALRs
2. [ ] Após todas as operações, executar:
   ```sql
   -- Verificar DERs órfãos - deve retornar 0
   SELECT COUNT(*) FROM der WHERE funcao_transacao_id IS NULL;
   
   -- Verificar ALRs órfãos - deve retornar 0
   SELECT COUNT(*) FROM alr WHERE funcao_transacao_id IS NULL;
   
   -- Verificar DERs com FK inválida - deve retornar 0
   SELECT COUNT(*) FROM der d 
   WHERE NOT EXISTS (
       SELECT 1 FROM funcao_transacao ft WHERE ft.id = d.funcao_transacao_id
   );
   
   -- Verificar ALRs com FK inválida - deve retornar 0
   SELECT COUNT(*) FROM alr a 
   WHERE NOT EXISTS (
       SELECT 1 FROM funcao_transacao ft WHERE ft.id = a.funcao_transacao_id
   );
   ```
3. [ ] **Resultado Esperado**: 0 órfãos em todas as queries

### Checklist de Validação Final

Após todas as mudanças implementadas:

- [ ] **Compilação**: Backend compila sem erros (`mvn clean compile`)
- [ ] **Cenário 1**: ✅ Remoção total funciona
- [ ] **Cenário 2**: ✅ Remoção parcial funciona
- [ ] **Cenário 3**: ✅ INM pode manter DERs/ALRs
- [ ] **Cenário 4**: ✅ Substituição funciona corretamente
- [ ] **Cenário 5**: ✅ Sem órfãos no banco
- [ ] **Regressão**: ✅ Criação de novas funções ainda funciona
- [ ] **Regressão**: ✅ Edição de outros campos (nome, tipo, etc.) funciona

## Considerações de Desempenho

### Impacto Esperado
- **Positivo**: Redução de registros órfãos no banco de dados
- **Neutro**: Performance de update permanece similar (mesmas operações, ordem diferente)
- **Sem Impacto**: Não há queries adicionais (usando objeto já carregado)

### Otimizações Aplicadas
- ✅ Reutilização do objeto `funcaoTransacaoOld` já carregado (sem query extra)
- ✅ Sincronização em memória antes de persistir (operações em lote)
- ✅ Hibernate gerencia CASCADE automaticamente (sem queries manuais)

## Referências

### Pesquisa Original
- **Relatório**: [`research_funcao_transacao_ders_alrs_bug.md`](file:///C:/Users/Administrator/.gemini/antigravity/brain/3eb7bcbb-1442-4692-af87-a27179cbde68/research_funcao_transacao_ders_alrs_bug.md)

### Código Fonte
- **Backend Resource**: [`FuncaoTransacaoResource.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/web/rest/FuncaoTransacaoResource.java)
- **Entidade Principal**: [`FuncaoTransacao.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/domain/FuncaoTransacao.java)
- **Entidade DER**: [`Der.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/domain/Der.java)
- **Entidade ALR**: [`Alr.java`](file:///c:/Users/Administrator/abaco/backend/src/main/java/br/com/basis/abaco/domain/Alr.java)

### Documentação Técnica
- **Hibernate orphanRemoval**: [Documentação oficial](https://docs.jbuild.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#pc-managed-state)
- **JPA Cascade Types**: [Java EE Tutorial](https://docs.oracle.com/javaee/7/tutorial/persistence-intro003.htm)
