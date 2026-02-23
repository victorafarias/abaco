package br.com.basis.abaco.service;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Funcionalidade;
import br.com.basis.abaco.domain.Modulo;
import br.com.basis.abaco.domain.Sistema;
import br.com.basis.abaco.repository.AnaliseRepository;
import br.com.basis.abaco.repository.FuncaoDadosRepository;
import br.com.basis.abaco.repository.FuncaoTransacaoRepository;
import br.com.basis.abaco.repository.SistemaRepository;
import br.com.basis.abaco.repository.search.SistemaSearchRepository;
import br.com.basis.abaco.service.dto.FuncaoDistintaDTO;
import br.com.basis.abaco.service.dto.RenomearFuncaoDTO;
import br.com.basis.abaco.service.dto.SistemaDropdownDTO;
import br.com.basis.abaco.service.dto.SistemaListDTO;
import br.com.basis.abaco.service.dto.filter.SistemaFilterDTO;
import br.com.basis.abaco.service.exception.RelatorioException;
import br.com.basis.abaco.service.relatorio.RelatorioSistemaColunas;
import br.com.basis.abaco.utils.AbacoUtil;
import br.com.basis.dynamicexports.service.DynamicExportsService;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;

@Service
@Transactional
public class SistemaService extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(SistemaService.class);

    private final SistemaRepository sistemaRepository;
    private final SistemaSearchRepository sistemaSearchRepository;
    private final DynamicExportsService dynamicExportsService;
    private final ModelMapper modelMapper;
    private final AnaliseRepository analiseRepository;
    private final FuncaoDadosRepository funcaoDadosRepository;
    private final FuncaoTransacaoRepository funcaoTransacaoRepository;

    public SistemaService(
            SistemaRepository sistemaRepository,
            SistemaSearchRepository sistemaSearchRepository,
            DynamicExportsService dynamicExportsService,
            ModelMapper modelMapper,
            AnaliseRepository analiseRepository,
            FuncaoDadosRepository funcaoDadosRepository,
            FuncaoTransacaoRepository funcaoTransacaoRepository) {
        this.sistemaRepository = sistemaRepository;
        this.sistemaSearchRepository = sistemaSearchRepository;
        this.dynamicExportsService = dynamicExportsService;
        this.modelMapper = modelMapper;
        this.analiseRepository = analiseRepository;
        this.funcaoDadosRepository = funcaoDadosRepository;
        this.funcaoTransacaoRepository = funcaoTransacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<SistemaDropdownDTO> getSistemaDropdown() {
        return sistemaRepository.getSistemaDropdown();
    }

    public BoolQueryBuilder bindFilterSearch(String nome, String sigla, String numeroOcorrencia , Long [] organizacao) {
        BoolQueryBuilder qb = new BoolQueryBuilder();
        mustMatchWildcardContainsQueryLowerCase(nome, qb, "nomeSearch");
        mustMatchWildcardContainsQueryLowerCase(sigla, qb, "siglaSearch");
        mustMatchWildcardContainsQueryLowerCase(numeroOcorrencia, qb, "numeroOcorrenciaSearch");
        if(organizacao != null && organizacao.length > 0 ){
            BoolQueryBuilder boolQueryBuilderOrganizacao = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("organizacao.id", organizacao));
            qb.must(boolQueryBuilderOrganizacao);
        }
        return qb;
    }

    public Sistema saveSistema( Sistema sistema) {
        Sistema result = sistemaRepository.save(sistema);
        sistemaSearchRepository.save(convertToEntity(convertToAnaliseEditDTO(result)));
        return result;
    }

    public Sistema convertToEntity(SistemaListDTO sistemaListDTO) {
        return modelMapper.map(sistemaListDTO, Sistema.class);
    }

    public SistemaListDTO convertToAnaliseEditDTO(Sistema sistema) {
        return modelMapper.map(sistema, SistemaListDTO.class);
    }


    public ByteArrayOutputStream gerarRelatorio(SistemaFilterDTO filtro, String tipoRelatorio) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            Long[] organizacoes = null;
            if(filtro.getOrganizacao() != null && !filtro.getOrganizacao().isEmpty()) {
                organizacoes = new Long[filtro.getOrganizacao().size()];
            }
            BoolQueryBuilder qb = bindFilterSearch(filtro.getNome(), filtro.getSigla(), filtro.getNumeroOcorrencia(), organizacoes != null ? filtro.getOrganizacao().toArray(organizacoes) : null);
            SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(qb).withPageable(dynamicExportsService.obterPageableMaximoExportacao()).build();
            Page<Sistema> page = sistemaSearchRepository.search(searchQuery);

            byteArrayOutputStream = dynamicExportsService.export(new RelatorioSistemaColunas(), page, tipoRelatorio, Optional.empty(), Optional.ofNullable(AbacoUtil.REPORT_LOGO_PATH), Optional.ofNullable(AbacoUtil.getReportFooter()));
        } catch (DRException | ClassNotFoundException | JRException | NoClassDefFoundError e) {
            throw new RelatorioException(e);
        }
        return byteArrayOutputStream;
    }

    public List<Sistema> getAll() {
        return sistemaRepository.findAll();
    }

    /**
     * Recupera todas as funções distintas (combinação única de Módulo, Funcionalidade e Nome)
     * de todas as análises associadas a um sistema.
     * 
     * OTIMIZAÇÃO: Este método foi refatorado para eliminar o problema N+1.
     * Antes: 201 queries (1 para analises + 100 para FD + 100 para FT)
     * Agora: 2 queries (1 para FD + 1 para FT) com DISTINCT no banco.
     * 
     * @param sistemaId ID do sistema
     * @return Set de funções distintas ordenadas
     */
    @Transactional(readOnly = true)
    public Set<FuncaoDistintaDTO> getFuncoesDistintas(Long sistemaId) {
        log.debug("[SistemaService] Buscando funções distintas para o sistema ID: {}", sistemaId);
        
        // LinkedHashSet para manter a ordem
        Set<FuncaoDistintaDTO> funcoesDistintas = new java.util.LinkedHashSet<>();
        
        // Buscar funções de dados (1 query otimizada com DISTINCT)
        List<Object[]> funcoesDados = funcaoDadosRepository.findFuncoesDistintasDadosBySistemaId(sistemaId);
        log.debug("[SistemaService] Encontradas {} funções de dados distintas", funcoesDados.size());
        
        for (Object[] row : funcoesDados) {
            FuncaoDistintaDTO dto = new FuncaoDistintaDTO(
                (String) row[0],  // nomeModulo
                (String) row[1],  // nomeFuncionalidade
                (String) row[2],  // nomeFuncao
                (String) row[3]   // tipo = "FD"
            );
            funcoesDistintas.add(dto);
        }
        
        // Buscar funções de transação (1 query otimizada com DISTINCT)
        List<Object[]> funcoesTransacao = funcaoTransacaoRepository.findFuncoesDistintasTransacaoBySistemaId(sistemaId);
        log.debug("[SistemaService] Encontradas {} funções de transação distintas", funcoesTransacao.size());
        
        for (Object[] row : funcoesTransacao) {
            FuncaoDistintaDTO dto = new FuncaoDistintaDTO(
                (String) row[0],  // nomeModulo
                (String) row[1],  // nomeFuncionalidade
                (String) row[2],  // nomeFuncao
                (String) row[3]   // tipo = "FT"
            );
            funcoesDistintas.add(dto);
        }
        
        log.debug("[SistemaService] Total de funções distintas encontradas: {}", funcoesDistintas.size());
        return funcoesDistintas;
    }

    /**
     * Busca funções distintas com paginação.
     * 
     * Este método usa as queries otimizadas da Fase 1 e aplica paginação em memória
     * para combinar FD + FT mantendo a ordenação correta.
     * 
     * @param sistemaId ID do sistema
     * @param pageable Configuração de paginação
     * @return Página de FuncaoDistintaDTO
     */
    @Transactional(readOnly = true)
    public Page<FuncaoDistintaDTO> getFuncoesDistintasPaged(Long sistemaId, Pageable pageable) {
        log.debug("[SistemaService] Buscando funções distintas paginadas para sistema: {}, page: {}, size: {}", 
            sistemaId, pageable.getPageNumber(), pageable.getPageSize());
        
        // Combinar FD + FT em uma única lista usando queries otimizadas
        List<FuncaoDistintaDTO> todasFuncoes = new java.util.ArrayList<>();
        
        // Buscar todas FD (query otimizada da Fase 1)
        List<Object[]> funcoesDados = funcaoDadosRepository.findFuncoesDistintasDadosBySistemaId(sistemaId);
        todasFuncoes.addAll(convertToDTO(funcoesDados));
        
        // Buscar todas FT (query otimizada da Fase 1)
        List<Object[]> funcoesTransacao = funcaoTransacaoRepository.findFuncoesDistintasTransacaoBySistemaId(sistemaId);
        todasFuncoes.addAll(convertToDTO(funcoesTransacao));
        
        log.debug("[SistemaService] Total de funções distintas (FD + FT): {}", todasFuncoes.size());
        
        // Aplicar paginação em memória (necessário para combinar FD + FT)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), todasFuncoes.size());
        
        // Garantir que start não exceda o tamanho da lista
        if (start > todasFuncoes.size()) {
            start = todasFuncoes.size();
        }
        
        List<FuncaoDistintaDTO> paginaAtual = todasFuncoes.subList(start, end);
        
        log.debug("[SistemaService] Retornando página {}: {} registros (de {} total)", 
            pageable.getPageNumber(), paginaAtual.size(), todasFuncoes.size());
        
        return new PageImpl<>(paginaAtual, pageable, todasFuncoes.size());
    }

    /**
     * Converte array de Object[] retornado pelas queries nativas em DTOs.
     * 
     * @param rows Lista de arrays [nomeModulo, nomeFuncionalidade, nomeFuncao, tipo]
     * @return Lista de FuncaoDistintaDTO
     */
    private List<FuncaoDistintaDTO> convertToDTO(List<Object[]> rows) {
        return rows.stream()
            .map(row -> new FuncaoDistintaDTO(
                (String) row[0],  // nomeModulo
                (String) row[1],  // nomeFuncionalidade
                (String) row[2],  // nomeFuncao
                (String) row[3]   // tipo
            ))
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Altera funções em lote. Para cada alteração, busca todas as ocorrências
     * da função com os dados atuais (mesmo Módulo, Funcionalidade e Nome) e atualiza
     * os campos que foram informados (novoNome, novoModulo, novaFuncionalidade).
     * 
     * @param sistemaId ID do sistema
     * @param renomeacoes Lista de alterações a serem aplicadas
     * @return Número de funções atualizadas
     */
    public int renomearFuncoes(Long sistemaId, List<RenomearFuncaoDTO> renomeacoes) {
        log.debug("[SistemaService] Alterando {} funções para o sistema ID: {}", renomeacoes.size(), sistemaId);
        
        int totalAtualizadas = 0;
        
        // Buscar todas as análises do sistema
        List<Analise> analises = analiseRepository.findAllBySistemaId(sistemaId);
        
        // Buscar o sistema para ter acesso aos módulos e funcionalidades
        Sistema sistema = sistemaRepository.findOne(sistemaId);
        
        for (RenomearFuncaoDTO renomeacao : renomeacoes) {
            log.debug("[SistemaService] Processando alteração: Módulo={}, Funcionalidade={}, Nome={}", 
                renomeacao.getNomeModulo(), renomeacao.getNomeFuncionalidade(), renomeacao.getNomeAtual());
            
            // Buscar a nova funcionalidade, se informada
            Funcionalidade novaFuncionalidade = null;
            if (renomeacao.getNovoModulo() != null || renomeacao.getNovaFuncionalidade() != null) {
                String moduloDestino = renomeacao.getNovoModulo() != null ? renomeacao.getNovoModulo() : renomeacao.getNomeModulo();
                String funcionalidadeDestino = renomeacao.getNovaFuncionalidade() != null ? renomeacao.getNovaFuncionalidade() : renomeacao.getNomeFuncionalidade();
                
                // Buscar a funcionalidade de destino
                for (Modulo modulo : sistema.getModulos()) {
                    if (moduloDestino.equals(modulo.getNome())) {
                        for (Funcionalidade func : modulo.getFuncionalidades()) {
                            if (funcionalidadeDestino.equals(func.getNome())) {
                                novaFuncionalidade = func;
                                break;
                            }
                        }
                        break;
                    }
                }
                
                if (novaFuncionalidade == null) {
                    log.warn("[SistemaService] Funcionalidade de destino não encontrada: Módulo={}, Funcionalidade={}", 
                        moduloDestino, funcionalidadeDestino);
                    continue;
                }
            }
            
            for (Analise analise : analises) {
                // Atualizar funções de dados
                Set<FuncaoDados> funcoesDados = funcaoDadosRepository.findByAnaliseId(analise.getId());
                for (FuncaoDados fd : funcoesDados) {
                    if (fd.getFuncionalidade() != null && 
                        fd.getFuncionalidade().getModulo() != null &&
                        renomeacao.getNomeModulo().equals(fd.getFuncionalidade().getModulo().getNome()) &&
                        renomeacao.getNomeFuncionalidade().equals(fd.getFuncionalidade().getNome()) &&
                        renomeacao.getNomeAtual().equals(fd.getName())) {
                        
                        // Atualizar nome se informado
                        if (renomeacao.getNovoNome() != null && !renomeacao.getNovoNome().isEmpty()) {
                            fd.setName(renomeacao.getNovoNome());
                        }
                        
                        // Atualizar funcionalidade se informada
                        if (novaFuncionalidade != null) {
                            fd.setFuncionalidade(novaFuncionalidade);
                        }
                        
                        funcaoDadosRepository.save(fd);
                        totalAtualizadas++;
                        log.debug("[SistemaService] Função de dados ID {} alterada", fd.getId());
                    }
                }
                
                // Atualizar funções de transação
                Set<FuncaoTransacao> funcoesTransacao = funcaoTransacaoRepository.findAllByAnaliseId(analise.getId());
                for (FuncaoTransacao ft : funcoesTransacao) {
                    if (ft.getFuncionalidade() != null && 
                        ft.getFuncionalidade().getModulo() != null &&
                        renomeacao.getNomeModulo().equals(ft.getFuncionalidade().getModulo().getNome()) &&
                        renomeacao.getNomeFuncionalidade().equals(ft.getFuncionalidade().getNome()) &&
                        renomeacao.getNomeAtual().equals(ft.getName())) {
                        
                        // Atualizar nome se informado
                        if (renomeacao.getNovoNome() != null && !renomeacao.getNovoNome().isEmpty()) {
                            ft.setName(renomeacao.getNovoNome());
                        }
                        
                        // Atualizar funcionalidade se informada
                        if (novaFuncionalidade != null) {
                            ft.setFuncionalidade(novaFuncionalidade);
                        }
                        
                        funcaoTransacaoRepository.save(ft);
                        totalAtualizadas++;
                        log.debug("[SistemaService] Função de transação ID {} alterada", ft.getId());
                    }
                }
            }
        }
        
        log.debug("[SistemaService] Total de funções atualizadas: {}", totalAtualizadas);
        return totalAtualizadas;
    }

}
