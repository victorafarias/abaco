package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Alr;
import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Compartilhada;
import br.com.basis.abaco.domain.Der;
import br.com.basis.abaco.domain.EsforcoFase;
import br.com.basis.abaco.domain.FatorAjuste;
import br.com.basis.abaco.domain.FuncaoAnalise;
import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Funcionalidade;
import br.com.basis.abaco.domain.Modulo;
import br.com.basis.abaco.domain.Rlr;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.domain.TipoEquipe;
import br.com.basis.abaco.domain.UploadedFile;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.domain.VwAnaliseDivergenteSomaPf;
import br.com.basis.abaco.domain.VwAnaliseSomaPf;
import br.com.basis.abaco.domain.enumeration.Complexidade;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;
import br.com.basis.abaco.domain.enumeration.TipoDeDataAnalise;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoDados;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoTransacao;
import br.com.basis.abaco.repository.CompartilhadaRepository;
import br.com.basis.abaco.repository.OrganizacaoRepository;
import br.com.basis.abaco.repository.SistemaRepository;
import br.com.basis.abaco.repository.TipoEquipeRepository;
import br.com.basis.abaco.repository.UploadedFilesRepository;
import br.com.basis.abaco.service.dto.AnaliseDTO;
import br.com.basis.abaco.service.dto.AnaliseDivergenceDTO;
import br.com.basis.abaco.service.dto.AnaliseDivergenceEditDTO;
import br.com.basis.abaco.service.dto.AnaliseEditDTO;
import br.com.basis.abaco.service.dto.AnaliseEncerramentoDTO;
import br.com.basis.abaco.service.dto.AnaliseJsonDTO;
import br.com.basis.abaco.service.dto.CompartilhadaDTO;
import br.com.basis.abaco.service.dto.filter.AnaliseFilterDTO;
import br.com.basis.abaco.service.dto.formularios.AnaliseFormulario;
import br.com.basis.abaco.service.dto.novo.AbacoMensagens;
import br.com.basis.abaco.service.exception.RelatorioException;
import br.com.basis.abaco.service.facades.AnaliseFacade;
import br.com.basis.abaco.service.relatorio.RelatorioAnaliseColunas;
import br.com.basis.abaco.service.relatorio.RelatorioDivergenciaColunas;
import br.com.basis.abaco.service.validadores.AnaliseValidador;
import br.com.basis.abaco.utils.AbacoUtil;
import br.com.basis.abaco.utils.PageUtils;
import br.com.basis.abaco.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;


@Service
@RequiredArgsConstructor
public class AnaliseService extends BaseService {

    private static final String ESTIMATIVA = "AFP - Estimativa";
    private static final String DETALHADA = "AFP - Detalhada";
    private static final String RESUMO = "Resumo";
    private static final String SHEET_INM = "AFP - INM";
    private static final String SHEET_INM_INDICATIVA = "AFP - Indicativa";

    // Tipo Metodo de Contagem
    private static final String METODO_DETALHADO = "Detalhada";
    private static final String METODO_ESTIMATIVA = "Estimativa";
    private static final String METODO_INDICATIVA = "Indicativa";

    // Tipo Função Dados
    private static final String METODO_ALI = "ALI";
    private static final String METODO_AIE = "AIE";
    private static final String METODO_INM = "INM";

    // Tipo Função Transação
    private static final String METODO_EE = "EE";
    private static final String METODO_SE = "SE";
    private static final String METODO_CE = "CE";

    // Complexidade
    private static final String METODO_SEM = "Sem";
    private static final String METODO_BAIXO = "Simples";
    private static final String METODO_MEDIO = "Médio";
    private static final String METODO_ALTA = "Complexo";

    // Função Status
    private static final String DIVERGENTE = "Divergente";
    private static final String EXCLUIDO = "Excluido";
    private static final String VALIDADO = "Validado";
    private static final String PENDENTE = "Pendente";

    private static final String GEROU_VALIDACAO = "Gerou a validação ";

    private final static String GEROU_VALIDACAO = "Gerou a validação ";

    public static final String ORGANIZACAO_ID = "organizacao.id";
    public static final String EQUIPE_RESPONSAVEL_ID = "equipeResponsavel.id";
    public static final String COMPARTILHADAS_EQUIPE_ID = "compartilhadas.equipeId";
    private static final String EMPTY_STRING = "";
    private static final String BASIS_MINUSCULO = "basis";
    private static final String BASIS = "basis";
    private static final int DECIMAL_PLACE = 2;
    private final BigDecimal percent = new BigDecimal("100");

    private Set<Der> ders = new HashSet<>();
    private Set<Rlr> rlrs = new HashSet<>();
    private Set<Alr> alrs = new HashSet<>();

    private final Logger log = LoggerFactory.getLogger(AnaliseService.class);
    private final SistemaRepository sistemaRepository;
    private final CompartilhadaRepository compartilhadaRepository;
    private final TipoEquipeRepository tipoEquipeRepository;
    private final UploadedFilesRepository uploadedFilesRepository;
    private final MailService mailService;
    private final PerfilService perfilService;
    private final AnaliseFacade analiseFacade;
    private final OrganizacaoRepository organizacaoRepository;

    private Boolean checarPermissao(Long idAnalise) {
        User logged = analiseFacade.obterUsuarioComAutorizacao();
        List<Long> equipesIds = logged.getTipoEquipes().stream().map(TipoEquipe::getId).collect(Collectors.toList());
        Integer analiseDaEquipe = analiseFacade.obterAnaliseEquipe(idAnalise, equipesIds);
        if (analiseDaEquipe == 0) {
            return verificaCompartilhada(idAnalise);
        } else {
            return true;
        }
    }

    private Boolean verificaCompartilhada(Long idAnalise) {
        return compartilhadaRepository.existsByAnaliseId(idAnalise);
    }

    public Analise recuperarAnalise(Long idAnalise) {
        boolean retorno = checarPermissao(idAnalise);
        if (retorno) {
            return analiseFacade.obterAnalisePorId(idAnalise);
        } else {
            return null;
        }
    }

    public Analise recuperarAnaliseDivergence(Long idAnalise) {
        return analiseFacade.obterAnalisePorId(idAnalise);
    }

    public Set<FuncaoDados> bindCloneFuncaoDados(Analise analise, Analise analiseClone) {
        Set<FuncaoDados> funcaoDados = new HashSet<>();
        analise.getFuncaoDados().forEach(fd -> {
            FuncaoDados funcaoDado = bindFuncaoDados(analiseClone, fd);
            funcaoDados.add(funcaoDado);
        });
        return funcaoDados;
    }

    private FuncaoDados bindFuncaoDados(Analise analiseClone, FuncaoDados fd) {
        Set<Rlr> rlrSet = new LinkedHashSet<>();
        Set<Der> derSet = new LinkedHashSet<>();
        FuncaoDados funcaoDado = new FuncaoDados();
        bindFuncaoDados(analiseClone, fd, rlrSet, derSet, funcaoDado);
        return funcaoDado;
    }

    public Set<FuncaoDados> bindDivergenceFuncaoDados(Analise analise, Analise analiseClone) {
        Set<FuncaoDados> funcaoDados = new LinkedHashSet<>();
        analise.getFuncaoDados().forEach(fd -> {
            FuncaoDados funcaoDado = bindFuncaoDados(analiseClone, fd);
            funcaoDado.setStatusFuncao(StatusFuncao.PENDENTE);
            funcaoDados.add(funcaoDado);
        });
        return funcaoDados;
    }

    public Set<FuncaoTransacao> bindCloneFuncaoTransacaos(Analise analise, Analise analiseClone) {
        Set<FuncaoTransacao> funcaoTransacoes = new HashSet<>();
        analise.getFuncaoTransacaos().forEach(ft -> {
            FuncaoTransacao funcaoTransacao = bindFuncaoTransacao(analiseClone, ft);
            funcaoTransacoes.add(funcaoTransacao);
        });
        return funcaoTransacoes;
    }

    public Set<FuncaoTransacao> bindDivergenceFuncaoTransacaos(Analise analise, Analise analiseClone) {
        Set<FuncaoTransacao> funcaoTransacoes = new LinkedHashSet<>();
        analise.getFuncaoTransacaos().forEach(ft -> {
            FuncaoTransacao funcaoTransacao = bindFuncaoTransacao(analiseClone, ft);
            funcaoTransacao.setStatusFuncao(StatusFuncao.PENDENTE);
            funcaoTransacoes.add(funcaoTransacao);
        });
        return funcaoTransacoes;
    }

    private FuncaoTransacao bindFuncaoTransacao(Analise analiseClone, FuncaoTransacao ft) {
        Set<Alr> alrSet = new LinkedHashSet<>();
        Set<Der> derSet = new LinkedHashSet<>();
        FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
        ft.getAlrs().forEach(alr -> {
            Alr alrClone = new Alr(null, alr.getNome(), alr.getValor(), funcaoTransacao, null);
            alrSet.add(alrClone);
        });
        ft.getDers().forEach(der -> {
            Der derClone = new Der(null, der.getNome(), der.getValor(), der.getRlr(), null, funcaoTransacao);
            derSet.add(derClone);
        });
        funcaoTransacao.bindFuncaoTransacao(ft.getTipo(), ft.getFtrStr(), ft.getQuantidade(), alrSet, null, ft.getFtrValues(), ft.getImpacto(), derSet, analiseClone, ft.getComplexidade(), ft.getPf(), ft.getGrossPF(), ft.getFuncionalidade(), ft.getDetStr(), ft.getFatorAjuste(), ft.getName(), ft.getSustantation(), ft.getDerValues(), ft.getEquipe(), ft.getOrdem());
        funcaoTransacao.setFuncionalidade(ft.getFuncionalidade());
        return funcaoTransacao;
    }

    private void bindFuncaoDados(Analise analiseClone, FuncaoDados fd, Set<Rlr> rlrs, Set<Der> ders, FuncaoDados funcaoDado) {
        Optional.ofNullable(fd.getDers()).orElse(Collections.emptySet()).forEach(der -> {
            Rlr rlr = null;
            if (der.getRlr() != null) {
                rlr = new Rlr(null, der.getRlr().getNome(), der.getRlr().getValor(), der.getRlr().getDers(), funcaoDado);
            }
            Der derClone = new Der(null, der.getNome(), der.getValor(), rlr, funcaoDado, null);
            ders.add(derClone);
        });
        Optional.ofNullable(fd.getRlrs()).orElse(Collections.emptySet()).forEach(rlr -> {
            Rlr rlrClone = new Rlr(null, rlr.getNome(), rlr.getValor(), ders, funcaoDado);
            rlrs.add(rlrClone);
        });
        funcaoDado.bindFuncaoDados(fd.getComplexidade(), fd.getPf(), fd.getGrossPF(), analiseClone, fd.getFuncionalidade(), fd.getDetStr(), fd.getFatorAjuste(), fd.getName(), fd.getSustantation(), fd.getDerValues(), fd.getTipo(), fd.getRetStr(), fd.getQuantidade(), rlrs, fd.getAlr(), fd.getFiles(), fd.getRlrValues(), ders, fd.getFuncaoDadosVersionavel(), fd.getImpacto(), fd.getEquipe(), fd.getOrdem());

    }

    public AnaliseDTO converterParaDto(Analise analise) {
        AnaliseDTO analiseDto = analiseFacade.converterParaDto(analise);
        if (analise.getAnaliseDivergence() != null) {
            analiseDto.setAnaliseDivergence(converterParaAnaliseDivergenciaDTO(analise.getAnaliseDivergence()));
        }
        return analiseDto;
    }

    public void anexarAnalise(@Valid @RequestBody Analise analiseUpdate, Analise analise) {
        analise.setNumeroOs(analiseUpdate.getNumeroOs());
        analise.setEquipeResponsavel(analiseUpdate.getEquipeResponsavel());
        analise.setIdentificadorAnalise(analiseUpdate.getIdentificadorAnalise());
        analise.setDataCriacaoOrdemServico(analiseUpdate.getDataCriacaoOrdemServico());
        analise.setMetodoContagem(analiseUpdate.getMetodoContagem());
        analise.setUsers(analiseUpdate.getUsers());
        analise.setPropositoContagem(analiseUpdate.getPropositoContagem());
        analise.setEscopo(analiseUpdate.getEscopo());
        analise.setFronteiras(analiseUpdate.getFronteiras());
        analise.setDocumentacao(analiseUpdate.getDocumentacao());
        analise.setBaselineImediatamente(analiseUpdate.getBaselineImediatamente());
        analise.setDataHomologacao(analiseUpdate.getDataHomologacao());
        analise.setEnviarBaseline(analiseUpdate.isEnviarBaseline());
        analise.setObservacoes(analiseUpdate.getObservacoes());
        analise.setEsforcoFases(analiseUpdate.getEsforcoFases());
        analise.setStatus(analiseUpdate.getStatus());
        analise.setFatorCriticidade(analiseUpdate.getFatorCriticidade());
        analise.setValorCriticidade(analiseUpdate.getValorCriticidade());
        analise.setScopeCreep(analiseUpdate.getScopeCreep());
        analise.setMotivo(analiseUpdate.getMotivo());
    }

    public boolean permissaoParaEditar(User user, Analise analise) {
        boolean podeEditar = false;
        if (!user.getOrganizacoes().contains(analise.getOrganizacao())) {
            return false;
        }
        if (user.getTipoEquipes().contains(analise.getEquipeResponsavel())) {
            return true;
        }
        for (TipoEquipe equipe : user.getTipoEquipes()) {
            for (Compartilhada compartilhada : analise.getCompartilhadas()) {
                if (equipe.getId().equals(compartilhada.getEquipeId()) && !compartilhada.isViewOnly()) {
                    return true;
                }
            }
        }
        return podeEditar;
    }

    public void atualizarPF(Analise analise) {
        VwAnaliseSomaPf vwAnaliseSomaPf = analiseFacade.obterAnaliseSomaPfPorId(analise.getId());
        BigDecimal sumFase = new BigDecimal(BigInteger.ZERO).setScale(DECIMAL_PLACE);
        if (analise.getEsforcoFases() != null && (!analise.getEsforcoFases().isEmpty())) {
            for (EsforcoFase esforcoFase : analise.getEsforcoFases()) {
                sumFase = sumFase.add(esforcoFase.getEsforco().setScale(DECIMAL_PLACE));
            }

        }
        sumFase = sumFase.divide(percent).setScale(DECIMAL_PLACE);
        analise.setPfTotal(vwAnaliseSomaPf.getPfGross().setScale(DECIMAL_PLACE));
        analise.setAdjustPFTotal(vwAnaliseSomaPf.getPfTotal().multiply(sumFase).setScale(DECIMAL_PLACE, RoundingMode.HALF_DOWN));

        analise.setPfTotalValor(vwAnaliseSomaPf.getPfGross().setScale(DECIMAL_PLACE).doubleValue());
        analise.setPfTotalAjustadoValor(vwAnaliseSomaPf.getPfTotal().multiply(sumFase).setScale(DECIMAL_PLACE, RoundingMode.HALF_DOWN).doubleValue());
    }

    public void atualizarPFDivergente(Analise analise) {
        VwAnaliseDivergenteSomaPf vwAnaliseDivergenteSomaPf = analiseFacade.obterAnaliseDivergenteSomaPfPorId(analise.getId());
        BigDecimal sumFase = new BigDecimal(BigInteger.ZERO).setScale(DECIMAL_PLACE);
        if (analise.getEsforcoFases() != null && (!analise.getEsforcoFases().isEmpty())) {
            for (EsforcoFase esforcoFase : analise.getEsforcoFases()) {
                sumFase = sumFase.add(esforcoFase.getEsforco().setScale(DECIMAL_PLACE));
            }

        }
        if (vwAnaliseDivergenteSomaPf != null) {
            sumFase = sumFase.divide(percent).setScale(DECIMAL_PLACE);
            analise.setPfTotal(vwAnaliseDivergenteSomaPf.getPfGross().setScale(DECIMAL_PLACE));
            analise.setAdjustPFTotal(vwAnaliseDivergenteSomaPf.getPfTotal().multiply(sumFase).setScale(DECIMAL_PLACE, RoundingMode.HALF_DOWN));
            analise.setPfTotalAprovado(analise.getAdjustPFTotal());
            Timestamp hoje = Timestamp.from(Instant.now());
            Analise analiseOriginalBasis = new Analise();
            if (!analise.getCompartilhadas().isEmpty()) {
                for (Analise analiseComparada : analise.getAnalisesComparadas()) {
                    if (analiseComparada.getEquipeResponsavel().getNome().toLowerCase().contains(BASIS) && (analiseComparada.getDataCriacaoOrdemServico().before(hoje))) {
                        hoje = analiseComparada.getDataCriacaoOrdemServico();
                        analiseOriginalBasis = analiseComparada;

                    }
                }
            }
            analise.setPfTotalOriginal(analiseOriginalBasis.getAdjustPFTotal());
            analise.setPfTotalValor(vwAnaliseDivergenteSomaPf.getPfGross().setScale(DECIMAL_PLACE).doubleValue());
            analise.setPfTotalAjustadoValor(vwAnaliseDivergenteSomaPf.getPfTotal().multiply(sumFase).setScale(DECIMAL_PLACE, RoundingMode.HALF_DOWN).doubleValue());
        }
    }

    public void setarAnaliseClone(Analise analiseClone, Analise analise, User user, Long idEquipe) {
        if (idEquipe == null) {
            Set<User> lstUsers = new LinkedHashSet<>();
            lstUsers.add(user);
            analiseClone.setIdentificadorAnalise(analise.getIdentificadorAnalise() + " - CÓPIA");
            analiseClone.setUsers(lstUsers);
            analiseClone.setDocumentacao(EMPTY_STRING);
            analiseClone.setFronteiras(EMPTY_STRING);
            analiseClone.setPropositoContagem(EMPTY_STRING);
            analise.setClonadaParaEquipe(false);
            analiseClone.setEscopo(EMPTY_STRING);
            analiseClone.setFuncaoDados(bindCloneFuncaoDados(analise, analiseClone));
            analiseClone.setFuncaoTransacaos(bindCloneFuncaoTransacaos(analise, analiseClone));
            analiseClone.setEsforcoFases(bindCloneEsforcoFase(analise));
        } else {
            TipoEquipe equipe = tipoEquipeRepository.findById(idEquipe);
            analiseClone.setIdentificadorAnalise(analise.getIdentificadorAnalise() + " - CÓPIADA PARA EQUIPE");
            analiseClone.setPfTotal(BigDecimal.ZERO);
            analiseClone.setAdjustPFTotal(BigDecimal.ZERO);
            analiseClone.setEquipeResponsavel(equipe);
            analiseClone.setUsers(new HashSet<>());
            analiseClone.setClonadaParaEquipe(true);
            analiseClone.setAnaliseClonadaParaEquipe(analise);
            analiseClone.setAnaliseClonou(false);
        }
        analiseClone.setBloqueiaAnalise(false);
        analiseClone.setDataHomologacao(null);
        analiseClone.setDtEncerramento(null);
        analiseClone.setIsEncerrada(false);
        analiseClone.setDataCriacaoOrdemServico(Timestamp.from(Instant.now()));
    }

    public Analise bindDivergenceAnalise(Analise analiseClone, Analise analise, User user) {
        Set<User> lstUsers = new LinkedHashSet<>();
        lstUsers.add(user);
        analiseClone.setUsers(lstUsers);
        analiseClone.setDocumentacao(EMPTY_STRING);
        analiseClone.setFronteiras(EMPTY_STRING);
        analiseClone.setPropositoContagem(EMPTY_STRING);
        analiseClone.setEscopo(EMPTY_STRING);
        analise.getFuncaoDados().forEach(funcao -> funcao.setEquipe(analise.getEquipeResponsavel()));
        analise.getFuncaoTransacaos().forEach(funcao -> funcao.setEquipe(analise.getEquipeResponsavel()));
        analiseClone.setFuncaoDados(bindDivergenceFuncaoDados(analise, analiseClone));
        analiseClone.setFuncaoTransacaos(bindDivergenceFuncaoTransacaos(analise, analiseClone));
        analiseClone.setEsforcoFases(bindCloneEsforcoFase(analise));
        analiseClone.setBloqueiaAnalise(false);
        analiseClone.setAdjustPFTotal(analise.getAdjustPFTotal());
        return analiseClone;
    }

    public Set<EsforcoFase> bindCloneEsforcoFase(Analise analise) {
        Set<EsforcoFase> esforcoFases = new HashSet<>();
        analise.getEsforcoFases().forEach(esforcoFase -> {
            esforcoFase = new EsforcoFase(esforcoFase.getId(), esforcoFase.getEsforco(), esforcoFase.getManual(), esforcoFase.getFase());
            esforcoFases.add(esforcoFase);
        });
        return esforcoFases;
    }

    public boolean changeStatusAnalise(Analise analise, Status status, User user) {

        if (user.getTipoEquipes().contains(analise.getEquipeResponsavel()) && user.getOrganizacoes().contains(analise.getOrganizacao())) {
            analise.setStatus(status);
            return true;
        } else {
            return false;
        }
    }

    public Analise generateDivergence1(Analise analise, Status status) {
        if (analiseFacade.obterUsuarioPorLogin() != null) {
            User user = analiseFacade.obterUsuarioPorLogin();
            Analise analiseDivergencia = new Analise(analise, user);
            analiseDivergencia = bindDivergenceAnalise(analiseDivergencia, analise, user);
            analiseDivergencia.setDataCriacaoOrdemServico(Timestamp.from(Instant.now()));
            analiseDivergencia.setStatus(status);
            analiseDivergencia.setIsDivergence(true);
            analiseDivergencia = salvar(analiseDivergencia);
            updateAnaliseRelationAndSendEmail(analise, status, analiseDivergencia);

            analiseFacade.inserirHistoricoAnalise(analise, user, GEROU_VALIDACAO + analiseDivergencia.getId());
            return analiseDivergencia;
        }
        return new Analise();
    }

    public Analise generateDivergence2(Analise analisePricinpal, Analise analiseSecundaria, Status status,
                                       boolean isUnionFunction) {
        Analise analiseDivergencia = bindAnaliseDivegernce(analisePricinpal, analiseSecundaria, status, isUnionFunction);

        salvar(analiseDivergencia);
        updateAnaliseRelationAndSendEmail(analisePricinpal, status, analiseDivergencia);
        updateAnaliseRelationAndSendEmail(analiseSecundaria, status, analiseDivergencia);
        sharedAnaliseDivergence(analiseSecundaria, analiseDivergencia);
        return analiseDivergencia;
    }

    private void sharedAnaliseDivergence(Analise analiseSecundaria, Analise analiseDivergencia) {
        Compartilhada compartilhada = new Compartilhada();
        compartilhada.setAnaliseId(analiseDivergencia.getId());
        compartilhada.setEquipeId(analiseSecundaria.getEquipeResponsavel().getId());
        compartilhada.setNomeEquipe(analiseSecundaria.getEquipeResponsavel().getNome());
        compartilhada.setViewOnly(true);
        compartilhadaRepository.save(compartilhada);
    }

    private void updateAnaliseRelationAndSendEmail(Analise analisePricinpal, Status status, Analise
        analiseDivergencia) {
        analisePricinpal.setStatus(status);
        analisePricinpal.setAnaliseDivergence(analiseDivergencia);
        salvar(analisePricinpal);
        if (analisePricinpal.getEquipeResponsavel().getNome() != null && analisePricinpal.getEquipeResponsavel().getEmailPreposto() != null) {
            mailService.sendDivergenceEmail(analisePricinpal);
        }
    }

    private Analise bindAnaliseDivegernce(Analise analisePrincipal, Analise analiseSecundaria, Status status,
                                          boolean isUnionFunction) {
        if (verificaUsuarioNulo()) {
            User user = analiseFacade.obterUsuarioPorLogin();
            Analise analiseDivergenciaPrincipal = new Analise(analisePrincipal, user);
            analiseDivergenciaPrincipal = bindDivergenceAnalise(analiseDivergenciaPrincipal, analisePrincipal, user);
            if (isUnionFunction) {
                unionFuncaoDadosAndFuncaoTransacao(analisePrincipal, analiseSecundaria, analiseDivergenciaPrincipal);
            }
            analiseDivergenciaPrincipal.setStatus(status);
            analiseDivergenciaPrincipal.setIsDivergence(true);
            analiseDivergenciaPrincipal.setDataCriacaoOrdemServico(Timestamp.from(Instant.now()));
            if (analisePrincipal.getEquipeResponsavel().getNome().toLowerCase().contains(BASIS) && analisePrincipal.getDataCriacaoOrdemServico().before(analiseSecundaria.getDataCriacaoOrdemServico())) {
                analiseDivergenciaPrincipal.setAdjustPFTotal(analisePrincipal.getAdjustPFTotal());
            } else if (analiseSecundaria.getEquipeResponsavel().getNome().toLowerCase().contains(BASIS) && analiseSecundaria.getDataCriacaoOrdemServico().before(analisePrincipal.getDataCriacaoOrdemServico())) {
                analiseDivergenciaPrincipal.setAdjustPFTotal(analiseSecundaria.getAdjustPFTotal());
            } else {
                analiseDivergenciaPrincipal.setAdjustPFTotal(analisePrincipal.getAdjustPFTotal());
            }
            return analiseDivergenciaPrincipal;
        }
        return new Analise();
    }

    private boolean verificaUsuarioNulo() {
        return analiseFacade.obterUsuarioPorLogin() != null && analiseFacade.obterUsuarioPorLogin().getId() != null;
    }


    private void unionFuncaoDadosAndFuncaoTransacao(Analise analisePrincipal, Analise analiseSecundaria, Analise
        analiseDivergenciaPrincipal) {
        Set<FuncaoDados> lstFuncaoDados = new LinkedHashSet<>();
        Set<FuncaoTransacao> lstFuncaoTransacaos = new LinkedHashSet<>();
        Set<FuncaoDados> lstOrganizadaFuncaoDados = new LinkedHashSet<>();
        Set<FuncaoTransacao> lstOrganizadaFuncaoTransacao = new LinkedHashSet<>();

        analisePrincipal.getFuncaoDados().forEach(funcao -> funcao.setEquipe(analisePrincipal.getEquipeResponsavel()));
        analisePrincipal.getFuncaoTransacaos().forEach(funcao -> funcao.setEquipe(analisePrincipal.getEquipeResponsavel()));
        analiseSecundaria.getFuncaoDados().forEach(funcao -> funcao.setEquipe(analiseSecundaria.getEquipeResponsavel()));
        analiseSecundaria.getFuncaoTransacaos().forEach(funcao -> funcao.setEquipe(analiseSecundaria.getEquipeResponsavel()));

        lstFuncaoDados.addAll(analisePrincipal.getFuncaoDados());
        lstFuncaoDados.addAll(analiseSecundaria.getFuncaoDados());
        lstFuncaoTransacaos.addAll(analisePrincipal.getFuncaoTransacaos());
        lstFuncaoTransacaos.addAll(analiseSecundaria.getFuncaoTransacaos());

        carregarFuncoes(lstFuncaoDados, lstFuncaoTransacaos, lstOrganizadaFuncaoDados, lstOrganizadaFuncaoTransacao);
        analisePrincipal.setFuncaoDados(lstOrganizadaFuncaoDados);
        analisePrincipal.setFuncaoTransacaos(lstOrganizadaFuncaoTransacao);
        analiseDivergenciaPrincipal.setFuncaoDados(bindDivergenceFuncaoDados(analisePrincipal, analiseDivergenciaPrincipal));
        analiseDivergenciaPrincipal.setFuncaoTransacaos(bindDivergenceFuncaoTransacaos(analisePrincipal, analiseDivergenciaPrincipal));
    }

    private void carregarFuncoes
        (Set<FuncaoDados> lstFuncaoDados, Set<FuncaoTransacao> lstFuncaoTransacaos, Set<FuncaoDados> lstOrganizadaFuncaoDados, Set<FuncaoTransacao> lstOrganizadaFuncaoTransacao) {
        carregarFuncoesDados(lstFuncaoDados, lstOrganizadaFuncaoDados);
        carregarFuncoesTransacao(lstFuncaoTransacaos, lstOrganizadaFuncaoTransacao);
    }

    private void carregarFuncoesTransacao
        (Set<FuncaoTransacao> lstFuncaoTransacaos, Set<FuncaoTransacao> lstOrganizadaFuncaoTransacao) {
        int ordem = 1;
        for (FuncaoTransacao funcao : lstFuncaoTransacaos) {
            if (funcao.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO)) {
                funcao.setOrdem((long) ordem++);
                lstOrganizadaFuncaoTransacao.add(funcao);
                for (FuncaoTransacao funcaoSecundaria : lstFuncaoTransacaos) {
                    if (!funcaoSecundaria.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO) && isFuncaoEquiparada(funcao, funcaoSecundaria)) {
                        funcaoSecundaria.setOrdem((long) ordem++);
                        lstOrganizadaFuncaoTransacao.add(funcaoSecundaria);
                    }
                }
            }
        }

        for (FuncaoTransacao funcao : lstFuncaoTransacaos) {
            if (!funcao.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO) && !lstOrganizadaFuncaoTransacao.contains(funcao)) {
                funcao.setOrdem((long) ordem++);
                lstOrganizadaFuncaoTransacao.add(funcao);
            }
        }
    }


    private void carregarFuncoesDados
        (Set<FuncaoDados> lstFuncaoDados, Set<FuncaoDados> lstOrganizadaFuncaoDados) {
        int ordem = 1;
        for (FuncaoDados funcao : lstFuncaoDados) {
            if (funcao.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO)) {
                funcao.setOrdem((long) ordem++);
                lstOrganizadaFuncaoDados.add(funcao);
                for (FuncaoDados funcaoSecundaria : lstFuncaoDados) {
                    if (!funcaoSecundaria.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO) && isFuncaoEquiparada(funcao, funcaoSecundaria)) {
                        funcaoSecundaria.setOrdem((long) ordem++);
                        lstOrganizadaFuncaoDados.add(funcaoSecundaria);
                    }
                }
            }
        }
    }

        for (FuncaoDados funcao : lstFuncaoDados) {
            if (!funcao.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO) && !lstOrganizadaFuncaoDados.contains(funcao)) {
                funcao.setOrdem((long) ordem++);
                lstOrganizadaFuncaoDados.add(funcao);
            }
        }
    }

    private boolean isFuncaoEquiparada(FuncaoAnalise funcaoPrimaria, FuncaoAnalise funcaoSecundaria) {
        return funcaoPrimaria.getName().equals(funcaoSecundaria.getName()) && funcaoPrimaria.getFuncionalidade().getNome().equals(funcaoSecundaria.getFuncionalidade().getNome()) && funcaoPrimaria.getFuncionalidade().getModulo().getNome().equals(funcaoSecundaria.getFuncionalidade().getModulo().getNome());
    }


    public Analise salvar(Analise analise) {
        if (analise.getIsDivergence() != null) {
            if (Boolean.FALSE.equals(analise.getIsDivergence())) {
                atualizarPF(analise);
            } else {
                atualizarPFDivergente(analise);
            }
        }
        salvarAnalise(analise);
        return analise;
    }

    public Analise atualizarDivergenciaAnalise(Analise analise) {
        analise.setIdentificadorAnalise(analise.getId().toString());
        analise = salvar(analise);
        return analise;
    }

    public void excluirDivergencia(Analise analise) {
        analise.getCompartilhadas().forEach(compartilhada -> compartilhadaRepository.delete(compartilhada.getId()));
        analise.getAnalisesComparadas().forEach(analiseComparada -> {
            analiseComparada.setAnaliseDivergence(null);
            salvar(analiseComparada);
            analiseFacade.inserirHistoricoAnalise(analiseComparada, null, String.format("A validação %s foi excluída", analise.getIdentificadorAnalise()));
        });
        excluirAnalise(analise);
    }

    public AnaliseDTO carregarAnaliseExcel(AnaliseDTO analiseDTO) {
        Analise analise = converterParaEntidade(analiseDTO);
        Analise analiseNova = new Analise();
        analiseNova.setOrganizacao(analise.getOrganizacao());
        analiseNova.setManual(analise.getManual());
        analiseNova.setEsforcoFases(analise.getEsforcoFases());
        analiseNova.setSistema(analise.getSistema());
        analiseNova.setContrato(analise.getContrato());
        analiseNova.setStatus(analise.getStatus());
        carregarDadosExcel(analiseNova, analise);
        return converterParaDto(analiseNova);
    }

    public void carregarDadosExcel(Analise analiseNova, Analise analise) {
        analiseNova.setDocumentacao(analise.getDocumentacao());
        analiseNova.setTipoAnalise(analise.getTipoAnalise());
        analiseNova.setPropositoContagem(analise.getPropositoContagem());
        analiseNova.setObservacoes(analise.getObservacoes());
        analiseNova.setNumeroOs(analise.getNumeroOs());
        analiseNova.setMetodoContagem(analise.getMetodoContagem());
        analiseNova.setIsDivergence(analise.getIsDivergence());
        analiseNova.setIdentificadorAnalise(analise.getIdentificadorAnalise());
        analiseNova.setFronteiras(analise.getFronteiras());
        analiseNova.setEscopo(analise.getEscopo());
        analiseNova.setFuncaoDados(analise.getFuncaoDados());
        analiseNova.setFuncaoTransacaos(analise.getFuncaoTransacaos());
        analiseNova.setDataCriacaoOrdemServico(analise.getDataCriacaoOrdemServico());
    }

    public boolean verificaModulos(AnaliseEditDTO analiseDTO) {
        if (analiseDTO.getSistema() != null) {
            return sistemaRepository.findOne(analiseDTO.getSistema().getId()).getModulos().isEmpty();
        }
        return false;
    }

    private void verificarFuncoes(FuncaoAnalise funcao, Analise analise) {
        if (analise.getManual() != null && (!analise.getManual().getFatoresAjuste().contains(funcao.getFatorAjuste()))) {
            funcao.setFatorAjuste(new ArrayList<>(analise.getManual().getFatoresAjuste()).get(0));
            analise.getManual().getFatoresAjuste().forEach(fatorAjuste -> {
                if (funcao.getFatorAjuste().getNome().equals(fatorAjuste.getNome())) {
                    funcao.setFatorAjuste(fatorAjuste);
                }
            });

        }
    }


    public void salvarFuncoesExcel(Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaoTransacaos, Analise analise) {
        salvarFuncaoDadosExcel(funcaoDados, analise);
        salvarFuncaoTransacaoExcel(funcaoTransacaos, analise);
        atualizarPF(analise);
        salvarAnalise(analise);
    }

    public Analise importarAnaliseExcel(AnaliseEditDTO analiseDTO) {
        User usuario = analiseFacade.obterUsuarioPorLogin();
        Analise analise = converterEditDtoParaEntidade(analiseDTO);
        analise.setIdentificadorAnalise(analise.getIdentificadorAnalise() + " Importada");
        analise.setCreatedBy(usuario);
        analise.getUsers().add(usuario);
        analise.setSistema(sistemaRepository.findOne(analise.getSistema().getId()));
        analise.setOrganizacao(organizacaoRepository.findOne(analise.getOrganizacao().getId()));
        Set<FuncaoDados> funcaoDados = analise.getFuncaoDados();
        Set<FuncaoTransacao> funcaoTransacaos = analise.getFuncaoTransacaos();
        analise.setFuncaoTransacaos(new HashSet<>());
        analise.setFuncaoDados(new HashSet<>());
        analise.setDataCriacaoOrdemServico(Timestamp.from(Instant.now()));
        salvarAnalise(analise);
        salvarFuncoesExcel(funcaoDados, funcaoTransacaos, analise);
        return analise;
    }

    private void salvarFuncaoTransacaoExcel(Set<FuncaoTransacao> funcaoTransacaos, Analise analise) {
        funcaoTransacaos.forEach(funcaoTransacao -> {
            funcaoTransacao.setId(null);
            funcaoTransacao.setAnalise(analise);
            funcaoTransacao.setEquipe(null);
            verificarFuncoes(funcaoTransacao, analise);
            funcaoTransacao.getDers().forEach(der -> {
                der.setFuncaoTransacao(funcaoTransacao);
                der.setId(null);
            });
            funcaoTransacao.getAlrs().forEach((alr -> {
                alr.setFuncaoTransacao(funcaoTransacao);
                alr.setId(null);
            }));
            setarFuncionalidadeFuncao(funcaoTransacao, analise);
            analiseFacade.salvarFuncaoTransacao(funcaoTransacao);
        });
    }

    private void salvarFuncaoDadosExcel(Set<FuncaoDados> funcaoDados, Analise analise) {
        funcaoDados.forEach(funcaoDado -> {
            funcaoDado.setId(null);
            funcaoDado.setAnalise(analise);
            verificarFuncoes(funcaoDado, analise);
            funcaoDado.getDers().forEach(der -> {
                der.setFuncaoDados(funcaoDado);
                der.setId(null);
            });
            funcaoDado.getRlrs().forEach(rlr -> {
                rlr.setFuncaoDados(funcaoDado);
                rlr.setId(null);
            });
            setarFuncionalidadeFuncao(funcaoDado, analise);
            analiseFacade.salvarFuncaoDado(funcaoDado);
        });
    }

    private void setarFuncionalidadeFuncao(FuncaoAnalise funcao, Analise analise) {
        funcao.setFuncionalidade(new ArrayList<>(new ArrayList<>(analise.getSistema().getModulos()).get(0).getFuncionalidades()).get(0));
        analise.getSistema().getModulos().forEach(modulo -> modulo.getFuncionalidades().forEach(func -> {
            if (func.getNome().contains(funcao.getFuncionalidade().getNome())) {
                funcao.setFuncionalidade(func);
            }
        }));
    }

    public List<AnaliseDTO> carregarAnalisesFromFuncaoFD(String nomeFuncao, String nomeModulo, String
        nomeFuncionalidade, String nomeSistema, String nomeEquipe) {
        return analiseFacade.obterPorFuncaoDados(nomeFuncao, nomeModulo, nomeFuncionalidade, nomeSistema, nomeEquipe).stream().map(this::converterParaDto).collect(Collectors.toList());
    }

    public List<AnaliseDTO> carregarAnalisesFromFuncaoFT(String nomeFuncao, String nomeModulo, String
        nomeFuncionalidade, String nomeSistema, String nomeEquipe) {
        return analiseFacade.obterPorFuncaoTransacao(nomeFuncao, nomeModulo, nomeFuncionalidade, nomeSistema, nomeEquipe).stream().map(this::converterParaDto).collect(Collectors.toList());
    }

    public void salvarCompartilhadasMultiplas(Set<CompartilhadaDTO> compartilhadaList, AbacoMensagens
        abacoMensagens) {
        List<Long> idsAnalise = compartilhadaList.stream().findFirst().orElse(new CompartilhadaDTO()).getAnalisesId();
        for (Long idAnalise : idsAnalise) {
            Set<Compartilhada> compartilhadas = new LinkedHashSet<>();
            Analise analise = analiseFacade.obterAnalisePorIdLimpo(idAnalise);
            List<Long> compartilhaveis = tipoEquipeRepository.findAllEquipesCompartilhaveis(analise.getOrganizacao().getId(), analise.getEquipeResponsavel().getId(), analise.getId()).stream().map(TipoEquipe::getId).collect(Collectors.toList());
            List<Long> jaCompartilhadas = analise.getCompartilhadas().stream().map(Compartilhada::getEquipeId).collect(Collectors.toList());

            compartilhadaList.forEach(compartilhadaDto -> {
                if (!jaCompartilhadas.isEmpty() && jaCompartilhadas.contains(compartilhadaDto.getEquipeId())) {
                    abacoMensagens.adicionarNovoErro("Analise " + analise.getIdentificadorAnalise() + " já compartilhada para " + compartilhadaDto.getNomeEquipe());
                } else {
                    if (compartilhaveis.contains(compartilhadaDto.getEquipeId())) {
                        Compartilhada compartilhada = new Compartilhada();
                        compartilhada.setAnaliseId(idAnalise);
                        compartilhada.setEquipeId(compartilhadaDto.getEquipeId());
                        compartilhada.setNomeEquipe(compartilhadaDto.getNomeEquipe());
                        compartilhada.setViewOnly(compartilhadaDto.isViewOnly());
                        compartilhadaRepository.save(compartilhada);
                        compartilhadas.add(compartilhada);
                    } else {
                        abacoMensagens.adicionarNovoErro("Não é permitido compartilhar análise " + analise.getIdentificadorAnalise() + " para equipe " + compartilhadaDto.getNomeEquipe() + ".");
                    }
                }
            });
            compartilharAnalise(analise, compartilhadas, abacoMensagens);

        }
    }

    public void saveAnaliseCompartilhada(Set<Compartilhada> lstCompartilhadas, AbacoMensagens
        abacoMensagens) {
        if (lstCompartilhadas != null && !lstCompartilhadas.isEmpty()) {
            long idAnalise = lstCompartilhadas.stream().findFirst().get().getAnaliseId();
            Analise analise = analiseFacade.obterAnalisePorIdLimpo(idAnalise);
            compartilharAnalise(analise, lstCompartilhadas, abacoMensagens);
        }
    }

    private void compartilharAnalise(Analise analise, Set<Compartilhada> compartilhadas, AbacoMensagens
        abacoMensagens) {
        if (!compartilhadas.isEmpty()) {
            analise.setCompartilhadas(compartilhadas);
            atualizarPF(analise);
            salvarAnalise(analise);
            List<String> nomeDasEquipes = new ArrayList<>();
            compartilhadas.forEach(compartilhada -> {
                TipoEquipe tipoEquipe = tipoEquipeRepository.findById(compartilhada.getEquipeId());
                nomeDasEquipes.add(tipoEquipe.getNome());
                if (!(StringUtils.isEmptyString(tipoEquipe.getEmailPreposto()) && StringUtils.isEmptyString(tipoEquipe.getPreposto()))) {
                    mailService.sendAnaliseSharedEmail(analise, tipoEquipe);
                }
            });
            analiseFacade.inserirHistoricoAnalise(analise, null, String.format("Compartilhou para a(s) equipe(s) %s", String.join(", ", nomeDasEquipes)));
            abacoMensagens.adicionarNovoSucesso(String.format("Análise " + analise.getIdentificadorAnalise() + " compartilhou para a(s) equipe(s) %s", String.join(", ", nomeDasEquipes)));
        }
    }

    public void prepararAtualizarPF(Analise analise) {
        if (analise.getIsDivergence() == null || !analise.getIsDivergence()) {
            atualizarPF(analise);
        } else {
            atualizarPFDivergente(analise);
        }
    }

    public void inserirHistoricoBloquearDesbloquear(Analise analise) {
        if (analise.getIsDivergence() != null && analise.getIsDivergence() && analise.getAnalisesComparadas() != null) {
            analise.getAnalisesComparadas().forEach(analisePai -> analiseFacade.inserirHistoricoAnalise(analisePai, null, analise.isBloqueiaAnalise() ? String.format("A validação %s foi bloqueada", analise.getIdentificadorAnalise()) : String.format("A validação %s foi desbloqueada", analise.getIdentificadorAnalise())));
        } else {
            analiseFacade.inserirHistoricoAnalise(analise, null, analise.isBloqueiaAnalise() ? "Bloqueou" : "Desbloqueou");
        }
    }

    public void excluirAnalise(Analise analise) {
        analiseFacade.excluirAnalise(analise);
    }

    public List<Analise> obterAnalisesDivergenciaForaDoPrazo() {
        return analiseFacade.obterAnalisesDivergenciaForaDoPrazo();
    }

    public AnaliseDTO criarAnalise(AnaliseEditDTO analiseDTO) {
        User user = analiseFacade.obterUsuarioPorLogin();
        Analise analise = converterEditDtoParaEntidade(analiseDTO);
        analise.setSistema(sistemaRepository.findOne(analise.getSistema().getId()));
        analise.setOrganizacao(organizacaoRepository.findOne(analise.getOrganizacao().getId()));
        analise.setCreatedBy(user);
        analise.getUsers().add(analise.getCreatedBy());
        salvarAnalise(analise);
        analiseFacade.inserirHistoricoAnalise(analise, user, "Criada");
        return converterParaDto(analise);
    }

    public AnaliseDTO atualizarAnalise(AnaliseEditDTO analiseDTO) {
        Analise analise = analiseFacade.obterAnalisePorIdLimpo(analiseDTO.getId());
        anexarAnalise(converterEditDtoParaEntidade(analiseDTO), analise);
        atualizarPF(analise);
        analise.setEditedBy(analiseFacade.obterAnalisePorIdLimpo(analise.getId()).getCreatedBy());
        analise.setAnaliseClonadaParaEquipe(null);
        salvarAnalise(analise);
        return converterParaDto(analise);
    }

    public AnaliseEditDTO bloquearDesbloquearAnalise(Long idAnalise) {
        Analise analise = recuperarAnalise(idAnalise);
        if (analise != null) {
            if (analise.getDataHomologacao() == null) {
                analise.setDataHomologacao(Timestamp.from(Instant.now()));
            }
            if (analise.getStatus().getNome().equals("Aprovada")) {
                analise.getAnalisesComparadas().forEach(analiseComparada -> {
                    analiseComparada.setDtEncerramento(Timestamp.from(Instant.now()));
                    analiseComparada.setIsEncerrada(true);
                    analiseComparada.setStatus(analise.getStatus());
                    salvarAnalise(analiseComparada);
                });
            }
            analise.setBloqueiaAnalise(!analise.isBloqueiaAnalise());
            prepararAtualizarPF(analise);
            salvarAnalise(analise);
            inserirHistoricoBloquearDesbloquear(analise);
        }
        return converterParaAnaliseEditDTO(analise);
    }

    public AnaliseDTO clonarAnalise(Long idAnalise, Long idEquipe) {
        User usuario = analiseFacade.obterUsuarioPorLogin();
        Analise analise = recuperarAnalise(idAnalise);
        Analise analiseClone = anexarAnaliseClone(analise, usuario, idEquipe);
        return converterParaDto(analiseClone);
    }

    private Analise anexarAnaliseClone(Analise analise, User usuario, Long idEquipe) {
        Analise analiseClone = new Analise(analise, usuario);
        if ((analise.getClonadaParaEquipe() == null || !analise.getClonadaParaEquipe()) && idEquipe == null) {
            setarAnaliseClone(analiseClone, analise, usuario, null);
            salvarAnalise(analiseClone);
            analiseFacade.inserirHistoricoAnalise(analise, null, String.format("Clonou para a análise %s", analiseClone.getNumeroOs() == null ? analiseClone.getIdentificadorAnalise() : analiseClone.getNumeroOs()));
            analiseFacade.inserirHistoricoAnalise(analiseClone, null, String.format("Clonada da análise %s", analise.getNumeroOs() == null ? analise.getIdentificadorAnalise() : analise.getNumeroOs()));
        } else {
            TipoEquipe equipe = tipoEquipeRepository.findById(idEquipe);
            setarAnaliseClone(analiseClone, analise, usuario, idEquipe);
            salvarAnalise(analiseClone);
            analise.setClonadaParaEquipe(true);
            analise.setAnaliseClonou(true);
            analise.setAnaliseClonadaParaEquipe(analiseClone);
            salvarAnalise(analise);
            analiseFacade.inserirHistoricoAnalise(analise, null, String.format("Clonou para equipe %s a análise %s", equipe.getNome(), analiseClone.getNumeroOs() == null ? analiseClone.getIdentificadorAnalise() : analiseClone.getNumeroOs()));
            analiseFacade.inserirHistoricoAnalise(analiseClone, null, String.format("Clonada para equipe %s da análise %s", equipe.getNome(), analise.getNumeroOs() == null ? analise.getIdentificadorAnalise() : analise.getNumeroOs()));
        }
        return analiseClone;
    }

    public AnaliseEditDTO obterAnalise(Long idAnalise) {
        Analise analise = recuperarAnalise(idAnalise);
        AnaliseEditDTO analiseEditDTO = converterParaAnaliseEditDTO(analise);
        if (recuperarAnalise(idAnalise) != null && permissaoParaEditar(analiseFacade.obterUsuarioPorLogin(), analise) && analise.getAnaliseDivergence() != null) {
            analiseEditDTO.setAnaliseDivergence(converterParaDto(analise.getAnaliseDivergence()));
        }
        return converterParaAnaliseEditDTO(analise);
    }

    public void deletarAnalise(Long idAnalise) {
        Analise analise = recuperarAnalise(idAnalise);
        User user = analiseFacade.obterUsuarioPorLogin();
        if (analise != null && user.getOrganizacoes().contains(analise.getOrganizacao()) && user.getTipoEquipes().contains(analise.getEquipeResponsavel())) {
            if (analise.getAnaliseClonadaParaEquipe() != null) {
                Analise analiseClonada = recuperarAnalise(analise.getAnaliseClonadaParaEquipe().getId());
                analise.setAnaliseClonadaParaEquipe(null);
                analiseClonada.setAnaliseClonadaParaEquipe(null);
                analiseClonada.setAnaliseClonou(false);
                analiseClonada.setClonadaParaEquipe(false);
                salvarAnalise(analiseClonada);
            }
            excluirAnalise(analise);
        }
    }

    public AbacoMensagens preencherCompartilhar(Set<CompartilhadaDTO> compartilhadaList, Boolean ehMultiplo) {
        AbacoMensagens abacoMensagens = new AbacoMensagens();
        if (Boolean.TRUE.equals(ehMultiplo)) {
            salvarCompartilhadasMultiplas(compartilhadaList, abacoMensagens);
        } else {
            Set<Compartilhada> compartilhadas = new LinkedHashSet<>();
            compartilhadaList.forEach(compartilhadaDto -> {
                Compartilhada compartilhada = new Compartilhada();
                compartilhada.setAnaliseId(compartilhadaDto.getAnalisesId().get(0));
                compartilhada.setEquipeId(compartilhadaDto.getEquipeId());
                compartilhada.setNomeEquipe(compartilhadaDto.getNomeEquipe());
                compartilhada.setViewOnly(compartilhadaDto.isViewOnly());
                compartilhadaRepository.save(compartilhada);
                compartilhadas.add(compartilhada);
            });
            saveAnaliseCompartilhada(compartilhadas, abacoMensagens);
        }
        return abacoMensagens;
    }

    public void deletarAnaliseCompartilhada(Long idAnalise) {
        Compartilhada compartilhada = compartilhadaRepository.getOne(idAnalise);
        TipoEquipe tipoEquipe = tipoEquipeRepository.findById(compartilhada.getEquipeId());
        Analise analise = analiseFacade.obterAnaliseCompartilhada(compartilhada.getAnaliseId());
        analise.getCompartilhadas().remove(compartilhada);
        atualizarPF(analise);
        salvarAnalise(analise);
        compartilhadaRepository.delete(idAnalise);
        analiseFacade.inserirHistoricoAnalise(analise, null, String.format("Descompartilhou para a equipe %s", tipoEquipe.getNome()));
    }

    public Analise obterAnaliseSetarFuncoes(Long idAnalise) {
        Analise analise = analiseFacade.obterAnalisePorIdLimpo(idAnalise);
        if (analise.getIsDivergence() != null && !analise.getIsDivergence()) {
            analise.setFuncaoDados(analiseFacade.obterFuncaoDadosPorAnaliseId(idAnalise));
            analise.setFuncaoTransacaos(analiseFacade.obterFuncaoTransacaoPorAnaliseId(idAnalise));
        }
        analise.setFuncaoDados(analiseFacade.obterFuncaoDadosPorAnaliseIdStatusFuncao(idAnalise));
        analise.setFuncaoTransacaos(analiseFacade.obterFuncaoTransacaoPorAnaliseIdStatusFuncao(idAnalise));
        return analise;
    }

    public UploadedFile arquivosCarregados(Long idAnalise) {
        Analise analise = obterAnaliseSetarFuncoes(idAnalise);
        Long idLogo = analise.getOrganizacao().getLogoId();
        UploadedFile uploadedFiles = new UploadedFile();
        if (idLogo != null && idLogo > 0) {
            uploadedFiles = uploadedFilesRepository.findOne(idLogo);
        }
        return uploadedFiles;
    }

    public ByteArrayOutputStream gerarRelatorioPdf(String query) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            new NativeSearchQueryBuilder().withQuery(multiMatchQuery(query)).build();
            Page<Analise> result = analiseFacade.obterTodasAnalises();
            byteArrayOutputStream = analiseFacade.exportar(new RelatorioAnaliseColunas(null), result, "pdf", Optional.empty(), Optional.of(AbacoUtil.REPORT_LOGO_PATH), Optional.of(AbacoUtil.getReportFooter()));
        } catch (DRException | ClassNotFoundException | JRException | NoClassDefFoundError e) {
            throw new RelatorioException(e);
        }
        return byteArrayOutputStream;
    }

    public ByteArrayOutputStream gerarRelatorioExportacao(String tipoRelatorio, AnaliseFilterDTO filtro) throws
        RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            Page<Analise> page = analiseFacade.obterPaginaAnaliseRelatorio(filtro);
            byteArrayOutputStream = analiseFacade.exportar(new RelatorioAnaliseColunas(filtro.getColumnsVisible()), page, tipoRelatorio, Optional.empty(), Optional.of(AbacoUtil.REPORT_LOGO_PATH), Optional.of(AbacoUtil.getReportFooter()));
        } catch (DRException | ClassNotFoundException | JRException | NoClassDefFoundError e) {
            throw new RelatorioException(e);
        }
        return byteArrayOutputStream;
    }

    public ByteArrayOutputStream gerarRelatorioAnaliseImprimir(AnaliseFilterDTO filtro) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            Page<Analise> page = analiseFacade.obterPaginaAnaliseRelatorio(filtro);
            byteArrayOutputStream = analiseFacade.exportar(new RelatorioAnaliseColunas(filtro.getColumnsVisible()), page, "pdf", Optional.empty(), Optional.of(AbacoUtil.REPORT_LOGO_PATH), Optional.of(AbacoUtil.getReportFooter()));
        } catch (DRException | ClassNotFoundException | JRException | NoClassDefFoundError e) {
            log.error("Erro ao gerar o relátorio: ".concat(e.getMessage()));
        }
        return byteArrayOutputStream;
    }

    public byte[] gerarRelatorioDivergenciaImprimir(AnaliseFilterDTO filtro) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream = gerarRelatorioDivergencia("pdf", filtro);
        return byteArrayOutputStream.toByteArray();
    }

    private ByteArrayOutputStream gerarRelatorioDivergencia(String tipoRelatorio, AnaliseFilterDTO filtro) throws
        RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            Page<Analise> page = analiseFacade.obterPaginaAnaliseDivergenciaRelatorio(filtro);
            byteArrayOutputStream = analiseFacade.exportar(new RelatorioDivergenciaColunas(), page, tipoRelatorio, Optional.empty(), Optional.of(AbacoUtil.REPORT_LOGO_PATH), Optional.of(AbacoUtil.getReportFooter()));
        } catch (DRException | ClassNotFoundException | JRException | NoClassDefFoundError e) {
            throw new RelatorioException(e);
        }
        return byteArrayOutputStream;
    }

    public ByteArrayOutputStream gerarRelatorioDivergenciaExportacao(String tipoRelatorio,
                                                                     AnaliseFilterDTO filter) throws RelatorioException {
        return gerarRelatorioDivergencia(tipoRelatorio, filter);
    }

    public Page<AnaliseDTO> obterTodasAnalisesEquipes(String order,
                                                      int pageNumber,
                                                      int size,
                                                      String sort,
                                                      String identificador,
                                                      Set<Long> sistema,
                                                      Set<MetodoContagem> metodo,
                                                      Set<Long> organizacao,
                                                      Long equipe,
                                                      Set<Long> status,
                                                      Set<Long> usuario,
                                                      TipoDeDataAnalise data,
                                                      Date dataInicio,
                                                      Date dataFim) {
        log.debug("DEBUG Consulta Analises - Inicio metodo");
        SortOrder sortOrderQb;
        if (order.equals("asc")) {
            sortOrderQb = SortOrder.ASC;
        } else {
            sortOrderQb = SortOrder.DESC;
        }
        Sort.Direction sortOrder = PageUtils.getSortDirection(order);
        Pageable pageable = new PageRequest(pageNumber, size, sortOrder, sort);
        FieldSortBuilder sortBuilder = new FieldSortBuilder(sort).order(sortOrderQb);
        BoolQueryBuilder qb = analiseFacade.obterBoolQueryBuilder(identificador, sistema, metodo, organizacao, equipe, usuario, status, data, dataInicio, dataFim);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(qb).withPageable(analiseFacade.obterPaginacaoMaximaExportacao()).withSort(sortBuilder).build();
        Page<Analise> page = analiseFacade.obterPaginaAnalise(searchQuery);
        log.debug("DEBUG Consulta Analises -  Consulta realizada");
        Page<AnaliseDTO> dtoPage = page.map(this::converterParaDto);
        log.debug("DEBUG Consulta Analises -  Conversão realizada");
        return perfilService.validarPerfilAnalise(dtoPage, pageable, false);
    }

    public AnaliseEditDTO atualizarSomaPF(Long idAnalise) {
        Analise analise = recuperarAnalise(idAnalise);
        if (analise.getId() != null) {
            atualizarPF(analise);
            salvarAnalise(analise);
            return converterParaAnaliseEditDTO(analise);
        } else {
            return new AnaliseEditDTO();
        }
    }

    public AnaliseEditDTO atualizarSomaDivergentePF(Long idAnalise) {
        Analise analise = recuperarAnalise(idAnalise);
        if (analise.getId() != null) {
            atualizarPFDivergente(analise);
            salvarAnalise(analise);
            return converterParaAnaliseEditDTO(analise);
        } else {
            return new AnaliseEditDTO();
        }
    }

    public AnaliseFormulario alterarStatusAnalise(Long idAnalise, Long idStatus) {
        AbacoMensagens mensagens;
        AnaliseFormulario formulario = new AnaliseFormulario();
        Analise analise = recuperarAnalise(idAnalise);
        Status status = analiseFacade.obterStatusPorId(idStatus);
        User user = analiseFacade.obterUsuarioPorLogin();
        mensagens = AnaliseValidador.validarAlterarStatus(idAnalise, idStatus, analise, status, user);

        if (Boolean.TRUE.equals(mensagens.contemAvisoOuErro())) {
            formulario.setMensagens(mensagens);
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
            return formulario;
        }
        return abacoMensagens;
    }

        if (changeStatusAnalise(analise, status, user)) {
            if (Boolean.FALSE.equals(analise.getIsDivergence())) {
                atualizarPF(analise);
            } else {
                atualizarPFDivergente(analise);
            }
            salvarAnalise(analise);
            if (Boolean.TRUE.equals(analise.getIsDivergence()) && analise.getAnalisesComparadas() != null) {
                analise.getAnalisesComparadas().forEach(analisePai -> analiseFacade.inserirHistoricoAnalise(analisePai, user, String.format("A validação %s alterou o status para %s", analise.getIdentificadorAnalise(), status.getNome())));
            } else {
                analiseFacade.inserirHistoricoAnalise(analise, user, "Alterou o status para " + status.getNome());
            }
            mensagens.adicionarNovoSucesso("O status da análise " + analise.getIdentificadorAnalise() + " foi alterado para " + status.getNome());
            formulario.setMensagens(mensagens);
            formulario.setAnalise(converterParaAnaliseEditDTO(analise));
        } else {
            mensagens.adicionarNovoErro("Usuário não tem permissão para alterar o status");
            formulario.setMensagens(mensagens);
        }
        return formulario;
    }

    public AnaliseEditDTO gerarDivergencia1(Long idAnaliseComparada) {
        Analise analise = analiseFacade.obterAnalisePorId(idAnaliseComparada);
        Status status = analiseFacade.obterStatusPorNome("Em Análise").orElse(analiseFacade.obterPrimeiroStatusPorDivergencia());
        if (status == null || status.getId() == null) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error status");
        }
        if (analise == null || analise.getId() == null) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error analise");
        }
        Analise analiseDivergencia = generateDivergence1(analise, status);
        return converterParaAnaliseEditDTO(analiseDivergencia);
    }

    public AnaliseEditDTO gerarDivergencia2(Long idAnalisePadrao, Long idAnaliseComparada, Boolean isUnionFunction) {
        Analise analisePadrao = analiseFacade.obterAnalisePorId(idAnalisePadrao);
        Analise analiseComparada = analiseFacade.obterAnalisePorId(idAnaliseComparada);
        Status status = analiseFacade.obterStatusPorNome("Em Análise").orElse(analiseFacade.obterPrimeiroStatusPorDivergencia());
        if (status == null || status.getId() == null) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error Status");
        }
        if (analisePadrao == null || analisePadrao.getId() == null) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error analise Padrão");
        }
        if (analiseComparada == null || analiseComparada.getId() == null) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error analise Comparada");
        }
        Analise analiseDivergencia = generateDivergence2(analisePadrao, analiseComparada, status, isUnionFunction);
        analiseFacade.inserirHistoricoAnalise(analisePadrao, null, GEROU_VALIDACAO + analiseDivergencia.getId());
        analiseFacade.inserirHistoricoAnalise(analiseComparada, null, GEROU_VALIDACAO + analiseDivergencia.getId());
        return converterParaAnaliseEditDTO(analiseDivergencia);
    }

    public AnaliseEditDTO atualizarAnaliseDivergente(Long idAnalise) {
        Analise analise = analiseFacade.obterAnalisePorId(idAnalise);
        if (analise == null || analise.getId() == null) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error analise Padrão");
        }
        analise = atualizarDivergenciaAnalise(analise);
        return converterParaAnaliseEditDTO(analise);
    }

    public Page<AnaliseDTO> obterDivergencias(String order, int pageNumber, int size, String sort, String
        identificador, Set<Long> sistema, Set<Long> organizacao, Set<Long> status, Boolean bloqueado) {
        log.debug("DEBUG Consulta Validação -  Inicio método");
        Sort.Direction sortOrder = PageUtils.getSortDirection(order);
        Pageable pageable = new PageRequest(pageNumber, size, sortOrder, sort);
        FieldSortBuilder sortBuilder = new FieldSortBuilder(sort).order(SortOrder.DESC);
        BoolQueryBuilder qb = analiseFacade.obterBoolQueryBuilderDivergencia(identificador, sistema, organizacao, status, bloqueado);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(qb).withPageable(analiseFacade.obterPaginacaoMaximaExportacao()).withSort(sortBuilder).build();
        Page<Analise> page = analiseFacade.obterPaginaAnalise(searchQuery);
        log.debug("DEBUG Consulta Validação -  Consulta realizada");
        Page<AnaliseDTO> dtoPage = page.map(this::converterParaDto);
        log.debug("DEBUG Consulta Validação -  Conversão realizada");
        return perfilService.validarPerfilAnalise(dtoPage, pageable, true);
    }

    public AnaliseDivergenceEditDTO obterDivergencia(Long idAnalise) {
        Analise analise = recuperarAnaliseDivergence(idAnalise);
        if (analise != null) {
            User user = analiseFacade.obterUsuarioPorLogin();
            if (user.getOrganizacoes().contains(analise.getOrganizacao())) {
                return converterParaAnaliseDivergenciaEditDTO(analise);
            }
        }
        return null;
    }

    public void deletarAnaliseDivergencia(Long idAnalise) {
        Analise analise = recuperarAnalise(idAnalise);
        User user = analiseFacade.obterUsuarioPorLogin();
        if (analise != null) {
            if (user.getOrganizacoes().contains(analise.getOrganizacao())) {
                excluirDivergencia(analise);
            }
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    public byte[] exportarExcel(Long idAnalise, Long modelo) throws IOException {
        ByteArrayOutputStream outputStream = analiseFacade.selecionarModelo(recuperarAnalise(idAnalise), modelo);
        return outputStream.toByteArray();
    }

    public byte[] importarExcelDivergencia(Long idAnalise, Long modelo) throws IOException {
        Analise analise = obterAnaliseSetarFuncoes(idAnalise);
        ByteArrayOutputStream outputStream = analiseFacade.selecionarModeloDivergencia(analise, modelo);
        return outputStream.toByteArray();
    }

    public void atualizarEncerramentoAnalise(AnaliseEncerramentoDTO analiseEncerramentoDTO) {
        Analise analise = analiseFacade.obterAnalisePorIdLimpo(analiseEncerramentoDTO.getId());
        boolean gerarHistorico = false;
        if (analise.getIsEncerrada() == null) {
            analise.setIsEncerrada(false);
        }
        if (Boolean.TRUE.equals(analiseEncerramentoDTO.isEncerrada() != analise.getIsEncerrada() || analise.getDtEncerramento() == null && analiseEncerramentoDTO.getDtEncerramento() != null) || analise.getDtEncerramento() != null && !analise.getDtEncerramento().equals(analiseEncerramentoDTO.getDtEncerramento())) {
            gerarHistorico = true;
        }
        return null;
    }

        if (!analiseEncerramentoDTO.isEncerrada()) {
            analise.setDtEncerramento(null);
        } else {
            analise.setDtEncerramento(analiseEncerramentoDTO.getDtEncerramento());
        }
        analise.setIsEncerrada(analiseEncerramentoDTO.isEncerrada());
        atualizarPF(analise);
        salvarAnalise(analise);
        if (gerarHistorico) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            analiseFacade.inserirHistoricoAnalise(analise, null, Boolean.TRUE.equals(analise.getIsEncerrada()) ? "Encerrou para " + sdf.format(analise.getDtEncerramento()) : "Abriu (Desabilitou o encerramento)");
        }
    }

    public Analise uploadExcel(MultipartFile file) throws IOException {
        FileInputStream inputStream = null;
        Path tempDir = Files.createTempDirectory("");
        File tempFile = tempDir.resolve(file.getOriginalFilename()).toFile();
        file.transferTo(tempFile);
        inputStream = new FileInputStream(tempFile);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        Analise analise = new Analise();
        Set<FuncaoDados> funcaoDados = new HashSet<>();
        Set<FuncaoTransacao> funcaoTransacaos = new HashSet<>();

        setarResumoExcelUpload(workbook, analise);
        if (analise.getMetodoContagem().equals(MetodoContagem.INDICATIVA)) {
            setarIndicativaExcelUpload(workbook, funcaoDados);
        } else {
            setarInmExcelUpload(workbook, funcaoTransacaos, funcaoDados);
        }
        if (analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)) {
            setarExcelDetalhadaUpload(workbook, funcaoDados, funcaoTransacaos);
        } else if (analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)) {
            setarExcelEstimadaUpload(workbook, funcaoDados, funcaoTransacaos);
        }
        analise.setFuncaoDados(funcaoDados);
        analise.setFuncaoTransacaos(funcaoTransacaos);
        return analise;
    }

    // Planilha Detalhada
    public void setarExcelDetalhadaUpload(XSSFWorkbook excelFile, Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaoTransacaos) {
        XSSFSheet excelSheet = excelFile.getSheet(DETALHADA);
        for (int i = 9; i < 1323; i++) {
            XSSFRow row = excelSheet.getRow(i);
            if (row.getCell(0).getNumericCellValue() > 0) {
                ders = new HashSet<>();
                if (tipoFuncaoDados().contains(row.getCell(6).getStringCellValue())) {
                    rlrs = new HashSet<>();
                    funcaoDados.add(setarFuncaoDadosDetalhada(row));
                } else if (tipoFuncaoTransacao().contains(row.getCell(6).getStringCellValue())) {
                    alrs = new HashSet<>();
                    funcaoTransacaos.add(setarFuncaoTrasacaoDetalhada(row));
                }
            }
        }
    }

    private FuncaoDados setarFuncaoDadosDetalhada(XSSFRow row) {
        FuncaoDados funcaoDados = new FuncaoDados();
        funcaoDados.setId((long) row.getCell(0).getNumericCellValue());
        setarModuloFuncionalidade(funcaoDados, row);
        setarTipoFuncaoDados(row, funcaoDados);
        setarDerRlrFuncaoDados(funcaoDados, row);
        setarSustentacaoStatus(funcaoDados, row);
        return funcaoDados;
    }

    private void setarDerRlrFuncaoDados(FuncaoDados funcaoDados, XSSFRow row) {
        Der der = new Der();
        der.setNome(row.getCell(8).getStringCellValue());
        ders.add(der);
        Rlr rlr = new Rlr();
        rlr.setNome(row.getCell(10).getStringCellValue());
        rlrs.add(rlr);
        funcaoDados.setDers(ders);
        funcaoDados.setRlrs(rlrs);
        setarFuncaoComplexidade(funcaoDados, row);
    }

    private void setarSustentacaoStatus(FuncaoAnalise funcao, XSSFRow row) {
        funcao.setSustantation(row.getCell(17).getStringCellValue());
        if (statusFuncao().contains(row.getCell(19).getStringCellValue())) {
            switch (row.getCell(19).getStringCellValue()) {
                case DIVERGENTE:
                    funcao.setStatusFuncao(StatusFuncao.DIVERGENTE);
                    break;
                case EXCLUIDO:
                    funcao.setStatusFuncao(StatusFuncao.EXCLUIDO);
                    break;
                case VALIDADO:
                    funcao.setStatusFuncao(StatusFuncao.VALIDADO);
                    break;
                case PENDENTE:
                    funcao.setStatusFuncao(StatusFuncao.PENDENTE);
                    break;
                default:
                    break;
            }
        }
    }

    private void setarModuloFuncionalidade(FuncaoAnalise funcao, XSSFRow row) {
        Funcionalidade funcionalidade = new Funcionalidade();
        Modulo modulo = new Modulo();
        FatorAjuste fatorAjuste = new FatorAjuste();
        modulo.setNome(row.getCell(3).getStringCellValue());
        funcionalidade.setModulo(modulo);
        funcionalidade.setNome(row.getCell(4).getStringCellValue());
        funcao.setFuncionalidade(funcionalidade);
        funcao.setName(row.getCell(5).getStringCellValue());
        fatorAjuste.setNome(row.getCell(1).getStringCellValue());
        funcao.setFatorAjuste(fatorAjuste);

    }

    private FuncaoTransacao setarFuncaoTrasacaoDetalhada(XSSFRow row) {
        FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
        funcaoTransacao.setId((long) row.getCell(0).getNumericCellValue());
        setarModuloFuncionalidade(funcaoTransacao, row);
        switch (row.getCell(6).getStringCellValue()) {
            case METODO_CE:
                funcaoTransacao.setTipo(TipoFuncaoTransacao.CE);
                break;
            case METODO_EE:
                funcaoTransacao.setTipo(TipoFuncaoTransacao.EE);
                break;
            case METODO_SE:
                funcaoTransacao.setTipo(TipoFuncaoTransacao.SE);
                break;
            case METODO_INM:
                funcaoTransacao.setTipo(TipoFuncaoTransacao.INM);
                break;
            default:
                break;
        }
        setarDerAlrFuncaoTransacao(funcaoTransacao, row);
        setarSustentacaoStatus(funcaoTransacao, row);
        return funcaoTransacao;
    }

    private void setarDerAlrFuncaoTransacao(FuncaoTransacao funcaoTransacao, XSSFRow row) {
        Der der = new Der();
        der.setNome(row.getCell(8).getStringCellValue());
        ders.add(der);
        Alr alr = new Alr();
        alr.setNome(row.getCell(10).getStringCellValue());
        alrs.add(alr);
        funcaoTransacao.setDers(ders);
        funcaoTransacao.setAlrs(alrs);
        setarFuncaoComplexidade(funcaoTransacao, row);
    }


    // Planilha Estimada
    public void setarExcelEstimadaUpload(XSSFWorkbook excelFile, Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaoTransacaos) {
        XSSFSheet excelSheetEstimada = excelFile.getSheet(ESTIMATIVA);
        for (int i = 10; i < 1081; i++) {
            XSSFRow row = excelSheetEstimada.getRow(i);
            if (row.getCell(0).getNumericCellValue() > 0) {
                if (tipoFuncaoDados().contains(row.getCell(7).getStringCellValue())) {
                    funcaoDados.add(setarFuncaoDadosEstimada(row));
                } else if (tipoFuncaoTransacao().contains(row.getCell(7).getStringCellValue())) {
                    funcaoTransacaos.add(setarFuncaoTransacaoEstimada(row));
                }
            }
        }
    }

    private void setarModuloFuncionalidadeEstimada(FuncaoAnalise funcao, XSSFRow row) {
        Funcionalidade funcionalidade = new Funcionalidade();
        Modulo modulo = new Modulo();
        FatorAjuste fatorAjuste = new FatorAjuste();
        funcao.setId((long) row.getCell(0).getNumericCellValue());
        modulo.setNome(row.getCell(4).getStringCellValue());
        funcionalidade.setModulo(modulo);
        funcionalidade.setNome(row.getCell(5).getStringCellValue());
        funcao.setFuncionalidade(funcionalidade);
        funcao.setName(row.getCell(6).getStringCellValue());
        fatorAjuste.setNome(row.getCell(1).getStringCellValue());
        funcao.setFatorAjuste(fatorAjuste);
    }

    private FuncaoDados setarFuncaoDadosEstimada(XSSFRow row) {
        FuncaoDados funcaoDados = new FuncaoDados();
        setarModuloFuncionalidadeEstimada(funcaoDados, row);
        switch (row.getCell(7).getStringCellValue()) {
            case METODO_AIE:
                funcaoDados.setTipo(TipoFuncaoDados.AIE);
                break;
            case METODO_ALI:
                funcaoDados.setTipo(TipoFuncaoDados.ALI);
                break;
            case METODO_INM:
                funcaoDados.setTipo(TipoFuncaoDados.INM);
                break;
            default:
                break;
        }
        funcaoDados.setPf(BigDecimal.valueOf(row.getCell(8).getNumericCellValue()));
        funcaoDados.setGrossPF(BigDecimal.valueOf(row.getCell(8).getNumericCellValue()));
        funcaoDados.setSustantation(row.getCell(9).getStringCellValue());
        return funcaoDados;
    }

    private FuncaoTransacao setarFuncaoTransacaoEstimada(XSSFRow row) {
        FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
        setarModuloFuncionalidadeEstimada(funcaoTransacao, row);
        switch (row.getCell(7).getStringCellValue()) {
            case METODO_CE:
                funcaoTransacao.setTipo(TipoFuncaoTransacao.CE);
                break;
            case METODO_EE:
                funcaoTransacao.setTipo(TipoFuncaoTransacao.EE);
                break;
            case METODO_SE:
                funcaoTransacao.setTipo(TipoFuncaoTransacao.SE);
                break;
            case METODO_INM:
                funcaoTransacao.setTipo(TipoFuncaoTransacao.INM);
                break;
            default:
                break;
        }
        funcaoTransacao.setPf(BigDecimal.valueOf(row.getCell(8).getNumericCellValue()));
        funcaoTransacao.setGrossPF(BigDecimal.valueOf(row.getCell(8).getNumericCellValue()));
        funcaoTransacao.setSustantation(row.getCell(9).getStringCellValue());
        return funcaoTransacao;
    }

    // Planilha Resumo
    public void setarResumoExcelUpload(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet sheet = excelFile.getSheet(RESUMO);
        analise.setNumeroOs(sheet.getRow(3).getCell(1).getStringCellValue());
        analise.setPropositoContagem(sheet.getRow(9).getCell(0).getStringCellValue());
        analise.setEscopo(sheet.getRow(12).getCell(0).getStringCellValue());
        switch (sheet.getRow(4).getCell(1).getStringCellValue()) {
            case METODO_ESTIMATIVA:
                analise.setMetodoContagem(MetodoContagem.ESTIMADA);
                break;
            case METODO_DETALHADO:
                analise.setMetodoContagem(MetodoContagem.DETALHADA);
                break;
            case METODO_INDICATIVA:
                analise.setMetodoContagem(MetodoContagem.INDICATIVA);
                break;
            default:
                break;
        }
    }

    // Planilha Indicativa
    public void setarIndicativaExcelUpload(XSSFWorkbook excelFile, Set<FuncaoDados> funcaoDadosList) {
        XSSFSheet excelSheetIndicativa = excelFile.getSheet(SHEET_INM_INDICATIVA);
        for (int i = 9; i < 107; i++) {
            XSSFRow row = excelSheetIndicativa.getRow(i);
            if (row.getCell(0).getNumericCellValue() > 0 && (tipoFuncaoDados().contains(row.getCell(6).getStringCellValue()))) {
                funcaoDadosList.add(setarFuncaoDadosIndicativa(row));

            }
        }
    }

    private FuncaoDados setarFuncaoDadosIndicativa(XSSFRow row) {
        FuncaoDados funcaoDados = new FuncaoDados();
        Funcionalidade funcionalidade = new Funcionalidade();
        Modulo modulo = new Modulo();
        funcaoDados.setId((long) row.getCell(0).getNumericCellValue());
        modulo.setNome(row.getCell(2).getStringCellValue());
        funcionalidade.setModulo(modulo);
        funcionalidade.setNome(row.getCell(3).getStringCellValue());
        funcaoDados.setFuncionalidade(funcionalidade);
        funcaoDados.setName(row.getCell(5).getStringCellValue());
        setarTipoFuncaoDados(row, funcaoDados);
        funcaoDados.setPf(BigDecimal.valueOf(row.getCell(7).getNumericCellValue()));
        funcaoDados.setGrossPF(BigDecimal.valueOf(row.getCell(7).getNumericCellValue()));
        funcaoDados.setSustantation(row.getCell(8).getStringCellValue());
        return funcaoDados;
    }

    private void setarTipoFuncaoDados(XSSFRow row, FuncaoDados funcaoDados) {
        switch (row.getCell(6).getStringCellValue()) {
            case METODO_AIE:
                funcaoDados.setTipo(TipoFuncaoDados.AIE);
                break;
            case METODO_ALI:
                funcaoDados.setTipo(TipoFuncaoDados.ALI);
                break;
            case METODO_INM:
                funcaoDados.setTipo(TipoFuncaoDados.INM);
                break;
            default:
                break;
        }
    }

    // Planilha INM
    public void setarInmExcelUpload(XSSFWorkbook excelFile, Set<FuncaoTransacao> funcaoTransacaos, Set<FuncaoDados> funcaoDados) {
        XSSFSheet excelSheetINM = excelFile.getSheet(SHEET_INM);
        for (int i = 10; i < 382; i++) {
            XSSFRow row = excelSheetINM.getRow(i);
            if (row.getCell(0).getNumericCellValue() > 0) {
                ders = new HashSet<>();
                if (tipoFuncaoDados().contains(row.getCell(6).getStringCellValue())) {
                    alrs = new HashSet<>();
                    funcaoTransacaos.add(setarInm(row));
                } else if (tipoFuncaoTransacao().contains(row.getCell(6).getStringCellValue())) {
                    rlrs = new HashSet<>();
                    funcaoDados.add(setarFuncaoDadosInm(row));
                }
            }
        }
    }

    private FuncaoTransacao setarInm(XSSFRow row) {
        FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
        if (TipoFuncaoTransacao.INM.toString().equals(row.getCell(12).getStringCellValue())) {
            setarModuloFuncionalidadeInm(funcaoTransacao, row);
            if (METODO_CE.equals(row.getCell(12).getStringCellValue())) {
                funcaoTransacao.setTipo(TipoFuncaoTransacao.INM);
            }
            setarDerAlrFuncaoTransacao(funcaoTransacao, row);
            funcaoTransacao.setSustantation(row.getCell(17).getStringCellValue());
            if (statusFuncao().contains(row.getCell(21).getStringCellValue())) {
                switch (row.getCell(21).getStringCellValue()) {
                    case DIVERGENTE:
                        funcaoTransacao.setStatusFuncao(StatusFuncao.DIVERGENTE);
                        break;
                    case EXCLUIDO:
                        funcaoTransacao.setStatusFuncao(StatusFuncao.EXCLUIDO);
                        break;
                    case VALIDADO:
                        funcaoTransacao.setStatusFuncao(StatusFuncao.VALIDADO);
                        break;
                    case PENDENTE:
                        funcaoTransacao.setStatusFuncao(StatusFuncao.PENDENTE);
                        break;
                    default:
                        break;
                }
            }
        }
        return funcaoTransacao;
    }

    private FuncaoDados setarFuncaoDadosInm(XSSFRow row) {
        FuncaoDados funcaoDados = new FuncaoDados();
        if (TipoFuncaoTransacao.INM.toString().equals(row.getCell(12).getStringCellValue())) {
            setarModuloFuncionalidadeInm(funcaoDados, row);
            if (row.getCell(12).getStringCellValue().equals(METODO_CE)) {
                funcaoDados.setTipo(TipoFuncaoDados.INM);
            }
            setarDerRlrFuncaoDados(funcaoDados, row);
        }
        return funcaoDados;
    }

    private void setarModuloFuncionalidadeInm(FuncaoAnalise funcao, XSSFRow row) {
        Modulo modulo = new Modulo();
        FatorAjuste fatorAjuste = new FatorAjuste();
        Funcionalidade funcionalidade = new Funcionalidade();
        funcao.setId((long) row.getCell(0).getNumericCellValue());
        modulo.setNome(row.getCell(5).getStringCellValue());
        funcionalidade.setModulo(modulo);
        funcionalidade.setNome(row.getCell(6).getStringCellValue());
        funcao.setFuncionalidade(funcionalidade);
        funcao.setName(row.getCell(7).getStringCellValue());
        fatorAjuste.setNome(row.getCell(1).getStringCellValue());
        funcao.setFatorAjuste(fatorAjuste);
    }

    private void setarFuncaoComplexidade(FuncaoAnalise funcao, XSSFRow row) {
        switch (row.getCell(12).getStringCellValue()) {
            case METODO_SEM:
                funcao.setComplexidade(Complexidade.SEM);
                break;
            case METODO_BAIXO:
                funcao.setComplexidade(Complexidade.BAIXA);
                break;
            case METODO_MEDIO:
                funcao.setComplexidade(Complexidade.MEDIA);
                break;
            case METODO_ALTA:
                funcao.setComplexidade(Complexidade.ALTA);
                break;
            default:
                break;

        }
        funcao.setPf(BigDecimal.valueOf(row.getCell(16).getNumericCellValue()));
        funcao.setGrossPF(BigDecimal.valueOf(row.getCell(16).getNumericCellValue()));
    }

    public void salvarAnalise(Analise analise) {
        analiseFacade.salvarAnalise(analise);
    }

    public Compartilhada salvarCompartilhada(CompartilhadaDTO compartilhadaDTO) {
        return converterCompartilhadaParaEntidade(compartilhadaDTO);
    }

    public Compartilhada converterCompartilhadaParaEntidade(CompartilhadaDTO compartilhadaDTO) {
        return analiseFacade.converterCompartilhadaParaEntidade(compartilhadaDTO);
    }

    public Analise converterParaEntidade(AnaliseDTO analiseDTO) {
        return analiseFacade.converterParaEntidade(analiseDTO);
    }

    public AnaliseEditDTO converterParaAnaliseEditDTO(Analise analise) {
        return analiseFacade.converterParaAnaliseEditDTO(analise);
    }

    public AnaliseJsonDTO converterParaAnaliseJsonDTO(Analise analise) {
        return analiseFacade.converterParaAnaliseJsonDTO(analise);
    }

    public Analise converterEditDtoParaEntidade(AnaliseEditDTO analiseEditDTO) {
        return analiseFacade.converterEditDtoParaEntidade(analiseEditDTO);
    }

    public AnaliseDivergenceEditDTO converterParaAnaliseDivergenciaEditDTO(Analise analise) {
        return analiseFacade.converterParaAnaliseDivergenciaEditDTO(analise);

    }

    public AnaliseDivergenceDTO converterParaAnaliseDivergenciaDTO(Analise analise) {
        return analiseFacade.converterParaAnaliseDivergenciaDTO(analise);
    }


    private List<String> tipoFuncaoDados() {
        List<String> valor = new ArrayList<>();
        valor.add(METODO_ALI);
        valor.add(METODO_AIE);
        valor.add(METODO_INM);
        return valor;
    }

    private List<String> tipoFuncaoTransacao() {
        List<String> valor = new ArrayList<>();
        valor.add(METODO_CE);
        valor.add(METODO_EE);
        valor.add(METODO_SE);
        valor.add(METODO_INM);
        return valor;
    }

    private List<String> statusFuncao() {
        List<String> valor = new ArrayList<>();
        valor.add(DIVERGENTE);
        valor.add(EXCLUIDO);
        valor.add(VALIDADO);
        valor.add(PENDENTE);
        return valor;
    }
}
