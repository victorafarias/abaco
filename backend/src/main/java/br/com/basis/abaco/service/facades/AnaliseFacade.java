package br.com.basis.abaco.service.facades;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Compartilhada;
import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.domain.VwAnaliseDivergenteSomaPf;
import br.com.basis.abaco.domain.VwAnaliseSomaPf;
import br.com.basis.abaco.repository.AnaliseRepository;
import br.com.basis.abaco.repository.search.AnaliseSearchRepository;
import br.com.basis.abaco.service.ConsultasService;
import br.com.basis.abaco.service.FuncoesService;
import br.com.basis.abaco.service.PlanilhaService;
import br.com.basis.abaco.service.RelatorioService;
import br.com.basis.abaco.service.dto.AnaliseDTO;
import br.com.basis.abaco.service.dto.AnaliseDivergenceDTO;
import br.com.basis.abaco.service.dto.AnaliseDivergenceEditDTO;
import br.com.basis.abaco.service.dto.AnaliseEditDTO;
import br.com.basis.abaco.service.dto.AnaliseJsonDTO;
import br.com.basis.abaco.service.dto.CompartilhadaDTO;
import br.com.basis.abaco.service.dto.filter.AnaliseFilterDTO;
import br.com.basis.abaco.service.dto.pesquisa.AnalisePesquisaDTO;
import br.com.basis.dynamicexports.pojo.PropriedadesRelatorio;
import br.com.basis.dynamicexports.pojo.ReportObject;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AnaliseFacade {
    private static final Logger log = LoggerFactory.getLogger(AnaliseFacade.class);
    
    public final AnaliseRepository analiseRepository;
    public final AnaliseSearchRepository analiseSearchRepository;
    public final FuncoesService funcoesService;
    public final ConsultasService consultasService;
    public final RelatorioService relatorioService;
    public final PlanilhaService planilhaService;
    private final ModelMapper modelMapper;

    public AnaliseFacade(AnaliseRepository analiseRepository,
                         AnaliseSearchRepository analiseSearchRepository,
                         FuncoesService funcoesService,
                         ConsultasService consultasService,
                         RelatorioService relatorioService,
                         PlanilhaService planilhaService, ModelMapper modelMapper) {
        this.analiseRepository = analiseRepository;
        this.analiseSearchRepository = analiseSearchRepository;
        this.funcoesService = funcoesService;
        this.consultasService = consultasService;
        this.relatorioService = relatorioService;
        this.planilhaService = planilhaService;
        this.modelMapper = modelMapper;
    }

    public User obterUsuarioComAutorizacao() {
        return consultasService.obterUsuarioComAutorizacao();
    }

    public User obterUsuarioPorLogin() {
        return consultasService.obterUsuarioPorLogin();
    }

    public Analise obterAnalisePorId(Long idAnalise) {
        return analiseRepository.findOneById(idAnalise);
    }

    public Analise obterAnalisePorIdLimpo(Long idAnalise) {
        return analiseRepository.findOneByIdClean(idAnalise);
    }

    public Analise obterAnaliseCompartilhada(Long idAnaliseCompartilhada) {
        return analiseRepository.getOne(idAnaliseCompartilhada);
    }

    public List<Analise> obterPorFuncaoDados(String nomeFuncao, String nomeModulo, String nomeFuncionalidade, String nomeSistema, String nomeEquipe) {
        return analiseRepository.obterPorFuncaoDados(nomeFuncao, nomeModulo, nomeFuncionalidade, nomeSistema, nomeEquipe);
    }

    public List<Analise> obterPorFuncaoTransacao(String nomeFuncao, String nomeModulo, String nomeFuncionalidade, String nomeSistema, String nomeEquipe) {
        return analiseRepository.obterPorFuncaoTransacao(nomeFuncao, nomeModulo, nomeFuncionalidade, nomeSistema, nomeEquipe);
    }

    public List<Analise> obterAnalisesDivergenciaForaDoPrazo() {
        return analiseRepository.obterAnalisesDivergenciaForaDoPrazo();
    }

    public Page<Analise> obterTodasAnalises() {
        return analiseSearchRepository.findAll(consultasService.obterPaginacaoMaximaExportacao());
    }

    public Page<Analise> obterPaginaAnalise(SearchQuery query) {
        return consultasService.obterPaginaAnalise(query);
    }

    public Page<Analise> obterPaginaAnaliseRelatorio(AnaliseFilterDTO filtro) {
        return consultasService.obterPaginaAnalise(relatorioService.obterQueryExportarRelatorio(filtro));
    }

    public Page<Analise> obterPaginaAnaliseDivergenciaRelatorio(AnaliseFilterDTO filtro) {
        return consultasService.obterPaginaAnalise(relatorioService.obterQueryExportarRelatorioDivergencia(filtro, consultasService.obterPaginacaoMaximaExportacao()));
    }

    public Pageable obterPaginacaoMaximaExportacao() {
        return consultasService.obterPaginacaoMaximaExportacao();
    }

    public Status obterStatusPorId(Long idstatus) {
        return consultasService.obterStatusPorId(idstatus);
    }

    public Status obterPrimeiroStatusPorDivergencia() {
        return consultasService.obterPrimeiroStatusPorDivergencia();
    }

    public Optional<Status> obterStatusPorNome(String nomeStatus) {
        return consultasService.obterStatusPorNome(nomeStatus);
    }

    public ByteArrayOutputStream exportar(PropriedadesRelatorio propriedadesRelatorio, Page<? extends ReportObject> page,
                                          String tipo, Optional<Map<String, String>> filtros, Optional<String> logoPath, Optional<String> footerMessage) throws JRException, DRException, ClassNotFoundException {
        return relatorioService.exportar(propriedadesRelatorio, page, tipo, filtros, logoPath, footerMessage);
    }

    public ByteArrayOutputStream selecionarModelo(Analise analise, Long modelo) throws IOException {
        return planilhaService.selecionarModelo(analise, modelo);
    }

    public ByteArrayOutputStream selecionarModeloDivergencia(Analise analise, Long modelo) throws IOException {
        return planilhaService.selecionarModeloDivergencia(analise, modelo);
    }

    public VwAnaliseSomaPf obterAnaliseSomaPfPorId(Long idAnalise) {
        return consultasService.obterAnaliseSomaPfPorId(idAnalise);
    }

    public VwAnaliseDivergenteSomaPf obterAnaliseDivergenteSomaPfPorId(Long idAnalise) {
        return consultasService.obterAnaliseDivergenteSomaPfPorId(idAnalise);
    }

    public BoolQueryBuilder obterBoolQueryBuilder(AnalisePesquisaDTO pesquisaDTO) {
        return relatorioService.getBoolQueryBuilder(pesquisaDTO);
    }

    public BoolQueryBuilder obterBoolQueryBuilderDivergencia(String identificador, Set<Long> sistema, Set<Long> organizacao,Set<Long> status,Boolean bloqueado) {
        return relatorioService.getBoolQueryBuilderDivergence(identificador, sistema, organizacao,status,bloqueado);
    }

    public Set<FuncaoDados> obterFuncaoDadosPorAnaliseId(Long idAnalise) {
        return funcoesService.obterFuncaoDadosPorAnaliseId(idAnalise);
    }

    public Set<FuncaoTransacao> obterFuncaoTransacaoPorAnaliseId(Long idAnalise) {
        return funcoesService.obterFuncaoTransacaoPorAnaliseIdStatusFuncao(idAnalise);
    }

    public Set<FuncaoDados> obterFuncaoDadosPorAnaliseIdStatusFuncao(Long idAnalise) {
        return funcoesService.obterFuncaoDadosPorAnaliseIdStatusFuncao(idAnalise);
    }

    public Set<FuncaoTransacao> obterFuncaoTransacaoPorAnaliseIdStatusFuncao(Long idAnalise) {
        return funcoesService.obterFuncaoTransacaoPorAnaliseIdStatusFuncao(idAnalise);
    }

    public Integer obterAnaliseEquipe(Long idAnalise, List<Long> equipes) {
        return analiseRepository.analiseEquipe(idAnalise, equipes);
    }

    public void salvarAnalise(Analise analise) {
        analiseRepository.save(analise);
        analiseSearchRepository.save(converterParaEntidade(converterParaDto(analise)));
    }

    // Alterado: Método que salva apenas no banco, sem ElasticSearch (para uso em transações)
    public void salvarAnaliseApenasDB(Analise analise) {
        analiseRepository.save(analise);
    }

    // Alterado: Método que salva apenas no ElasticSearch
    public void salvarAnaliseApenasES(Analise analise) {
        analiseSearchRepository.save(converterParaEntidade(converterParaDto(analise)));
    }

    public void excluirAnalise(Analise analise) {
        analiseRepository.delete(analise);
        analiseSearchRepository.delete(analise.getId());
    }

    public void inserirHistoricoAnalise(Analise analise, User usuario, String acao) {
        consultasService.inserirHistoricoAnalise(analise, usuario, acao);
    }

    public void salvarFuncaoTransacao(FuncaoTransacao funcaoTransacao) {
        funcoesService.salvarFuncaoTransacao(funcaoTransacao);
    }

    public void salvarFuncaoDado(FuncaoDados funcaoDados) {
        funcoesService.salvarFuncaoDado(funcaoDados);
    }

    public Analise converterParaEntidade(AnaliseDTO analiseDTO) {
        return modelMapper.map(analiseDTO, Analise.class);
    }

    public AnaliseDTO converterParaDto(Analise analise) {
        AnaliseDTO analiseDTO = modelMapper.map(analise, AnaliseDTO.class);
        if (analise.getAnaliseDivergence() != null) {
            analiseDTO.setAnaliseDivergence(modelMapper.map(analise.getAnaliseDivergence(), AnaliseDivergenceDTO.class));
        }
        return analiseDTO;
    }

    public AnaliseEditDTO converterParaAnaliseEditDTO(Analise analise) {
        return modelMapper.map(analise, AnaliseEditDTO.class);
    }

    public Analise converterEditDtoParaEntidade(AnaliseEditDTO analise) {
        log.info("=== CONVERSÃO DTO → ENTIDADE ===");
        log.info("Total FD no DTO: {}", analise.getFuncaoDados() != null ? analise.getFuncaoDados().size() : 0);
        log.info("Total FT no DTO: {}", analise.getFuncaoTransacao() != null ? analise.getFuncaoTransacao().size() : 0);
        
        // Verifica se as funções do DTO têm Fator de Ajuste
        if (analise.getFuncaoDados() != null) {
            analise.getFuncaoDados().forEach(fd -> {
                if (fd.getFatorAjuste() != null) {
                    log.info("DTO FD '{}' tem FA: {} (ID: {})", fd.getName(), fd.getFatorAjuste().getNome(), fd.getFatorAjuste().getId());
                } else {
                    log.warn("DTO FD '{}' NÃO tem FA", fd.getName());
                }
            });
        }
        if (analise.getFuncaoTransacao() != null) {
            analise.getFuncaoTransacao().forEach(ft -> {
                if (ft.getFatorAjuste() != null) {
                    log.info("DTO FT '{}' tem FA: {} (ID: {})", ft.getName(), ft.getFatorAjuste().getNome(), ft.getFatorAjuste().getId());
                } else {
                    log.warn("DTO FT '{}' NÃO tem FA", ft.getName());
                }
            });
        }
        
        Analise resultado = modelMapper.map(analise, Analise.class);
        
        // Verifica se as funções da entidade convertida têm Fator de Ajuste
        log.info("=== APÓS CONVERSÃO ===");
        if (resultado.getFuncaoDados() != null) {
            resultado.getFuncaoDados().forEach(fd -> {
                if (fd.getFatorAjuste() != null) {
                    log.info("ENTIDADE FD '{}' tem FA: {}", fd.getName(), fd.getFatorAjuste().getNome());
                } else {
                    log.warn("ENTIDADE FD '{}' PERDEU FA!", fd.getName());
                }
            });
        }
        if (resultado.getFuncaoTransacao() != null) {
            resultado.getFuncaoTransacao().forEach(ft -> {
                if (ft.getFatorAjuste() != null) {
                    log.info("ENTIDADE FT '{}' tem FA: {}", ft.getName(), ft.getFatorAjuste().getNome());
                } else {
                    log.warn("ENTIDADE FT '{}' PERDEU FA!", ft.getName());
                }
            });
        }
        
        // Debug Modulo/Funcionalidade
        if (analise.getFuncaoDados() != null) {
            analise.getFuncaoDados().forEach(fd -> {
                String modName = (fd.getFuncionalidade() != null && fd.getFuncionalidade().getModulo() != null) ? fd.getFuncionalidade().getModulo().getNome() : "NULL";
                log.info("DTO FD '{}' - Func: {} - Modulo: {}", fd.getName(), fd.getFuncionalidade() != null ? fd.getFuncionalidade().getNome() : "NULL", modName);
            });
        }
        if (resultado.getFuncaoDados() != null) {
            resultado.getFuncaoDados().forEach(fd -> {
                String modName = (fd.getFuncionalidade() != null && fd.getFuncionalidade().getModulo() != null) ? fd.getFuncionalidade().getModulo().getNome() : "NULL";
                log.info("ENTIDADE FD '{}' - Func: {} - Modulo: {}", fd.getName(), fd.getFuncionalidade() != null ? fd.getFuncionalidade().getNome() : "NULL", modName);
                log.info("ENTIDADE FD '{}' - DERs: {}, RLRs: {}", fd.getName(), fd.getDers() != null ? fd.getDers().size() : 0, fd.getRlrs() != null ? fd.getRlrs().size() : 0);
            });
        }
        if (resultado.getFuncaoTransacao() != null) {
            resultado.getFuncaoTransacao().forEach(ft -> {
                String modName = (ft.getFuncionalidade() != null && ft.getFuncionalidade().getModulo() != null) ? ft.getFuncionalidade().getModulo().getNome() : "NULL";
                log.info("ENTIDADE FT '{}' - Func: {} - Modulo: {}", ft.getName(), ft.getFuncionalidade() != null ? ft.getFuncionalidade().getNome() : "NULL", modName);
                log.info("ENTIDADE FT '{}' - DERs: {}, ALRs: {}", ft.getName(), ft.getDers() != null ? ft.getDers().size() : 0, ft.getAlrs() != null ? ft.getAlrs().size() : 0);
            });
        }
        
        return resultado;
    }

    public AnaliseJsonDTO converterParaAnaliseJsonDTO(Analise analise) {
        return modelMapper.map(analise, AnaliseJsonDTO.class);
    }

    public AnaliseDivergenceEditDTO converterParaAnaliseDivergenciaEditDTO(Analise analise) {
        return modelMapper.map(analise, AnaliseDivergenceEditDTO.class);

    }

    public AnaliseDivergenceDTO converterParaAnaliseDivergenciaDTO(Analise analise) {
        return modelMapper.map(analise, AnaliseDivergenceDTO.class);
    }

    public Analise converterJsonParaEntidade(AnaliseJsonDTO analiseJsonDTO) {
        return modelMapper.map(analiseJsonDTO, Analise.class);
    }

    public Compartilhada converterCompartilhadaParaEntidade(CompartilhadaDTO compartilhadaDTO) {
        return modelMapper.map(compartilhadaDTO, Compartilhada.class);
    }

    public CompartilhadaDTO converterCompartilhadaParaDto(Compartilhada compartilhada) {
        return modelMapper.map(compartilhada, CompartilhadaDTO.class);
    }

}
