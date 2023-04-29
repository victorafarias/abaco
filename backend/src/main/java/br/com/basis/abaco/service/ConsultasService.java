package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.domain.VwAnaliseDivergenteSomaPf;
import br.com.basis.abaco.domain.VwAnaliseSomaPf;
import br.com.basis.abaco.repository.StatusRepository;
import br.com.basis.abaco.repository.VwAnaliseDivergenteSomaPfRepository;
import br.com.basis.abaco.repository.VwAnaliseSomaPfRepository;
import br.com.basis.abaco.security.SecurityUtils;
import br.com.basis.dynamicexports.service.DynamicExportsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsultasService {

    private final DynamicExportsService dynamicExportsService;
    public final StatusRepository statusRepository;
    private final VwAnaliseDivergenteSomaPfRepository vwAnaliseDivergenteSomaPfRepository;
    private final VwAnaliseSomaPfRepository vwAnaliseSomaPfRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;
    public final UserService userService;
    private final HistoricoService historicoService;

    public ConsultasService(ElasticsearchTemplate elasticsearchTemplate,
                            DynamicExportsService dynamicExportsService,
                            StatusRepository statusRepository,
                            VwAnaliseDivergenteSomaPfRepository vwAnaliseDivergenteSomaPfRepository,
                            VwAnaliseSomaPfRepository vwAnaliseSomaPfRepository,
                            UserService userService,
                            HistoricoService historicoService) {
        this.dynamicExportsService = dynamicExportsService;
        this.statusRepository = statusRepository;
        this.vwAnaliseDivergenteSomaPfRepository = vwAnaliseDivergenteSomaPfRepository;
        this.vwAnaliseSomaPfRepository = vwAnaliseSomaPfRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.userService = userService;
        this.historicoService = historicoService;
    }

    public Pageable obterPaginacaoMaximaExportacao() {
        return dynamicExportsService.obterPageableMaximoExportacao();
    }

    public Page<Analise> obterPaginaAnalise(SearchQuery query) {
        return elasticsearchTemplate.queryForPage(query, Analise.class);
    }

    public Status obterStatusPorId(Long idStatus) {
        return statusRepository.findById(idStatus);
    }

    public Status obterPrimeiroStatusPorDivergencia() {
        return statusRepository.findFirstByDivergenciaTrue();
    }

    public Optional<Status> obterStatusPorNome(String nomeStatus) {
        return statusRepository.findByNomeContainsIgnoreCase(nomeStatus);
    }

    public User obterUsuarioPorLogin() {
        return userService.obterUsuarioPorLogin(SecurityUtils.getCurrentUserLogin()).orElse(new User());
    }

    public User obterUsuarioComAutorizacao() {
        return userService.obterUsuarioComAutorizacao(SecurityUtils.getCurrentUserLogin()).orElse(new User());
    }

    public VwAnaliseSomaPf obterAnaliseSomaPfPorId(Long idAnalise) {
        return vwAnaliseSomaPfRepository.findByAnaliseId(idAnalise);
    }

    public VwAnaliseDivergenteSomaPf obterAnaliseDivergenteSomaPfPorId(Long idAnalise) {
        return vwAnaliseDivergenteSomaPfRepository.findByAnaliseId(idAnalise);
    }

    public void inserirHistoricoAnalise(Analise analise, User usuario, String acao) {
        historicoService.inserirHistoricoAnalise(analise, usuario, acao);
    }

}
