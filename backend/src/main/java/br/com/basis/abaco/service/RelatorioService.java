package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Organizacao;
import br.com.basis.abaco.domain.TipoEquipe;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoDeDataAnalise;
import br.com.basis.abaco.security.SecurityUtils;
import br.com.basis.abaco.service.dto.filter.AnaliseFilterDTO;
import br.com.basis.abaco.service.facades.AnaliseFacade;
import br.com.basis.abaco.utils.StringUtils;
import br.com.basis.dynamicexports.pojo.PropriedadesRelatorio;
import br.com.basis.dynamicexports.pojo.ReportObject;
import br.com.basis.dynamicexports.service.DynamicExportsService;
import lombok.AllArgsConstructor;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static br.com.basis.abaco.service.AnaliseService.COMPARTILHADAS_EQUIPE_ID;
import static br.com.basis.abaco.service.AnaliseService.EQUIPE_RESPONSAVEL_ID;
import static br.com.basis.abaco.service.AnaliseService.ORGANIZACAO_ID;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;

@Service
@AllArgsConstructor
public class RelatorioService {

    private final DynamicExportsService dynamicExportsService;
    private final UserService userService;

    public ByteArrayOutputStream exportar(PropriedadesRelatorio propriedadesRelatorio, Page<? extends ReportObject> page,
                                          String tipo, Optional<Map<String, String>> filtros, Optional<String> logoPath, Optional<String> footerMessage) throws JRException, DRException, ClassNotFoundException {
        return dynamicExportsService.export(propriedadesRelatorio, page, tipo, filtros, logoPath, footerMessage);
    }

    public SearchQuery obterQueryExportarRelatorio(AnaliseFilterDTO filtro) {
        Set<Long> sistema = new HashSet<>();
        Set<MetodoContagem> metodo = new HashSet<>();
        Set<Long> organizacao = new HashSet<>();
        Set<Long> usuario = new HashSet<>();
        Set<Long> status = new HashSet<>();
        preencheFiltro(sistema, metodo, organizacao, usuario, status, filtro);
        Pageable pageable = dynamicExportsService.obterPageableMaximoExportacao();
        BoolQueryBuilder qb = getBoolQueryBuilder(filtro.getIdentificadorAnalise(), sistema, metodo, organizacao, filtro.getEquipe() == null ? null : filtro.getEquipe().getId(), usuario, status, filtro.getData(), filtro.getDataInicio(), filtro.getDataFim());
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(qb).withPageable(pageable).build();
        return searchQuery;
    }

    public SearchQuery obterQueryExportarRelatorioDivergencia(AnaliseFilterDTO filtro, Pageable pageable) {
        Set<Long> sistema = new HashSet<>();
        Set<Long> organizacao = new HashSet<>();
        preencheFiltro(sistema, null, organizacao, null, null, filtro);
        BoolQueryBuilder qb = getBoolQueryBuilderDivergence(filtro.getIdentificadorAnalise(), sistema, organizacao);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(qb).withPageable(pageable).build();
        return searchQuery;
    }

    public BoolQueryBuilder getBoolQueryBuilderDivergence(String identificador, Set<Long> sistema, Set<Long> organizacao) {
        User user = userService.obterUsuarioPorLogin(SecurityUtils.getCurrentUserLogin()).get();
        Set<Long> organicoesIds = (organizacao != null && organizacao.size() > 0) ? organizacao : getIdOrganizacoes(user);
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        bindFilterSearchDivergence(identificador, sistema, organicoesIds, qb);
        return qb;
    }

    public void bindFilterSearchDivergence(String identificador, Set<Long> sistema, Set<Long> organizacoes, BoolQueryBuilder qb) {
        if (!StringUtils.isEmptyString((identificador))) {
            BoolQueryBuilder queryBuilderIdentificador = QueryBuilders.boolQuery().must(nestedQuery("analisesComparadas", boolQuery().must(QueryBuilders.matchPhraseQuery("analisesComparadas.identificadorAnalise", identificador))));
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().should(queryBuilderIdentificador).should(QueryBuilders.matchPhraseQuery("numeroOs", identificador)).should(QueryBuilders.matchPhraseQuery("identificadorAnalise", identificador));
            qb.must(boolQueryBuilder);
        }
        bindFilterEquipeAndOrganizacaoDivergence(organizacoes, qb);
        BoolQueryBuilder boolQueryBuilderDivergence;
        boolQueryBuilderDivergence = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("isDivergence", true));
        qb.must(boolQueryBuilderDivergence);
        if (sistema != null && sistema.size() > 0) {
            BoolQueryBuilder boolQueryBuilderSistema = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("sistema.id", sistema));
            qb.must(boolQueryBuilderSistema);
        }
    }

    private void bindFilterEquipeAndOrganizacaoDivergence(Set<Long> organizacoes, BoolQueryBuilder qb) {
        BoolQueryBuilder boolQueryBuilderEquipe = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery(ORGANIZACAO_ID, organizacoes));
        BoolQueryBuilder boolQueryBuilderCompartilhada = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery(ORGANIZACAO_ID, organizacoes));
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().should(boolQueryBuilderEquipe).should(boolQueryBuilderCompartilhada);
        qb.must(boolQueryBuilder);
    }

    public BoolQueryBuilder getBoolQueryBuilder(String identificador, Set<Long> sistema, Set<MetodoContagem> metodo, Set<Long> organizacao, Long equipe, Set<Long> usuario, Set<Long> idsStatus, TipoDeDataAnalise data, Date dataInicio, Date dataFim) {
        User user = userService.obterUsuarioPorLogin(SecurityUtils.getCurrentUserLogin()).get();
        Set<Long> equipesIds = getIdEquipes(user);
        Set<Long> organicoesIds = (organizacao != null && organizacao.size() > 0) ? organizacao : getIdOrganizacoes(user);
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        bindFilterSearch(identificador, sistema, metodo, usuario, equipe, equipesIds, organicoesIds, idsStatus, qb, data, dataInicio, dataFim);
        return qb;
    }

    public void bindFilterSearch(String identificador, Set<Long> sistema, Set<MetodoContagem> metodo, Set<Long> usuario, Long equipesIds, Set<Long> equipesUsersId, Set<Long> organizacoes, Set<Long> status, BoolQueryBuilder qb, TipoDeDataAnalise data, Date dataInicio, Date dataFim) {
        if (!StringUtils.isEmptyString((identificador))) {
            BoolQueryBuilder queryBuilderIdentificador = QueryBuilders.boolQuery().must(nestedQuery("analisesComparadas", boolQuery().must(QueryBuilders.matchPhrasePrefixQuery("analisesComparadas.identificadorAnalise", identificador))));
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().should(queryBuilderIdentificador).should(QueryBuilders.matchPhrasePrefixQuery("numeroOs", identificador)).should(QueryBuilders.matchPhrasePrefixQuery("identificadorAnalise", identificador));
            qb.must(boolQueryBuilder);
        }
        bindFilterEquipeAndOrganizacao(equipesIds, equipesUsersId, organizacoes, qb);
        BoolQueryBuilder boolQueryBuilderDivergence;
        boolQueryBuilderDivergence = QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery("isDivergence", true));
        qb.must(boolQueryBuilderDivergence);
        if (sistema != null && sistema.size() > 0) {
            BoolQueryBuilder boolQueryBuilderSistema = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("sistema.id", sistema));
            qb.must(boolQueryBuilderSistema);
        }
        if (metodo != null && metodo.size() > 0) {
            BoolQueryBuilder boolQueryBuilderSistema = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("metodoContagem", metodo));
            qb.must(boolQueryBuilderSistema);
        }
        if (status != null && status.size() > 0) {
            BoolQueryBuilder boolQueryBuilderStatus = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("status.id", status));
            qb.must(boolQueryBuilderStatus);
        }

        if (usuario != null && usuario.size() > 0) {
            BoolQueryBuilder queryBuilderUsers = QueryBuilders.boolQuery().must(nestedQuery("users", boolQuery().must(QueryBuilders.termsQuery("users.id", usuario))));
            qb.must(queryBuilderUsers);
        }
        bindFilterDataAnalise(qb, data, dataInicio, dataFim);
    }

    private void bindFilterEquipeAndOrganizacao(Long equipesIds, Set<Long> equipesUsersId, Set<Long> organizacoes, BoolQueryBuilder qb) {
        BoolQueryBuilder boolQueryBuilderEquipe;
        BoolQueryBuilder boolQueryBuilderCompartilhada;
        if (equipesIds != null && equipesIds > 0) {
            if (equipesUsersId.contains(equipesIds)) {
                boolQueryBuilderEquipe = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(EQUIPE_RESPONSAVEL_ID, equipesIds)).must(QueryBuilders.termsQuery(ORGANIZACAO_ID, organizacoes));
                boolQueryBuilderCompartilhada = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(EQUIPE_RESPONSAVEL_ID, equipesIds)).must(QueryBuilders.termsQuery(COMPARTILHADAS_EQUIPE_ID, equipesUsersId)).must(QueryBuilders.termsQuery(ORGANIZACAO_ID, organizacoes));

                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().should(boolQueryBuilderEquipe).should(boolQueryBuilderCompartilhada);
                qb.must(boolQueryBuilder);
            } else {
                boolQueryBuilderCompartilhada = QueryBuilders.boolQuery().must(QueryBuilders.termQuery(EQUIPE_RESPONSAVEL_ID, equipesIds)).must(QueryBuilders.termsQuery(COMPARTILHADAS_EQUIPE_ID, equipesUsersId)).must(QueryBuilders.termsQuery(ORGANIZACAO_ID, organizacoes));

                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().should(boolQueryBuilderCompartilhada);
                qb.must(boolQueryBuilder);
            }
        } else {
            boolQueryBuilderEquipe = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery(EQUIPE_RESPONSAVEL_ID, equipesUsersId)).must(QueryBuilders.termsQuery(ORGANIZACAO_ID, organizacoes));

            boolQueryBuilderCompartilhada = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery(COMPARTILHADAS_EQUIPE_ID, equipesUsersId)).must(QueryBuilders.termsQuery(ORGANIZACAO_ID, organizacoes));
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().should(boolQueryBuilderEquipe).should(boolQueryBuilderCompartilhada);
            qb.must(boolQueryBuilder);
        }
    }

    private void bindFilterDataAnalise(BoolQueryBuilder qb, TipoDeDataAnalise data, Date dataInicio, Date dataFim) {
        if (data != null && (dataInicio != null || dataFim != null)) {
            Timestamp start = Timestamp.from(Instant.ofEpochMilli(1L));
            Timestamp end = Timestamp.from(Instant.now());
            if (dataInicio != null) {
                start = new Timestamp(dataInicio.getTime());
            }
            if (dataFim != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(dataFim.getTime());
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                end = new Timestamp(cal.getTimeInMillis());
            }
            BoolQueryBuilder boolQueryBuilderData = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(getNomeData(data)).gte(start).lte(end));
            qb.must(boolQueryBuilderData);
        }
    }

    private String getNomeData(TipoDeDataAnalise data) {
        switch (data) {
            case CRIACAO:
                return "dataCriacaoOrdemServico";
            case BLOQUEIO:
                return "dataHomologacao";
            case ENCERRAMENTO:
                return "dtEncerramento";
        }
        return "";
    }

    private Set<Long> getIdEquipes(User user) {
        Set<TipoEquipe> listaEquipes = user.getTipoEquipes();
        Set<Long> equipesIds = new HashSet<>();
        listaEquipes.forEach(tipoEquipe -> {
            equipesIds.add(tipoEquipe.getId());
        });
        return equipesIds;
    }

    private Set<Long> getIdOrganizacoes(User user) {
        Set<Organizacao> organizacaos = user.getOrganizacoes();
        Set<Long> organizacoesIds = new HashSet<>();
        organizacaos.forEach(organizacao -> {
            organizacoesIds.add(organizacao.getId());
        });
        return organizacoesIds;
    }

    private void preencheFiltro(Set<Long> sistema, Set<MetodoContagem> metodo, Set<Long> organizacao, Set<Long> usuario, Set<Long> status, AnaliseFilterDTO filtro) {
        if (filtro.getSistema() != null) {
            sistema.add(filtro.getSistema().getId());
        }
        if (filtro.getMetodoContagem() != null) {
            metodo.add(filtro.getMetodoContagem());
        }
        if (filtro.getOrganizacao() != null) {
            organizacao.add(filtro.getOrganizacao().getId());
        }
        if (filtro.getUsuario() != null) {
            usuario.add(filtro.getUsuario().getId());
        }
        if (filtro.getStatus() != null) {
            status.add(filtro.getStatus().getId());
        }
    }

}
