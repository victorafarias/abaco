package br.com.basis.abaco.service.facades;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Compartilhada;
import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.domain.VwAnaliseDivergenteSomaPf;
import br.com.basis.abaco.domain.VwAnaliseSomaPf;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoDeDataAnalise;
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
import br.com.basis.dynamicexports.pojo.PropriedadesRelatorio;
import br.com.basis.dynamicexports.pojo.ReportObject;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AnaliseFacade {
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

    public BoolQueryBuilder obterBoolQueryBuilder(String identificador, Set<Long> sistema, Set<MetodoContagem> metodo, Set<Long> organizacao, Long equipe, Set<Long> usuario, Set<Long> idsStatus, TipoDeDataAnalise data, Date dataInicio, Date dataFim) {
        return relatorioService.getBoolQueryBuilder(identificador, sistema, metodo, organizacao, equipe, usuario, idsStatus, data, dataInicio, dataFim);
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
        return modelMapper.map(analise, AnaliseDTO.class);
    }

    public AnaliseEditDTO converterParaAnaliseEditDTO(Analise analise) {
        return modelMapper.map(analise, AnaliseEditDTO.class);
    }

    public Analise converterEditDtoParaEntidade(AnaliseEditDTO analise) {
        return modelMapper.map(analise, Analise.class);
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
