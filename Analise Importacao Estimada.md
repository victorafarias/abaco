Análise de Importação de Análise Estimada
Sumário Executivo
Este documento detalha todos os componentes (classes, métodos, endpoints etc.) envolvidos na funcionalidade de importação de análise do tipo Estimada no sistema ABACO, apresentados na ordem de execução do fluxo completo.

1. Visão Geral do Fluxo
O fluxo de importação de análise Estimada é dividido em duas etapas principais:

Etapa 1: Carregamento e Leitura do Arquivo Excel
O sistema recebe um arquivo Excel, processa-o e retorna os dados extraídos para o frontend, permitindo validação e mapeamento (especialmente de fatores de ajuste).

Etapa 2: Importação e Persistência dos Dados
Após validação no frontend, o sistema recebe os dados processados e os persiste no banco de dados, criando a análise e suas funções associadas.

1.1 Correções Implementadas (Versão 1.0.71)
Correção 1: Aplicação de trim() em Fatores de Ajuste
Problema Identificado: A análise Estimada não aplicava trim() ao ler nomes de Fatores de Ajuste da planilha, diferentemente da análise Detalhada. Isso causava falhas de correspondência quando os nomes continham espaços extras.

Solução Implementada:

Arquivo: AnaliseService.java
Método: setarModuloFuncionalidadeEstimada (linha ~2048-2055)
Mudança: Adicionado trim() e validação de null/vazio ao ler Fator de Ajuste da coluna B
Código Anterior:
fatorAjuste.setNome(getCellValueAsString(row, 1));
funcao.setFatorAjuste(fatorAjuste);

Código Atual:
String nomeFatorAjuste = getCellValueAsString(row, 1); // Coluna B
if (nomeFatorAjuste != null && !nomeFatorAjuste.trim().isEmpty()) {
    fatorAjuste.setNome(nomeFatorAjuste.trim());
    funcao.setFatorAjuste(fatorAjuste);
} else {
    log.warn("Função '{}' não tem Fator de Ajuste na planilha (Coluna B vazia)", funcao.getName());
}

Benefícios:
Consistência com análise Detalhada
Remove espaços em branco extras dos nomes
Evita criação de FatorAjuste com nome null/vazio
Melhora correspondência com fatores cadastrados no manual

Correção 2: Logs Detalhados na Validação de Fatores
Problema Identificado: Dificuldade em debugar problemas de correspondência de fatores de ajuste.

Solução Implementada:

Arquivo: AnaliseService.java
Método: validarFatoresAjuste (linha ~957-1010)
Mudança: Adicionados logs informativos mostrando:
Todos os fatores disponíveis no manual
Todos os fatores únicos encontrados na planilha
Quais fatores não foram encontrados (serão exibidos no de-para)
Comparação com trim() para evitar problemas de espaços

Logs Adicionados:
===== DEBUG VALIDAÇÃO DE FATORES DE AJUSTE =====
Manual: SISP 2.3
Fatores disponíveis no manual (total: 6): [...]
Mapa de de-para fornecido: {...}
Fatores ÚNICOS encontrados na planilha (total: 4): [...]
Fatores NÃO ENCONTRADOS (serão exibidos no de-para) (total: 2): [...]
==============================================

Benefícios:
Facilita identificação de problemas de correspondência
Mostra exatamente quais fatores estão faltando
Permite validar se trim() está funcionando corretamente

2. Componentes por Camada (Ordem de Execução)
2.1 FRONTEND - Componente de Listagem
Componente: AnaliseListComponent
Arquivo: frontend/src/app/analise/analise-list/analise-list.component.ts
Descrição: Componente Angular responsável pela interface de listagem de análises
Métodos Relacionados à Importação:
Interface permite upload de arquivo Excel
Exibe diálogo de importação com validações
Permite mapeamento de fatores de ajuste não encontrados (De-Para)

2.2 BACKEND - Camada de Recursos (REST Endpoints)
Classe: AnaliseResource
Arquivo: backend/src/main/java/br/com/basis/abaco/web/rest/AnaliseResource.java
Descrição: Controlador REST que expõe endpoints HTTP para operações de análise

Endpoint 1: Carregar Arquivo Excel
Método: carregarArquivoExcel
@PostMapping("/analises/importar-excel/Xlsx")
public ResponseEntity<AnaliseUploadDTO> carregarArquivoExcel(@RequestParam("file") MultipartFile file)
Descrição: Recebe arquivo Excel via HTTP POST, processa-o e retorna DTO com dados extraídos
URL: POST /api/analises/importar-excel/Xlsx
Entrada: MultipartFile (arquivo Excel)
Saída: AnaliseUploadDTO contendo análise e funções extraídas
Chamada: Invoca analiseService.uploadExcelComDTO(file)

Endpoint 2: Importar Análise
Método: importarAnaliseExcel
@PostMapping("/analises/importar-Excel")
public ResponseEntity<Object> importarAnaliseExcel(@Valid @RequestBody AnaliseEditDTO analise)
Descrição: Recebe dados validados do frontend, persiste análise com suas funções no banco de dados
URL: POST /api/analises/importar-Excel
Entrada: AnaliseEditDTO (dados da análise + mapa de fatores de ajuste)
Saída: Analise persistida ou erro de validação de fatores de ajuste
Validações:
Verifica se módulos existem: analiseService.verificaModulos(analise)
Captura exceções de fatores de ajuste não encontrados: FatorAjusteException
Chamada: Invoca analiseService.importarAnaliseExcel(analise)

2.3 BACKEND - Camada de Serviços
Classe: AnaliseService
Arquivo: backend/src/main/java/br/com/basis/abaco/service/AnaliseService.java
Descrição: Serviço principal que contém toda a lógica de negócio para importação de análises

ETAPA 1: LEITURA DO ARQUIVO EXCEL
Método 1.1: uploadExcelComDTO
public AnaliseUploadDTO uploadExcelComDTO(MultipartFile file)
Descrição: Método que retorna DTO com módulos serializados após processar Excel
Execução:
Chama uploadExcel(file) para processar arquivo
Chama converterParaUploadDTO(analise) para converter em DTO
Retorno: AnaliseUploadDTO com dados prontos para serialização JSON

Método 1.2: uploadExcel
public Analise uploadExcel(MultipartFile file)
Descrição: Método principal de processamento do arquivo Excel
Execução:
Cria arquivo temporário do upload
Abre workbook Excel (XSSFWorkbook)
Inicializa estruturas de dados:
Set<FuncaoDados> para funções de dados
Set<FuncaoTransacao> para funções de transação
Contadores de ordem separados: AtomicLong ordemDados e AtomicLong ordemTransacao
Chama setarResumoExcelUpload(workbook, analise) - extrai dados gerais
Verifica método de contagem e chama processadores específicos:
Se INDICATIVA: chama setarIndicativaExcelUpload()
Se DETALHADA: chama setarExcelDetalhadaUpload()
Se ESTIMADA: chama setarExcelEstimadaUpload() ⭐
Verifica se há funções INM: chama setarInmExcelUpload() se aplicável
Atribui funções à análise
Retorna objeto Analise com dados não persistidos

Método 1.3: setarResumoExcelUpload
public void setarResumoExcelUpload(XSSFWorkbook excelFile, Analise analise)
Descrição: Extrai dados gerais da aba "Resumo" do Excel
Dados Extraídos:
Identificador da análise
Número da OS
Método de contagem (INDICATIVA, DETALHADA, ESTIMADA)
Tipo de análise (DESENVOLVIMENTO, MELHORIA, APLICACAO)
Escopo, fronteiras, documentação
Propósito da contagem
Configurações de bloqueio e baseline

Método 1.4: setarExcelEstimadaUpload ⭐ (Específico para Estimada)
public void setarExcelEstimadaUpload(
    XSSFWorkbook excelFile, 
    Set<FuncaoDados> funcaoDados, 
    Set<FuncaoTransacao> funcaotransacao, 
    Analise analise, 
    AtomicLong ordemDados, 
    AtomicLong ordemTransacao
)
Descrição: Processa aba "AFP - Estimativa" do Excel, extraindo funções de dados e transação
Execução:
Obtém aba "AFP - Estimativa" do workbook
Itera sobre linhas 10 a 1081 da planilha
Para cada linha com dados válidos (coluna 0 > 0):
Lê tipo da função (coluna G, índice 6)
Se tipo for função de dados (ALI, AIE):
Chama setarFuncaoDadosEstimada(row, analise, ordemDados)
Adiciona ao conjunto funcaoDados
Se tipo for função de transação (EE, SE, CE):
Chama setarFuncaoTransacaoEstimada(row, analise, ordemTransacao)
Adiciona ao conjunto funcaotransacao

Método 1.5: setarFuncaoDadosEstimada
private FuncaoDados setarFuncaoDadosEstimada(XSSFRow row, Analise analise, AtomicLong ordem)
Descrição: Extrai dados de uma função de dados da linha Excel (análise Estimada)
Colunas Lidas (índices do Excel):
Coluna A (0): ID temporário (existe na planilha mas NÃO é lido pelo sistema)
Coluna B (1): Nome do módulo
Coluna C (2): Nome da funcionalidade
Coluna D (3): Nome da função
Coluna G (6): Tipo (ALI/AIE)
Coluna H (7): Complexidade
Coluna I (8): PF (Pontos de Função)
Coluna J (9): Fator de Ajuste
Coluna K (10): % Impacto
Coluna L (11): Gross PF
Coluna M (12): Sustentação/Justificativa
Coluna N (13): Status da função
Execução:
Cria nova instância FuncaoDados
Extrai valores das colunas usando getCellValueAsString() e getCellValueAsNumber()
Busca ou cria Modulo: buscarOuCriarModulo(nomeModulo, sistema)
Busca ou cria Funcionalidade: buscarOuCriarFuncionalidade(nomeFuncionalidade, modulo)
Mapeia tipo para enum TipoFuncaoDados
Mapeia complexidade para enum Complexidade
Define valores de PF e GrossPF
Extrai nome do Fator de Ajuste (coluna J) - será validado posteriormente
Define ordem sequencial: funcao.setOrdem(ordem.getAndIncrement())
Retorna objeto FuncaoDados (ainda não persistido)
Observação: Na análise Estimada, DERs e RLRs não são extraídos do Excel (diferença para Detalhada).

Método 1.6: setarFuncaoTransacaoEstimada
private FuncaoTransacao setarFuncaoTransacaoEstimada(XSSFRow row, Analise analise, AtomicLong ordem)
Descrição: Extrai dados de uma função de transação da linha Excel (análise Estimada)
Colunas Lidas: Semelhantes às de função de dados, com tipo (EE/SE/CE) ao invés de (ALI/AIE)
Execução: Análoga ao setarFuncaoDadosEstimada, retornando FuncaoTransacao
Observação: Na análise Estimada, DERs e ALRs não são extraídos do Excel.

Método 1.7: buscarOuCriarModulo
private Modulo buscarOuCriarModulo(String nomeModulo, Sistema sistema)
Descrição: Busca módulo existente por nome e sistema, ou cria novo se não encontrado
Execução:
Busca módulo no moduloRepository filtrando por nome exato e sistemaId
Se não encontrado, cria novo módulo com nome e sistema
Persiste novo módulo
Retorna módulo (existente ou novo)

Método 1.8: buscarOuCriarFuncionalidade
private Funcionalidade buscarOuCriarFuncionalidade(String nomeFuncionalidade, Modulo modulo)
Descrição: Busca funcionalidade existente por nome e módulo, ou cria nova se não encontrada
Execução:
Busca funcionalidade no funcionalidadeRepository filtrando por nome exato e moduloId
Se não encontrada, cria nova funcionalidade com nome e módulo
Persiste nova funcionalidade
Retorna funcionalidade (existente ou nova)

Método 1.9: converterParaUploadDTO
private AnaliseUploadDTO converterParaUploadDTO(Analise analise)
Descrição: Converte entidade Analise para AnaliseUploadDTO para serialização JSON
Execução:
Cria AnaliseUploadDTO
Copia dados gerais da análise (identificador, número OS, método, escopo, etc.)
Converte funções de dados para List<FuncaoUploadDTO>
Incluindo módulo e funcionalidade completos para cada função
Incluindo DERs e RLRs (se existirem)
Incluindo Fator de Ajuste
Converte funções de transação para List<FuncaoUploadDTO>
Incluindo módulo, funcionalidade, DERs, ALRs, Fator de Ajuste
Retorna DTO pronto para envio ao frontend

ETAPA 2: IMPORTAÇÃO E PERSISTÊNCIA
Método 2.1: importarAnaliseExcel
@Transactional
public Analise importarAnaliseExcel(AnaliseEditDTO analiseDTO)
Descrição: Método transacional principal de importação que persiste análise e funções
Entrada: AnaliseEditDTO (vindo do frontend com mapeamento de fatores de ajuste)
Execução:
Obtém usuário logado: analiseFacade.obterUsuarioPorLogin()
Converte DTO para entidade: converterEditDtoParaEntidade(analiseDTO) → analiseOrigem
VALIDAÇÃO DE FATORES DE AJUSTE:
Chama validarFatoresAjuste(manual, funcaoDados, funcaotransacao, mapaFatorAjuste)
Se algum fator não for encontrado, lança FatorAjusteException
Limpa sessão JPA: entityManager.clear() (desanexa entidades carregadas)
Recarrega usuário após limpeza
Cria nova instância Analise copiando dados de analiseOrigem
Adiciona sufixo " Importada" ao identificador
Associa usuário criador
Valida e busca Sistema no banco
Extrai funções de dados e transação de analiseOrigem
SALVA APENAS NO BANCO: analiseFacade.salvarAnaliseApenasDB(analise)
SALVA FUNÇÕES: salvarFuncoesExcel(funcaoDados, funcaotransacao, analise, mapaFatorAjuste)
SALVA NO ELASTICSEARCH: analiseFacade.salvarAnaliseApenasES(analise)
Retorna análise persistida

Método 2.2: converterEditDtoParaEntidade (delegado para AnaliseFacade)
public Analise converterEditDtoParaEntidade(AnaliseEditDTO analiseEditDTO)
Descrição: Converte AnaliseEditDTO em entidade Analise usando ModelMapper
Execução: Delega para analiseFacade.converterEditDtoParaEntidade(analiseEditDTO)

Método 2.3: validarFatoresAjuste
private void validarFatoresAjuste(
    Manual manual, 
    Set<FuncaoDados> funcaoDados, 
    Set<FuncaoTransacao> funcaotransacao, 
    Map<String, Long> mapaFatorAjuste
)
Descrição: Valida se todos os fatores de ajuste das funções existem no manual ou no mapa De-Para
Execução:
Busca todos fatores de ajuste do Manual no banco
Para cada função de dados com fator de ajuste:
Verifica se nome do fator está no mapa De-Para (mapaFatorAjuste)
Se não, verifica se existe fator com mesmo nome no manual
Se não encontrado em nenhum lugar, adiciona à lista de não encontrados
Repete para funções de transação
Se houver fatores não encontrados, lança FatorAjusteException com lista
Mapeia fatores de ajuste: Para cada função, substitui o fator temporário pelo fator do banco (usando ID do mapa ou busca por nome)

Método 2.4: salvarFuncoesExcel
public void salvarFuncoesExcel(
    Set<FuncaoDados> funcaoDados, 
    Set<FuncaoTransacao> funcaotransacao, 
    Analise analise, 
    Map<String, Long> mapaFatorAjuste
)
Descrição: Coordena salvamento de funções de dados e transação
Execução:
Cria contadores separados:
AtomicLong ordemDados = 1L
AtomicLong ordemTransacao = 1L
Chama salvarFuncaoDadosExcel(funcaoDados, analise, mapaFatorAjuste, ordemDados)
Chama salvarFuncaoTransacaoExcel(funcaotransacao, analise, mapaFatorAjuste, ordemTransacao)
Executa entityManager.flush() para persistir dados no banco
Chama atualizarPF(analise) para recalcular totais
Chama salvarAnalise(analise) para atualizar análise com totais

Método 2.5: salvarFuncaoDadosExcel
private void salvarFuncaoDadosExcel(
    Set<FuncaoDados> funcaoDados, 
    Analise analise, 
    Map<String, Long> mapaFatorAjuste, 
    AtomicLong ordemDados
)
Descrição: Persiste todas as funções de dados no banco
Execução (para cada função de dados):
Ordena funções por ID temporário
Cria nova instância FuncaoDados (não usar entidades gerenciadas)
Copia todos os campos primitivos (name, sustantation, impacto, tipo, complexidade, pf, grossPF, etc.)
Copia FatorAjuste (já validado e mapeado)
Copia Funcionalidade (que contém o módulo)
Associa à Analise
Define ordem sequencial: novaFuncao.setOrdem(ordemDados.getAndIncrement())
Chama verificarFuncoes(novaFuncao, analise, mapaFatorAjuste) - ajusta fator se necessário
CÁLCULO DO GROSSPF:
Se fator ≠ 100% e ≠ 0: GrossPF = PF / (Fator/100)
Arredondamento: 4 casas decimais (HALF_UP)
Copia DERs (cria novos objetos Der associados à nova função)
Copia RLRs (cria novos objetos Rlr associados à nova função)
Chama setarFuncionalidadeFuncao(novaFuncao, analise) - garante funcionalidade válida
PERSISTE: analiseFacade.salvarFuncaoDado(novaFuncao)
Observação para Estimada: DERs e RLRs estarão vazios pois não são extraídos do Excel Estimada.

Método 2.6: salvarFuncaoTransacaoExcel
private void salvarFuncaoTransacaoExcel(
    Set<FuncaoTransacao> funcaotransacao, 
    Analise analise, 
    Map<String, Long> mapaFatorAjuste, 
    AtomicLong ordemTransacao
)
Descrição: Persiste todas as funções de transação no banco
Execução: Análoga ao salvarFuncaoDadosExcel, mas para funções de transação
Cria objetos FuncaoTransacao
Copia campos específicos (ftrStr ao invés de retStr/detStr)
Copia DERs e ALRs (ao invés de RLRs)
Define ordem sequencial independente: ordemTransacao.getAndIncrement()
Calcula GrossPF baseado em fator de ajuste
Persiste: analiseFacade.salvarFuncaoTransacao(novaFuncao)
Observação para Estimada: DERs e ALRs estarão vazios.

2.4 BACKEND - Camada de Facade
Classe: AnaliseFacade
Arquivo: backend/src/main/java/br/com/basis/abaco/service/facades/AnaliseFacade.java
Descrição: Facade que encapsula operações complexas e coordenação entre serviços
Método: converterEditDtoParaEntidade
public Analise converterEditDtoParaEntidade(AnaliseEditDTO analise)
Descrição: Usa ModelMapper para converter DTO em entidade com logging detalhado
Execução:
Registra logs de conversão (funções, fatores de ajuste, módulos)
Aplica conversão: modelMapper.map(analise, Analise.class)
Valida se fatores de ajuste, módulos e funcionalidades foram preservados
Retorna entidade Analise com todas as relações
Método: salvarAnaliseApenasDB
public void salvarAnaliseApenasDB(Analise analise)
Descrição: Persiste análise APENAS no banco de dados relacional (PostgreSQL)
Não atualiza ElasticSearch
Método: salvarAnaliseApenasES
public void salvarAnaliseApenasES(Analise analise)
Descrição: Persiste análise APENAS no ElasticSearch (índice de busca)
Chamado após salvamento completo no banco
Método: salvarFuncaoDado
public void salvarFuncaoDado(FuncaoDados funcao)
Descrição: Persiste função de dados no banco de dados
Método: salvarFuncaoTransacao
public void salvarFuncaoTransacao(FuncaoTransacao funcao)
Descrição: Persiste função de transação no banco de dados

2.5 BACKEND - DTOs (Objetos de Transferência de Dados)
DTO 1: AnaliseUploadDTO
Arquivo: backend/src/main/java/br/com/basis/abaco/service/dto/upload/AnaliseUploadDTO.java
Descrição: DTO usado para retornar dados da análise após leitura do Excel
Campos:
identificadorAnalise: String
numeroOs: String
metodoContagem: String
escopo: String
propositoContagem: String
bloqueiaAnalise: Boolean
enviarBaseline: Boolean
funcaoDados: List<FuncaoUploadDTO>
funcaoTransacao: List<FuncaoUploadDTO>

DTO 2: FuncaoUploadDTO
Arquivo: backend/src/main/java/br/com/basis/abaco/service/dto/upload/FuncaoUploadDTO.java
Descrição: DTO para funções (dados e transação) com módulo e funcionalidade completos
Campos:
id: Long (temporário)
name: String
funcionalidade: FuncionalidadeUploadDTO (inclui módulo)
tipo: String
complexidade: Complexidade (enum)
pf: BigDecimal
grossPF: BigDecimal
sustantation: String
statusFuncao: StatusFuncao (enum)
impacto: ImpactoFatorAjuste (enum)
ordem: Long
fatorAjuste: FatorAjusteUploadDTO
ders: Set<DerDTO>
rlrs: Set<RlrDTO> (apenas para funções de dados)
alrs: Set<AlrDTO> (apenas para funções de transação)

DTO 3: AnaliseEditDTO
Arquivo: backend/src/main/java/br/com/basis/abaco/service/dto/AnaliseEditDTO.java
Descrição: DTO usado para receber dados de análise do frontend para importação
Herda: AnaliseBaseDTO
Campos Principais:
Todos os campos de AnaliseBaseDTO (identificador, sistema, organização, etc.)
funcaoDados: Set<FuncaoDados>
funcaoTransacao: Set<FuncaoTransacao>
mapaFatorAjuste: Map<String, Long> - Mapa De-Para de fatores de ajuste
manual: Manual
users: Set<User>
escopo, fronteiras, documentacao, propositoContagem, etc.

2.6 BACKEND - Entidades de Domínio
Entidade 1: Analise
Arquivo: backend/src/main/java/br/com/basis/abaco/domain/Analise.java
Descrição: Entidade JPA principal representando uma análise de pontos de função
Relações:
@ManyToOne: Sistema, Organizacao, TipoEquipe, Status, Manual, Contrato
@OneToMany: FuncaoDados, FuncaoTransacao, EsforcoFases, Compartilhadas
@ManyToMany: Users

Entidade 2: FuncaoDados
Arquivo: backend/src/main/java/br/com/basis/abaco/domain/FuncaoDados.java
Descrição: Entidade JPA representando função de dados (ALI ou AIE)
Relações:
@ManyToOne: Analise, Funcionalidade, FatorAjuste
@OneToMany: Ders, Rlrs

Entidade 3: FuncaoTransacao
Arquivo: backend/src/main/java/br/com/basis/abaco/domain/FuncaoTransacao.java
Descrição: Entidade JPA representando função de transação (EE, SE ou CE)
Relações:
@ManyToOne: Analise, Funcionalidade, FatorAjuste
@OneToMany: Ders, Alrs

Entidade 4: Modulo
Arquivo: backend/src/main/java/br/com/basis/abaco/domain/Modulo.java
Descrição: Representa módulo de um sistema
Relações:
@ManyToOne: Sistema
@OneToMany: Funcionalidades

Entidade 5: Funcionalidade
Arquivo: backend/src/main/java/br/com/basis/abaco/domain/Funcionalidade.java
Descrição: Representa funcionalidade dentro de um módulo
Relações:
@ManyToOne: Modulo

Entidade 6: FatorAjuste
Arquivo: backend/src/main/java/br/com/basis/abaco/domain/FatorAjuste.java
Descrição: Representa fator de ajuste aplicado a funções
Relações:
@ManyToOne: Manual

Entidade 7: Der (Data Element Referenced)
Descrição: Elemento de dado referenciado em função
Relações: @ManyToOne com FuncaoDados ou FuncaoTransacao

Entidade 8: Rlr (Record Element Type Referenced)
Descrição: Tipo de registro lógico referenciado em função de dados
Relações: @ManyToOne com FuncaoDados

Entidade 9: Alr (Application Logic Referenced)
Descrição: Arquivo lógico referenciado em função de transação
Relações: @ManyToOne com FuncaoTransacao

2.7 BACKEND - Repositórios
AnaliseRepository
Descrição: Repositório JPA para entidade Analise

FuncaoDadosRepository
Descrição: Repositório JPA para entidade FuncaoDados

FuncaoTransacaoRepository
Descrição: Repositório JPA para entidade FuncaoTransacao

ModuloRepository
Descrição: Repositório JPA para entidade Modulo
Métodos Customizados:
Busca módulo por nome e sistemaId

FuncionalidadeRepository
Descrição: Repositório JPA para entidade Funcionalidade
Métodos Customizados:
Busca funcionalidade por nome e moduloId

FatorAjusteRepository
Descrição: Repositório JPA para entidade FatorAjuste
Métodos Customizados:
Busca fatores de ajuste por manual

2.8 BACKEND - Métodos Auxiliares
getCellValueAsString
private String getCellValueAsString(XSSFRow row, int columnIndex)
Descrição: Lê valor de célula Excel como String, tratando fórmulas e valores diretos
Retorno: String com valor da célula (ou resultado da fórmula)

getCellValueAsNumber
private double getCellValueAsNumber(XSSFRow row, int columnIndex)
Descrição: Lê valor numérico de célula Excel
Retorno: double com valor numérico (ou resultado da fórmula numérica)

verificarFuncoes
private void verificarFuncoes(FuncaoAnalise funcao, Analise analise, Map<String, Long> mapaFatorAjuste)
Descrição: Ajusta fator de ajuste da função usando mapa De-Para se necessário

setarFuncionalidadeFuncao
private void setarFuncionalidadeFuncao(FuncaoAnalise funcao, Analise analise)
Descrição: Garante que função tem funcionalidade válida, criando ou buscando no banco

3. Componentes Comuns entre Estimada e Detalhada
Os seguintes componentes são compartilhados entre os fluxos de importação de análise Estimada e Detalhada:

3.1 Endpoints REST (Idênticos)
AnaliseResource.carregarArquivoExcel - POST /api/analises/importar-excel/Xlsx
AnaliseResource.importarAnaliseExcel - POST /api/analises/importar-Excel

3.2 Serviços Principais (Comuns)
AnaliseService.uploadExcelComDTO - Processa Excel e retorna DTO
AnaliseService.uploadExcel - Processa Excel (decide qual aba ler)
AnaliseService.setarResumoExcelUpload - Extrai dados gerais
AnaliseService.importarAnaliseExcel - Persiste análise
AnaliseService.converterEditDtoParaEntidade - Converte DTO em entidade
AnaliseService.validarFatoresAjuste - Valida fatores de ajuste
AnaliseService.salvarFuncoesExcel - Salva todas as funções
AnaliseService.salvarFuncaoDadosExcel - Salva funções de dados
AnaliseService.salvarFuncaoTransacaoExcel - Salva funções de transação
AnaliseService.converterParaUploadDTO - Converte entidade para DTO
AnaliseService.buscarOuCriarModulo - Busca/cria módulo
AnaliseService.buscarOuCriarFuncionalidade - Busca/cria funcionalidade

3.3 Facade (Comuns)
AnaliseFacade.converterEditDtoParaEntidade
AnaliseFacade.salvarAnaliseApenasDB
AnaliseFacade.salvarAnaliseApenasES
AnaliseFacade.salvarFuncaoDado
AnaliseFacade.salvarFuncaoTransacao

3.4 DTOs (Comuns)
AnaliseUploadDTO
FuncaoUploadDTO
AnaliseEditDTO

3.5 Entidades (Comuns)
Analise
FuncaoDados
FuncaoTransacao
Modulo
Funcionalidade
FatorAjuste
Der
Rlr
Alr

3.6 Repositórios (Comuns)
AnaliseRepository
FuncaoDadosRepository
FuncaoTransacaoRepository
ModuloRepository
FuncionalidadeRepository
FatorAjusteRepository

3.7 Métodos Auxiliares (Comuns)
getCellValueAsString
getCellValueAsNumber
verificarFuncoes
setarFuncionalidadeFuncao

4. Componentes Específicos de Estimada
Os seguintes componentes são específicos para o fluxo de análise Estimada:

4.1 Métodos Específicos
setarExcelEstimadaUpload
Descrição: Processa aba "AFP - Estimativa" do Excel
Diferença: Lê linhas 10 a 1081 da aba específica

setarFuncaoDadosEstimada
Descrição: Extrai função de dados da linha da planilha Estimada
Diferença:
Não lê DERs e RLRs (não existem na planilha Estimada)
Lê PF e Gross PF diretamente calculados

setarFuncaoTransacaoEstimada
Descrição: Extrai função de transação da linha da planilha Estimada
Diferença:
Não lê DERs e ALRs (não existem na planilha Estimada)
Lê PF e Gross PF diretamente calculados

4.2 Estrutura da Planilha Estimada
Aba: "AFP - Estimativa"
Linhas Processadas: 10 a 1081
Colunas:
A (0): ID temporário
B (1): Módulo
C (2): Funcionalidade
D (3): Nome da função
E (?): (não utilizada)
F (?): (não utilizada)
G (6): Tipo (ALI, AIE, EE, SE, CE)
H (7): Complexidade (BAIXA, MEDIA, ALTA)
I (8): PF (Pontos de Função)
J (9): Fator de Ajuste (nome)
K (10): % Impacto
L (11): Gross PF
M (12): Sustentação/Justificativa
N (13): Status da função

5. Componentes Específicos de Detalhada
Os seguintes componentes são específicos para o fluxo de análise Detalhada:

5.1 Métodos Específicos
setarExcelDetalhadaUpload
Descrição: Processa aba "AFP - Detalhada" do Excel
Diferença: Lê linhas 10 (índice 9) a 1323 (índice 1322) da aba específica

setarFuncaoDadosDetalhada
Descrição: Extrai função de dados da linha da planilha Detalhada
Diferença:
Lê DERs e RLRs da planilha (contagens detalhadas)
PF é calculado baseado em complexidade e quantidade de DERs/RLRs

setarFuncaoTrasacaoDetalhada
Descrição: Extrai função de transação da linha da planilha Detalhada
Diferença:
Lê DERs e ALRs da planilha (contagens detalhadas)
PF é calculado baseado em complexidade e quantidade de DERs/ALRs

5.2 Estrutura da Planilha Detalhada
Aba: "AFP - Detalhada"
Linhas Processadas: 10 a 1323
Colunas (incluem colunas adicionais para contagens):
Mesmas colunas básicas da Estimada
Colunas adicionais para:
Lista de DERs (nomes e valores)
Lista de RLRs (para funções de dados)
Lista de ALRs (para funções de transação)
Contagens detalhadas que influenciam cálculo de complexidade

6. Principais Diferenças entre Estimada e Detalhada
Aspecto	Análise Estimada	Análise Detalhada
Aba Excel	"AFP - Estimativa"	"AFP - Detalhada"
Linhas Processadas	10 a 1081	10 a 1323
DERs	❌ Não lidos	✅ Lidos e persistidos
RLRs	❌ Não lidos	✅ Lidos e persistidos
ALRs	❌ Não lidos	✅ Lidos e persistidos
Cálculo de PF	Direto da planilha	Baseado em DERs/RLRs/ALRs
Gross PF	Lido da planilha	Pode ser recalculado
Complexidade	Lida da planilha	Calculada baseada em contagens
Método Processador	setarExcelEstimadaUpload	setarExcelDetalhadaUpload
Extrator FD	setarFuncaoDadosEstimada	setarFuncaoDadosDetalhada
Extrator FT	setarFuncaoTransacaoEstimada	setarFuncaoTrasacaoDetalhada

7. Ordem de Execução Completa (Resumo)
Etapa 1: Carregamento do Excel
Frontend: Usuário seleciona arquivo Excel
Endpoint: POST /api/analises/importar-excel/Xlsx
Resource: AnaliseResource.carregarArquivoExcel(file)
Service: AnaliseService.uploadExcelComDTO(file)
Service: AnaliseService.uploadExcel(file)
5.1. Cria arquivo temporário
5.2. Abre workbook Excel
5.3. setarResumoExcelUpload() - extrai dados gerais
5.4. Detecta método de contagem
5.5. setarExcelEstimadaUpload() ⭐ (para Estimada)
5.5.1. Para cada linha válida da aba "AFP - Estimativa":
Se função de dados: setarFuncaoDadosEstimada()
Extrai dados da linha
buscarOuCriarModulo()
buscarOuCriarFuncionalidade()
Retorna FuncaoDados não persistida
Se função de transação: setarFuncaoTransacaoEstimada()
Extrai dados da linha
buscarOuCriarModulo()
buscarOuCriarFuncionalidade()
Retorna FuncaoTransacao não persistida
5.6. Retorna Analise com funções
Service: converterParaUploadDTO(analise)
Response: Retorna AnaliseUploadDTO para frontend

Etapa 2: Importação e Persistência
Frontend: Usuário valida dados e mapeia fatores de ajuste (De-Para)
Endpoint: POST /api/analises/importar-Excel
Resource: AnaliseResource.importarAnaliseExcel(analiseDTO)
10.1. verificaModulos() - valida módulos
Service: AnaliseService.importarAnaliseExcel(analiseDTO)
11.1. obterUsuarioPorLogin()
11.2. converterEditDtoParaEntidade(analiseDTO) → analiseOrigem
11.2.1. Facade: AnaliseFacade.converterEditDtoParaEntidade()
ModelMapper converte DTO em entidade
11.3. validarFatoresAjuste() - valida e mapeia fatores
Se inválido: lança FatorAjusteException
11.4. entityManager.clear() - limpa sessão
11.5. Cria nova Analise copiando dados
11.6. Adiciona sufixo " Importada" ao identificador
11.7. Facade: salvarAnaliseApenasDB(analise) - persiste análise
11.8. salvarFuncoesExcel()
11.8.1. salvarFuncaoDadosExcel()
Para cada função de dados:
Cria nova instância
Copia dados
Define ordem sequencial
verificarFuncoes() - ajusta fator
Calcula Gross PF baseado em fator de ajuste
Copia DERs (vazio para Estimada)
Copia RLRs (vazio para Estimada)
setarFuncionalidadeFuncao()
Facade: salvarFuncaoDado() - persiste
11.8.2. salvarFuncaoTransacaoExcel()
Para cada função de transação:
Cria nova instância
Copia dados
Define ordem sequencial
verificarFuncoes() - ajusta fator
Calcula Gross PF baseado em fator de ajuste
Copia DERs (vazio para Estimada)
Copia ALRs (vazio para Estimada)
setarFuncionalidadeFuncao()
Facade: salvarFuncaoTransacao() - persiste
11.8.3. entityManager.flush() - persiste tudo no banco
11.8.4. atualizarPF() - recalcula totais
11.8.5. salvarAnalise() - atualiza análise
11.9. Facade: salvarAnaliseApenasES(analise) - indexa no ElasticSearch
11.10. Retorna Analise persistida
Response: Retorna análise para frontend
Frontend: Exibe mensagem de sucesso e redireciona

8. Observações Técnicas Importantes
8.1 Transação e Persistência
Todo processo de importação ocorre em uma única transação (@Transactional)
Se houver erro (ex: validação de fator de ajuste), há rollback completo
Salvamento no banco ocorre ANTES de salvamento no ElasticSearch

8.2 Ordem das Funções
Funções de dados têm numeração independente (1, 2, 3...)
Funções de transação têm numeração independente (1, 2, 3...)
Ordem é definida pela sequência de leitura no Excel

8.3 Fatores de Ajuste
Validação ocorre ANTES de persistência
Usa mapa De-Para (mapaFatorAjuste) para mapear nomes de fatores do Excel para IDs no banco
Se fator não encontrado, retorna erro com lista de fatores inválidos

8.4 Cálculo de Gross PF
Se fator de ajuste ≠ 100%: GrossPF = PF / (Fator/100)
Exemplo: PF=10, Fator=50% → GrossPF = 10 / 0.5 = 20
Arredondamento: 4 casas decimais (HALF_UP)

8.5 Módulo e Funcionalidade
Sistema busca módulo existente por nome exato e sistemaId
Se não encontrado, cria automaticamente
Mesmo comportamento para funcionalidades

Conclusão
O fluxo de importação de análise Estimada compartilha a maioria dos componentes com o fluxo Detalhado, diferindo principalmente nos métodos de leitura da planilha Excel (setarExcelEstimadaUpload vs setarExcelDetalhadaUpload) e na ausência de leitura de DERs/RLRs/ALRs na análise Estimada.

Componentes-chave:
Endpoints: carregarArquivoExcel, importarAnaliseExcel
Serviços: uploadExcel, setarExcelEstimadaUpload, importarAnaliseExcel, salvarFuncoesExcel
DTOs: AnaliseUploadDTO, AnaliseEditDTO, FuncaoUploadDTO
Entidades: Analise, FuncaoDados, FuncaoTransacao, Modulo, Funcionalidade, FatorAjuste
