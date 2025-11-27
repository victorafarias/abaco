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
import br.com.basis.abaco.domain.Manual;
import br.com.basis.abaco.domain.Modulo;
import br.com.basis.abaco.domain.Rlr;
import br.com.basis.abaco.domain.Sistema;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.domain.TipoEquipe;
import br.com.basis.abaco.domain.UploadedFile;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.domain.VwAnaliseDivergenteSomaPf;
import br.com.basis.abaco.domain.VwAnaliseSomaPf;
import br.com.basis.abaco.domain.enumeration.Complexidade;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoDados;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoTransacao;
import br.com.basis.abaco.repository.AlrRepository;
import br.com.basis.abaco.repository.CompartilhadaRepository;
import br.com.basis.abaco.repository.DerRepository;
import br.com.basis.abaco.repository.FuncionalidadeRepository;
import br.com.basis.abaco.repository.ModuloRepository;
import br.com.basis.abaco.repository.OrganizacaoRepository;
import br.com.basis.abaco.repository.RlrRepository;
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
import br.com.basis.abaco.service.dto.pesquisa.AnalisePesquisaDTO;
import br.com.basis.abaco.service.exception.RelatorioException;
import br.com.basis.abaco.service.facades.AnaliseFacade;
import br.com.basis.abaco.service.relatorio.RelatorioAnaliseColunas;
import br.com.basis.abaco.service.relatorio.RelatorioDivergenciaColunas;
import br.com.basis.abaco.service.validadores.AnaliseValidador;
import br.com.basis.abaco.utils.AbacoUtil;
import br.com.basis.abaco.utils.PageUtils;
import br.com.basis.abaco.utils.StringUtils;
import br.com.basis.abaco.service.exception.FatorAjusteException;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
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

import javax.persistence.EntityManager;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

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

    public static final String ORGANIZACAO_ID = "organizacao.id";
    public static final String EQUIPE_RESPONSAVEL_ID = "equipeResponsavel.id";
    public static final String COMPARTILHADAS_EQUIPE_ID = "compartilhadas.equipeId";
    private static final String EMPTY_STRING = "";
    private static final String BASIS_MINUSCULO = "basis";
    private static final String BASIS = "basis";
    private static final int DECIMAL_PLACE = 2;
    public static final String APROVADA = "Aprovada";
    private final BigDecimal percent = new BigDecimal("100");



    private final Logger log = LoggerFactory.getLogger(AnaliseService.class);
    private final SistemaRepository sistemaRepository;
    private final CompartilhadaRepository compartilhadaRepository;
    private final TipoEquipeRepository tipoEquipeRepository;
    private final UploadedFilesRepository uploadedFilesRepository;
    private final MailService mailService;
    private final PerfilService perfilService;
    private final AnaliseFacade analiseFacade;
    private final OrganizacaoRepository organizacaoRepository;
    private final FuncionalidadeService funcionalidadeService;
    private final ModuloRepository moduloRepository;
    private final FuncionalidadeRepository funcionalidadeRepository;
    private final EntityManager entityManager;
    
    // Alterado: DataFormatter para ler células com fórmulas corretamente
    private final DataFormatter dataFormatter = new DataFormatter();

    /**
     * Alterado: Método helper para ler valor de célula do Excel, tratando fórmulas e valores diretos
     * @param row Linha do Excel
     * @param columnIndex Índice da coluna
     * @return String com o valor da célula (resultado da fórmula ou valor direto)
     */
    private String getCellValueAsString(XSSFRow row, int columnIndex) {
        if (row == null || row.getCell(columnIndex) == null) {
            return "";
        }
        XSSFCell cell = row.getCell(columnIndex);
        // Alterado: Usar FormulaEvaluator para garantir que fórmulas sejam avaliadas antes de formatar
        FormulaEvaluator evaluator = row.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        return dataFormatter.formatCellValue(cell, evaluator);
    }
    
    /**
     * Alterado: Método helper para ler valor numérico de célula do Excel
     * @param row Linha do Excel
     * @param columnIndex Índice da coluna
     * @return double com o valor numérico (resultado da fórmula ou valor direto)
     */
    private double getCellValueAsNumber(XSSFRow row, int columnIndex) {
        if (row == null || row.getCell(columnIndex) == null) {
            return 0.0;
        }
        try {
            // Se for fórmula, getNumericCellValue() retorna o resultado avaliado
            return row.getCell(columnIndex).getNumericCellValue();
        } catch (Exception e) {
            // Se falhar, tenta converter o valor string
            String value = getCellValueAsString(row, columnIndex);
            try {
                return Double.parseDouble(value.replace(",", "."));
            } catch (NumberFormatException nfe) {
                return 0.0;
            }
        }
    }
    
    /**
     * Alterado: Busca ou cria Módulo com correspondência exata de nome e sistemaId
     * @param nomeModulo Nome do módulo
     * @param sistema Sistema ao qual o módulo pertence
     * @return Módulo encontrado ou criado
     */
    private Modulo buscarOuCriarModulo(String nomeModulo, Sistema sistema) {
        if (nomeModulo == null || nomeModulo.trim().isEmpty() || sistema == null) {
            log.warn("buscarOuCriarModulo: nomeModulo ou sistema nulo. nomeModulo='{}', sistema={}", nomeModulo, sistema);
            return null;
        }
        
        String nomeTrimmed = nomeModulo.trim();
        
        log.info("=== BUSCANDO MÓDULO: Nome='{}', SistemaID={}, Sistema.Nome='{}'", nomeTrimmed, sistema.getId(), sistema.getNome());
        
        // Buscar módulo existente com correspondência exata (case insensitive)
        Optional<List<Modulo>> modulosOpt = moduloRepository.findAllByNomeIgnoreCaseAndSistemaId(
            nomeTrimmed, 
            sistema.getId()
        );
        
        log.info("=== RESULTADO DA BUSCA: isPresent={}, isEmpty={}", 
            modulosOpt.isPresent(), 
            (modulosOpt.isPresent() ? modulosOpt.get().isEmpty() : "N/A"));
        
        if (modulosOpt.isPresent() && !modulosOpt.get().isEmpty()) {
            Modulo existente = modulosOpt.get().get(0);
            log.info("=== MÓDULO EXISTENTE ENCONTRADO: ID={}, Nome='{}', SistemaID={}", 
                existente.getId(), existente.getNome(), existente.getSistema().getId());
            return existente;
        }
        
        log.warn("=== MÓDULO NÃO ENCONTRADO. CRIANDO NOVO: Nome='{}', SistemaID={}", nomeTrimmed, sistema.getId());
        
        // Se não encontrou, criar novo módulo
        Modulo novoModulo = new Modulo();
        novoModulo.setNome(nomeTrimmed);
        novoModulo.setSistema(sistema);
        Modulo moduloSalvo = moduloRepository.saveAndFlush(novoModulo);
        log.warn("=== NOVO MÓDULO CRIADO: ID={}, Nome='{}', SistemaID={}", 
            moduloSalvo.getId(), moduloSalvo.getNome(), moduloSalvo.getSistema().getId());
        return moduloSalvo;
    }
    
    /**
     * Alterado: Busca ou cria Funcionalidade com correspondência exata de nome e moduloId
     * @param nomeFuncionalidade Nome da funcionalidade
     * @param modulo Módulo ao qual a funcionalidade pertence
     * @return Funcionalidade encontrada ou criada
     */
    private Funcionalidade buscarOuCriarFuncionalidade(String nomeFuncionalidade, Modulo modulo) {
        if (nomeFuncionalidade == null || nomeFuncionalidade.trim().isEmpty() || modulo == null || modulo.getId() == null) {
            log.warn("buscarOuCriarFuncionalidade: nomeFuncionalidade ou modulo inválido. nomeFuncionalidade='{}', moduloID={}", 
                nomeFuncionalidade, (modulo != null ? modulo.getId() : "null"));
            return null;
        }
        
        String nomeTrimmed = nomeFuncionalidade.trim();
        
        log.info("=== BUSCANDO FUNCIONALIDADE: Nome='{}', ModuloID={}, Modulo.Nome='{}'", 
            nomeTrimmed, modulo.getId(), modulo.getNome());
        
        // Buscar funcionalidade existente com correspondência exata (case insensitive)
        Optional<List<Funcionalidade>> funcionalidadesOpt = funcionalidadeRepository.findAllByNomeIgnoreCaseAndModuloId(
            nomeTrimmed,
            modulo.getId()
        );
        
        log.info("=== RESULTADO DA BUSCA FUNCIONALIDADE: isPresent={}, isEmpty={}", 
            funcionalidadesOpt.isPresent(), 
            (funcionalidadesOpt.isPresent() ? funcionalidadesOpt.get().isEmpty() : "N/A"));
        
        if (funcionalidadesOpt.isPresent() && !funcionalidadesOpt.get().isEmpty()) {
            Funcionalidade existente = funcionalidadesOpt.get().get(0);
            log.info("=== FUNCIONALIDADE EXISTENTE ENCONTRADA: ID={}, Nome='{}', ModuloID={}", 
                existente.getId(), existente.getNome(), existente.getModulo().getId());
            return existente;
        }
        
        log.warn("=== FUNCIONALIDADE NÃO ENCONTRADA. CRIANDO NOVA: Nome='{}', ModuloID={}", nomeTrimmed, modulo.getId());
        
        // Se não encontrou, criar nova funcionalidade
        Funcionalidade novaFuncionalidade = new Funcionalidade();
        novaFuncionalidade.setNome(nomeTrimmed);
        novaFuncionalidade.setModulo(modulo);
        Funcionalidade funcionalidadeSalva = funcionalidadeRepository.saveAndFlush(novaFuncionalidade);
        log.warn("=== NOVA FUNCIONALIDADE CRIADA: ID={}, Nome='{}', ModuloID={}", 
            funcionalidadeSalva.getId(), funcionalidadeSalva.getNome(), funcionalidadeSalva.getModulo().getId());
        return funcionalidadeSalva;
    }

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
        
        // Alterado: Adicionar null check para evitar NullPointerException quando a view não retorna dados
        if (vwAnaliseSomaPf != null) {
            analise.setPfTotal(vwAnaliseSomaPf.getPfGross().setScale(DECIMAL_PLACE));
            analise.setAdjustPFTotal(vwAnaliseSomaPf.getPfTotal().multiply(sumFase).setScale(DECIMAL_PLACE, RoundingMode.HALF_DOWN));

            analise.setPfTotalValor(vwAnaliseSomaPf.getPfGross().setScale(DECIMAL_PLACE).doubleValue());
            analise.setPfTotalAjustadoValor(vwAnaliseSomaPf.getPfTotal().multiply(sumFase).setScale(DECIMAL_PLACE, RoundingMode.HALF_DOWN).doubleValue());
        } else {
            // Alterado: Log de warning para debug - não deveria acontecer após o flush
            log.warn("View vw_analise_soma_pf retornou null para análise ID: {}. PF não será atualizado.", analise.getId());
        }

        if(analise.getAnaliseDivergence() != null
                && analise.getAnaliseDivergence().getStatus().getNome().equals(APROVADA)
                && analise.getAnaliseDivergence().isBloqueiaAnalise()) {
            analise.setPfTotalAprovado(analise.getAnaliseDivergence().getPfTotalAprovado());
        }
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
        analiseDivergencia.setIdentificadorAnalise(analiseDivergencia.getId().toString());
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

    private void verificarFuncoes(FuncaoAnalise funcao, Analise analise, Map<String, Long> mapaFatorAjuste) {
        if (analise.getManual() != null) {
            // Alterado: Adicionada verificação de nulo para evitar NullPointerException
            if (funcao.getFatorAjuste() != null && mapaFatorAjuste != null && mapaFatorAjuste.containsKey(funcao.getFatorAjuste().getNome())) {
                Long idFator = mapaFatorAjuste.get(funcao.getFatorAjuste().getNome());
                analise.getManual().getFatoresAjuste().stream()
                    .filter(f -> f.getId().equals(idFator))
                    .findFirst()
                    .ifPresent(funcao::setFatorAjuste);
            } else if (funcao.getFatorAjuste() != null && !analise.getManual().getFatoresAjuste().contains(funcao.getFatorAjuste())) {
                Set<FatorAjuste> fatoresSemelhantes = new HashSet<>();
                analise.getManual().getFatoresAjuste().forEach(fatorAjuste -> {
                    if (funcao.getFatorAjuste().getNome().equals(fatorAjuste.getNome())) {
                        fatoresSemelhantes.add(fatorAjuste);
                    }
                });

                if (fatoresSemelhantes.size() == 1) {
                    funcao.setFatorAjuste(fatoresSemelhantes.iterator().next());
                } else {
                    funcao.setFatorAjuste(null);
                }
            }
        }
    }


    // Alterado: Criar contadores separados para ordem das funções de dados e transação
    public void salvarFuncoesExcel(Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaoTransacaos, Analise analise, Map<String, Long> mapaFatorAjuste) {
        java.util.concurrent.atomic.AtomicLong ordemDados = new java.util.concurrent.atomic.AtomicLong(1L);
        java.util.concurrent.atomic.AtomicLong ordemTransacao = new java.util.concurrent.atomic.AtomicLong(1L);
        
        salvarFuncaoDadosExcel(funcaoDados, analise, mapaFatorAjuste, ordemDados);
        salvarFuncaoTransacaoExcel(funcaoTransacaos, analise, mapaFatorAjuste, ordemTransacao);
        
        // Alterado: Flush para garantir que os dados das funções sejam persistidos no banco
        // antes de consultar a view vw_analise_soma_pf em atualizarPF
        entityManager.flush();
        
        atualizarPF(analise);
        salvarAnalise(analise);
    }

    @Transactional
    public Analise importarAnaliseExcel(AnaliseEditDTO analiseDTO) {
        User usuario = analiseFacade.obterUsuarioPorLogin();
        Analise analiseOrigem = converterEditDtoParaEntidade(analiseDTO);
        
        // Alterado: VALIDAR fatores de ajuste ANTES de criar nova instância e salvar
        // Validar sobre as funções da analiseOrigem (que contêm dados)
        validarFatoresAjuste(
            analiseOrigem.getManual(),
            analiseOrigem.getFuncaoDados(),
            analiseOrigem.getFuncaoTransacaos(),
            analiseDTO.getMapaFatorAjuste()
        );
        
        // Limpar a sessão para desanexar todas as entidades carregadas pelo converterEditDtoParaEntidade
        // Isso evita que objetos Der/Rlr/Alr gerenciados causem EntityExistsException quando salvamos novos objetos com mesmos IDs (se houver)
        entityManager.clear();
        
        // Recarregar usuário pois foi desanexado
        usuario = analiseFacade.obterUsuarioPorLogin();
        
        // Se chegou aqui, validação passou - prosseguir com salvamento
        Analise analise = new Analise();
        org.springframework.beans.BeanUtils.copyProperties(analiseOrigem, analise, "id", "funcaoDados", "funcaoTransacaos", "users");

        analise.setIdentificadorAnalise(analise.getIdentificadorAnalise() + " Importada");
        analise.setCreatedBy(usuario);
        analise.getUsers().add(usuario);

        // Verificação de segurança para Sistema
    if (analise.getSistema() == null || analise.getSistema().getId() == null) {
        throw new RuntimeException("É necessário selecionar um Sistema para importar a análise.");
    }
    analise.setSistema(sistemaRepository.findOne(analise.getSistema().getId()));
        // Usar as funções da origem (convertida do DTO)
        Set<FuncaoDados> funcaoDados = analiseOrigem.getFuncaoDados();
        Set<FuncaoTransacao> funcaoTransacaos = analiseOrigem.getFuncaoTransacaos();

        analise.setFuncaoTransacaos(new HashSet<>());
        analise.setFuncaoDados(new HashSet<>());
        analise.setDataCriacaoOrdemServico(Timestamp.from(Instant.now()));

        // Alterado: Salvar APENAS no banco durante a transação (não no ElasticSearch)
        analiseFacade.salvarAnaliseApenasDB(analise);
        
        // Salvar funções (também apenas no banco)
        salvarFuncoesExcel(funcaoDados, funcaoTransacaos, analise, analiseDTO.getMapaFatorAjuste());
        
        // Alterado: Salvar no ElasticSearch APENAS após tudo ser persistido com sucesso
        // Se houver qualquer erro acima, a transação faz rollback e o ES não é tocado
        analiseFacade.salvarAnaliseApenasES(analise);
        
        return analise;
    }

    // Alterado: Receber coleções de funções como parâmetros em vez de usar analise.getFuncaoDados()
    private void validarFatoresAjuste(Manual manual, Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaoTransacaos, Map<String, Long> mapaFatorAjuste) {
        if (manual == null) {
            return;
        }
        
        Set<String> fatoresNaoEncontrados = new HashSet<>();
        Set<String> nomesFatoresManual = manual.getFatoresAjuste().stream()
                .map(FatorAjuste::getNome)
                .collect(Collectors.toSet());

        // Alterado: Validar sobre as coleções recebidas como parâmetro
        if (funcaoDados != null) {
            funcaoDados.forEach(fd -> verificarFatorAjuste(fd, nomesFatoresManual, mapaFatorAjuste, fatoresNaoEncontrados));
        }
        if (funcaoTransacaos != null) {
            funcaoTransacaos.forEach(ft -> verificarFatorAjuste(ft, nomesFatoresManual, mapaFatorAjuste, fatoresNaoEncontrados));
        }

        if (!fatoresNaoEncontrados.isEmpty()) {
            throw new FatorAjusteException(new ArrayList<>(fatoresNaoEncontrados));
        }
    }

    private void verificarFatorAjuste(FuncaoAnalise funcao, Set<String> nomesFatoresManual, Map<String, Long> mapaFatorAjuste, Set<String> fatoresNaoEncontrados) {
        if (funcao.getFatorAjuste() != null) {
            String nomeFator = funcao.getFatorAjuste().getNome();
            boolean existeNoManual = nomesFatoresManual.contains(nomeFator);
            boolean existeNoMapa = mapaFatorAjuste != null && mapaFatorAjuste.containsKey(nomeFator);

            if (!existeNoManual && !existeNoMapa) {
                fatoresNaoEncontrados.add(nomeFator);
            }
        }
    }

    // Alterado: Recebe contador de ordem para funções de transação
    private void salvarFuncaoTransacaoExcel(Set<FuncaoTransacao> funcaoTransacaos, Analise analise, Map<String, Long> mapaFatorAjuste, java.util.concurrent.atomic.AtomicLong ordemTransacao) {
        funcaoTransacaos.stream()
            .sorted(Comparator.comparing(FuncaoTransacao::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
            .forEach(funcaoTransacao -> {
                // Alterado: Criar nova instância manualmente, copiando apenas campos primitivos
                FuncaoTransacao novaFuncao = new FuncaoTransacao();
                novaFuncao.setId(null); // Garantir que é transient
                novaFuncao.setName(funcaoTransacao.getName());
                novaFuncao.setSustantation(funcaoTransacao.getSustantation());
                novaFuncao.setImpacto(funcaoTransacao.getImpacto());
                novaFuncao.setTipo(funcaoTransacao.getTipo());
                novaFuncao.setComplexidade(funcaoTransacao.getComplexidade());
                novaFuncao.setPf(funcaoTransacao.getPf());
                novaFuncao.setGrossPF(funcaoTransacao.getGrossPF());
                novaFuncao.setDetStr(funcaoTransacao.getDetStr());
                novaFuncao.setStatusFuncao(funcaoTransacao.getStatusFuncao());
                novaFuncao.setQuantidade(funcaoTransacao.getQuantidade());
                novaFuncao.setFtrStr(funcaoTransacao.getFtrStr());
                
                // Copiar FatorAjuste
                if (funcaoTransacao.getFatorAjuste() != null) {
                    novaFuncao.setFatorAjuste(funcaoTransacao.getFatorAjuste());
                }
                
                if (funcaoTransacao.getFuncionalidade() != null) {
                    novaFuncao.setFuncionalidade(funcaoTransacao.getFuncionalidade());
                }
                
                novaFuncao.setAnalise(analise);
                novaFuncao.setEquipe(null);
                // Alterado: Setar ordem sequencial para funções de transação
                novaFuncao.setOrdem(ordemTransacao.getAndIncrement());
                verificarFuncoes(novaFuncao, analise, mapaFatorAjuste);
                
                // Alterado: Criar novos Ders/Alrs ANTES de salvar a função
                Set<Der> novosDers = new HashSet<>();
                if (funcaoTransacao.getDers() != null) {
                    funcaoTransacao.getDers().forEach(derOriginal -> {
                        Der novoDer = new Der();
                        novoDer.setNome(derOriginal.getNome());
                        novoDer.setValor(derOriginal.getValor());
                        novoDer.setFuncaoTransacao(novaFuncao);
                        novosDers.add(novoDer);
                    });
                }
                novaFuncao.setDers(novosDers);
                
                Set<Alr> novosAlrs = new HashSet<>();
                if (funcaoTransacao.getAlrs() != null) {
                    funcaoTransacao.getAlrs().forEach(alrOriginal -> {
                        Alr novoAlr = new Alr();
                        novoAlr.setNome(alrOriginal.getNome());
                        novoAlr.setValor(alrOriginal.getValor());
                        novoAlr.setFuncaoTransacao(novaFuncao);
                        novosAlrs.add(novoAlr);
                    });
                }
                novaFuncao.setAlrs(novosAlrs);

                setarFuncionalidadeFuncao(novaFuncao, analise);
                analiseFacade.salvarFuncaoTransacao(novaFuncao);
            });
    }

    // Alterado: Recebe contador de ordem para funções de dados
    private void salvarFuncaoDadosExcel(Set<FuncaoDados> funcaoDados, Analise analise, Map<String, Long> mapaFatorAjuste, java.util.concurrent.atomic.AtomicLong ordemDados) {
        funcaoDados.stream()
            .sorted(Comparator.comparing(FuncaoDados::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
            .forEach(funcaoDado -> {
                // Alterado: Criar nova instância manualmente, copiando apenas campos primitivos
                FuncaoDados novaFuncao = new FuncaoDados();
                novaFuncao.setId(null); // Garantir que é transient
                novaFuncao.setName(funcaoDado.getName());
                novaFuncao.setSustantation(funcaoDado.getSustantation());
                novaFuncao.setImpacto(funcaoDado.getImpacto());
                novaFuncao.setTipo(funcaoDado.getTipo());
                novaFuncao.setComplexidade(funcaoDado.getComplexidade());
                novaFuncao.setPf(funcaoDado.getPf());
                novaFuncao.setGrossPF(funcaoDado.getGrossPF());
                novaFuncao.setDetStr(funcaoDado.getDetStr());
                novaFuncao.setStatusFuncao(funcaoDado.getStatusFuncao());
                novaFuncao.setQuantidade(funcaoDado.getQuantidade());
                novaFuncao.setRetStr(funcaoDado.getRetStr());
                
                // Copiar FatorAjuste
                if (funcaoDado.getFatorAjuste() != null) {
                    novaFuncao.setFatorAjuste(funcaoDado.getFatorAjuste());
                }

                if (funcaoDado.getFuncionalidade() != null) {
                    novaFuncao.setFuncionalidade(funcaoDado.getFuncionalidade());
                }

                novaFuncao.setAnalise(analise);
                // Alterado: Setar ordem sequencial para funções de dados
                novaFuncao.setOrdem(ordemDados.getAndIncrement());
                verificarFuncoes(novaFuncao, analise, mapaFatorAjuste);
                
                // Alterado: Criar novos Ders/Rlrs ANTES de salvar a função
                Set<Der> novosDers = new HashSet<>();
                if (funcaoDado.getDers() != null) {
                    funcaoDado.getDers().forEach(derOriginal -> {
                        Der novoDer = new Der();
                        novoDer.setNome(derOriginal.getNome());
                        novoDer.setValor(derOriginal.getValor());
                        novoDer.setFuncaoDados(novaFuncao);
                        novosDers.add(novoDer);
                    });
                }
                novaFuncao.setDers(novosDers);
                
                Set<Rlr> novosRlrs = new HashSet<>();
                if (funcaoDado.getRlrs() != null) {
                    funcaoDado.getRlrs().forEach(rlrOriginal -> {
                        Rlr novoRlr = new Rlr();
                        novoRlr.setNome(rlrOriginal.getNome());
                        novoRlr.setValor(rlrOriginal.getValor());
                        novoRlr.setFuncaoDados(novaFuncao);
                        novosRlrs.add(novoRlr);
                    });
                }
                novaFuncao.setRlrs(novosRlrs);

                setarFuncionalidadeFuncao(novaFuncao, analise);
                analiseFacade.salvarFuncaoDado(novaFuncao);
            });
    }

    // Alterado:  Refatorado para buscar/criar Módulo e Funcionalidade com correspondência exata
    private void setarFuncionalidadeFuncao(FuncaoAnalise funcao, Analise analise) {
        // Alterado: Se o sistema for null (durante uploadExcel), apenas retornar.
    // Os dados transientes (nomes) já foram setados em setarModuloFuncionalidade.
    if (analise.getSistema() == null) {
        log.debug("Sistema não setado na análise (fase de upload). Pulando busca de funcionalidade no banco.");
        return;
    }

    if (funcao.getFuncionalidade() == null) {
        throw new RuntimeException("Funcionalidade inválida (null) para a função.");
    }
    
    // Obter nomes do Módulo e Funcionalidade que vieram do Excel
    String nomeModulo = funcao.getFuncionalidade().getModulo() != null 
        ? funcao.getFuncionalidade().getModulo().getNome() 
        : null;
    String nomeFuncionalidade = funcao.getFuncionalidade().getNome();
    
    log.info("Processando Módulo/Funcionalidade: Módulo='{}', Funcionalidade='{}'", nomeModulo, nomeFuncionalidade);
    
    if (nomeModulo == null || nomeFuncionalidade == null) {
        throw new RuntimeException("Nome do Módulo ou Funcionalidade não encontrado na importação.");
    }
        
        // 1. Buscar ou criar Módulo (com correspondência exata)
        Modulo modulo = buscarOuCriarModulo(nomeModulo, analise.getSistema());
        
        if (modulo == null) {
            log.error("Falha ao buscar/criar Módulo: {}", nomeModulo);
            return;
        }
        log.info("Módulo persistido/encontrado: ID={}, Nome={}", modulo.getId(), modulo.getNome());
        
        // 2. Buscar ou criar Funcionalidade (com correspondência exata) vinculada ao Módulo
        Funcionalidade funcionalidade = buscarOuCriarFuncionalidade(nomeFuncionalidade, modulo);
        
        if (funcionalidade == null) {
            log.error("Falha ao buscar/criar Funcionalidade: {} no Módulo: {}", nomeFuncionalidade, nomeModulo);
            return;
        }
        log.info("Funcionalidade persistida/encontrada: ID={}, Nome={}", funcionalidade.getId(), funcionalidade.getNome());
        
        // 3. Atribuir a Funcionalidade já persistida (com ID) à Função
        funcao.setFuncionalidade(funcionalidade);
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
            long idAnalise = lstCompartilhadas.stream().findFirst().orElse(new Compartilhada()).getAnaliseId();
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
            abacoMensagens.adicionarNovoSucesso(String.format("Análise %s compartilhou para a(s) equipe(s) %s", analise.getIdentificadorAnalise(), String.join(", ", nomeDasEquipes)));
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
            if (analise.getStatus().getNome().equals(APROVADA)) {
                analise.getAnalisesComparadas().forEach(analiseComparada -> {
                    analiseComparada.setPfTotalAprovado(analise.getPfTotalAprovado());
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

    public Page<AnaliseDTO> obterTodasAnalisesEquipes(AnalisePesquisaDTO pesquisaDTO) {
        log.debug("DEBUG Consulta Analises - Inicio metodo");
        SortOrder sortOrderQb;
        if (pesquisaDTO.getOrder().equals("asc")) {
            sortOrderQb = SortOrder.ASC;
        } else {
            sortOrderQb = SortOrder.DESC;
        }
        Sort.Direction sortOrder = PageUtils.getSortDirection(pesquisaDTO.getOrder());
        Pageable pageable = new PageRequest(pesquisaDTO.getPageNumber(), pesquisaDTO.getSize(), sortOrder, pesquisaDTO.getSort());
        FieldSortBuilder sortBuilder = new FieldSortBuilder(pesquisaDTO.getSort()).order(sortOrderQb);
        BoolQueryBuilder qb = analiseFacade.obterBoolQueryBuilder(pesquisaDTO);
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

    public Page<AnaliseDTO> obterDivergencias(AnalisePesquisaDTO pesquisaDTO) {
        log.debug("DEBUG Consulta Validação -  Inicio método");
        Sort.Direction sortOrder = PageUtils.getSortDirection(pesquisaDTO.getOrder());
        Pageable pageable = new PageRequest(pesquisaDTO.getPageNumber(), pesquisaDTO.getSize(), sortOrder, pesquisaDTO.getSort());
        FieldSortBuilder sortBuilder = new FieldSortBuilder(pesquisaDTO.getSort()).order(SortOrder.DESC);
        BoolQueryBuilder qb = analiseFacade.obterBoolQueryBuilderDivergencia(pesquisaDTO.getIdentificador(), pesquisaDTO.getSistema(), pesquisaDTO.getOrganizacao(), pesquisaDTO.getStatus(), pesquisaDTO.getBloqueado());
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
        Path tempDir = Files.createTempDirectory("");
        File tempFile = tempDir.resolve(file.getOriginalFilename()).toFile();
        file.transferTo(tempFile);

        try(
            FileInputStream inputStream = new FileInputStream(tempFile);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream)
        ) {

            Analise analise = new Analise();
            Set<FuncaoDados> funcaoDados = new HashSet<>();
            Set<FuncaoTransacao> funcaoTransacaos = new HashSet<>();
            
            // Alterado: Contadores separados para ordem das funções de dados e transação
            // A numeração de função de dados é independente da numeração de função de transação
            java.util.concurrent.atomic.AtomicLong ordemDados = new java.util.concurrent.atomic.AtomicLong(1L);
            java.util.concurrent.atomic.AtomicLong ordemTransacao = new java.util.concurrent.atomic.AtomicLong(1L);

            setarResumoExcelUpload(workbook, analise);
            if (analise.getMetodoContagem().equals(MetodoContagem.INDICATIVA)) {
                setarIndicativaExcelUpload(workbook, funcaoDados, analise, ordemDados);
            } else {
                // INM usa o contador de transação, pois as funções INM continuam a numeração das funções de transação
                setarInmExcelUpload(workbook, funcaoTransacaos, funcaoDados, analise, ordemTransacao);
            }
            if (analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)) {
                setarExcelDetalhadaUpload(workbook, funcaoDados, funcaoTransacaos, analise, ordemDados, ordemTransacao);
            } else if (analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)) {
                setarExcelEstimadaUpload(workbook, funcaoDados, funcaoTransacaos, analise, ordemDados, ordemTransacao);
            }
            analise.setFuncaoDados(funcaoDados);
            analise.setFuncaoTransacaos(funcaoTransacaos);
            return analise;

        }
    }

    // Planilha Detalhada
    // Alterado: Recebe contadores separados para funções de dados e transação
    public void setarExcelDetalhadaUpload(XSSFWorkbook excelFile, Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaoTransacaos, Analise analise, java.util.concurrent.atomic.AtomicLong ordemDados, java.util.concurrent.atomic.AtomicLong ordemTransacao) {
        XSSFSheet excelSheet = excelFile.getSheet(DETALHADA);
        for (int i = 9; i < 1323; i++) {
            XSSFRow row = excelSheet.getRow(i);
            if (row != null && getCellValueAsNumber(row, 0) > 0) {
                // Alterado: Usar helper para ler tipo (Coluna G = índice 6)
                String tipo = getCellValueAsString(row, 6);
                if (tipoFuncaoDados().contains(tipo)) {
                    funcaoDados.add(setarFuncaoDadosDetalhada(row, analise, ordemDados));
                } else if (tipoFuncaoTransacao().contains(tipo)) {
                    funcaoTransacaos.add(setarFuncaoTrasacaoDetalhada(row, analise, ordemTransacao));
                }
            }
        }
    }


    private FuncaoDados setarFuncaoDadosDetalhada(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        FuncaoDados funcaoDados = new FuncaoDados();
        funcaoDados.setId((long) getCellValueAsNumber(row, 0));
        setarModuloFuncionalidade(funcaoDados, row);
        
        // Alterado: Persistir/Vincular Funcionalidade e Módulo
        setarFuncionalidadeFuncao(funcaoDados, analise);
        
        setarTipoFuncaoDados(row, funcaoDados);
        setarDerRlrFuncaoDados(funcaoDados, row);
        setarSustentacaoStatus(funcaoDados, row);
        funcaoDados.setOrdem(ordem.getAndIncrement());
        return funcaoDados;
    }

    private void setarDerRlrFuncaoDados(FuncaoDados funcaoDados, XSSFRow row) {
        Set<Der> ders = new HashSet<>();
        Set<Alr> alrs = new HashSet<>();
        setarDerAlr(row, ders, alrs);
        
        funcaoDados.setDers(clonarDers(ders));
        
        // Converter ALRs para RLRs (pois compartilham a mesma coluna no Excel)
        Set<Rlr> rlrs = new HashSet<>();
        alrs.forEach(alr -> {
            Rlr rlr = new Rlr();
            rlr.setNome(alr.getNome());
            rlrs.add(rlr);
        });
        funcaoDados.setRlrs(clonarRlrs(rlrs));
        
        setarFuncaoComplexidade(funcaoDados, row);
    }

    // Alterado: Usar helper para ler células (trata fórmulas e textos)
    private void setarDerAlr(XSSFRow row, Set<Der> ders, Set<Alr> alrs) {
        // Coluna I (índice 8) - TD/DER Descrição
        String dersValue = getCellValueAsString(row, 8);
        if (dersValue != null && !dersValue.trim().isEmpty()) {
            Arrays.stream(dersValue.split(", ")).forEach(value -> {
                if (value != null && !value.trim().isEmpty()) {
                    Der der = new Der();
                    der.setNome(value.trim());
                    ders.add(der);
                }
            });
        }

        // Coluna K (índice 10) - RLR/ALR Descrição
        String alrsValue = getCellValueAsString(row, 10);
        if (alrsValue != null && !alrsValue.trim().isEmpty()) {
            Arrays.stream(alrsValue.split(", ")).forEach(value -> {
                if (value != null && !value.trim().isEmpty()) {
                    Alr alr = new Alr();
                    alr.setNome(value.trim());
                    alrs.add(alr);
                }
            });
        }
    }

    // Alterado: Usar helper para ler células (trata fórmulas e textos)
    private void setarSustentacaoStatus(FuncaoAnalise funcao, XSSFRow row) {
        // Coluna R (índice 17) - Observação/Parecer Técnico (Sustentação)
        // Antes estava 14 (O), mas O é uma coluna de fórmula de cálculo
        funcao.setSustantation(getCellValueAsString(row, 17));
        
        // Coluna S (índice 18) - Status
        String status = getCellValueAsString(row, 18);
        if (statusFuncao().contains(status)) {
            switch (status) {
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

    // Alterado: Usar helper para ler células (trata fórmulas e textos)
    private void setarModuloFuncionalidade(FuncaoAnalise funcao, XSSFRow row) {
        Modulo modulo = new Modulo();
        modulo.setNome(getCellValueAsString(row, 3)); // Coluna D (Módulo)

        Funcionalidade funcionalidade = new Funcionalidade();
        funcionalidade.setModulo(modulo);
        funcionalidade.setNome(getCellValueAsString(row, 4)); // Coluna E (Requisito/Funcionalidade)

        FatorAjuste fatorAjuste = new FatorAjuste();
        fatorAjuste.setNome(getCellValueAsString(row, 1)); // Coluna B (Fator Ajuste)

        funcao.setFuncionalidade(funcionalidade);
        funcao.setName(getCellValueAsString(row, 5)); // Coluna F (Processos elementares)
        funcao.setFatorAjuste(fatorAjuste);
    }

    private FuncaoTransacao setarFuncaoTrasacaoDetalhada(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
        funcaoTransacao.setId((long) getCellValueAsNumber(row, 0));
        setarModuloFuncionalidade(funcaoTransacao, row);
        
        // Alterado: Persistir/Vincular Funcionalidade e Módulo
        setarFuncionalidadeFuncao(funcaoTransacao, analise);
        
        // Alterado: Usar helper para ler tipo (Coluna G = índice 6)
        setarTipoFuncaoTransacao(funcaoTransacao, getCellValueAsString(row, 6));
        
        setarDerAlrFuncaoTransacao(funcaoTransacao, row);
        setarSustentacaoStatus(funcaoTransacao, row);
        funcaoTransacao.setOrdem(ordem.getAndIncrement());
        return funcaoTransacao;
    }

    private void setarDerAlrFuncaoTransacao(FuncaoTransacao funcaoTransacao, XSSFRow row) {
        Set<Der> ders = new HashSet<>();
        Set<Alr> alrs = new HashSet<>();
        setarDerAlr(row, ders, alrs);
        
        funcaoTransacao.setDers(clonarDers(ders));
        funcaoTransacao.setAlrs(clonarAlrs(alrs));
        setarFuncaoComplexidade(funcaoTransacao, row);
    }


    // Planilha Estimada
    // Alterado: Recebe contadores separados para funções de dados e transação
    public void setarExcelEstimadaUpload(XSSFWorkbook excelFile, Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaoTransacaos, Analise analise, java.util.concurrent.atomic.AtomicLong ordemDados, java.util.concurrent.atomic.AtomicLong ordemTransacao) {
        XSSFSheet excelSheetEstimada = excelFile.getSheet(ESTIMATIVA);
        for (int i = 10; i < 1081; i++) {
            XSSFRow row = excelSheetEstimada.getRow(i);
            if (row != null && getCellValueAsNumber(row, 0) > 0) {
                String tipo = getCellValueAsString(row, 6); // Coluna G
                if (tipoFuncaoDados().contains(tipo)) {
                    funcaoDados.add(setarFuncaoDadosEstimada(row, analise, ordemDados));
                } else if (tipoFuncaoTransacao().contains(tipo)) {
                    funcaoTransacaos.add(setarFuncaoTransacaoEstimada(row, analise, ordemTransacao));
                }
            }
        }
    }

    private void setarModuloFuncionalidadeEstimada(FuncaoAnalise funcao, XSSFRow row) {
        Funcionalidade funcionalidade = new Funcionalidade();
        Modulo modulo = new Modulo();
        FatorAjuste fatorAjuste = new FatorAjuste();
        funcao.setId((long) getCellValueAsNumber(row, 0));
        modulo.setNome(getCellValueAsString(row, 3)); // Coluna D
        funcionalidade.setModulo(modulo);
        funcionalidade.setNome(getCellValueAsString(row, 4)); // Coluna E
        funcao.setFuncionalidade(funcionalidade);
        funcao.setName(getCellValueAsString(row, 5)); // Coluna F
        fatorAjuste.setNome(getCellValueAsString(row, 1)); // Coluna B
        funcao.setFatorAjuste(fatorAjuste);
    }

    private FuncaoDados setarFuncaoDadosEstimada(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        FuncaoDados funcaoDados = new FuncaoDados();
        setarModuloFuncionalidadeEstimada(funcaoDados, row);
        
        // Alterado: Persistir/Vincular Funcionalidade e Módulo
        setarFuncionalidadeFuncao(funcaoDados, analise);
        
        String tipo = getCellValueAsString(row, 6); // Coluna G
        switch (tipo) {
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
        
        // Alterado: Usar helper para ler Complexidade e PF (Colunas N e Q)
        setarFuncaoComplexidade(funcaoDados, row);
        
        funcaoDados.setSustantation(getCellValueAsString(row, 17)); // Coluna R
        funcaoDados.setOrdem(ordem.getAndIncrement());
        return funcaoDados;
    }

    private FuncaoTransacao setarFuncaoTransacaoEstimada(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
        setarModuloFuncionalidadeEstimada(funcaoTransacao, row);
        
        // Alterado: Persistir/Vincular Funcionalidade e Módulo
        setarFuncionalidadeFuncao(funcaoTransacao, analise);
        
        setarTipoFuncaoTransacao(funcaoTransacao, getCellValueAsString(row, 6)); // Coluna G
        
        // Alterado: Usar helper para ler Complexidade e PF (Colunas N e Q)
        setarFuncaoComplexidade(funcaoTransacao, row);
        
        funcaoTransacao.setSustantation(getCellValueAsString(row, 17)); // Coluna R
        funcaoTransacao.setOrdem(ordem.getAndIncrement());
        return funcaoTransacao;
    }

    private void setarTipoFuncaoTransacao(FuncaoTransacao funcaoTransacao,String tipo) {
        switch (tipo) {
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
    }

    // Planilha Resumo
    public void setarResumoExcelUpload(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet sheet = excelFile.getSheet(RESUMO);
        analise.setNumeroOs(sheet.getRow(3).getCell(1).getStringCellValue());
        analise.setPropositoContagem(sheet.getRow(9).getCell(0).getStringCellValue());
        analise.setEscopo(sheet.getRow(12).getCell(0).getStringCellValue());
        
            // Alterado: Removida a lógica de buscar o Sistema pela planilha.
        // O sistema deve ser selecionado pelo usuário na tela de importação.
        /*
        try {
            String nomeSistema = getCellValueAsString(sheet.getRow(4), 5);
            log.info("Tentando ler Sistema da planilha. Valor encontrado na célula (4,5): '{}'", nomeSistema);
            
            if (nomeSistema != null && !nomeSistema.trim().isEmpty()) {
                Optional<Sistema> sistemaOpt = sistemaRepository.findByNomeIgnoreCase(nomeSistema.trim());
                if (sistemaOpt.isPresent()) {
                    analise.setSistema(sistemaOpt.get());
                    log.info("Sistema encontrado e vinculado à análise: {}", sistemaOpt.get().getNome());
                } else {
                    log.warn("Sistema não encontrado pelo nome na planilha: {}", nomeSistema);
                }
            } else {
                log.warn("Nome do sistema está vazio ou nulo na célula (4,5).");
            }
        } catch (Exception e) {
            log.warn("Erro ao ler Sistema da planilha: {}", e.getMessage());
        }
        */
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
    public void setarIndicativaExcelUpload(XSSFWorkbook excelFile, Set<FuncaoDados> funcaoDadosList, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        XSSFSheet excelSheetIndicativa = excelFile.getSheet(SHEET_INM_INDICATIVA);
        for (int i = 9; i < 107; i++) {
            XSSFRow row = excelSheetIndicativa.getRow(i);
            if (row != null && getCellValueAsNumber(row, 0) > 0 && (tipoFuncaoDados().contains(getCellValueAsString(row, 6)))) {
                funcaoDadosList.add(setarFuncaoDadosIndicativa(row, analise, ordem));
            }
        }
    }

    private FuncaoDados setarFuncaoDadosIndicativa(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        FuncaoDados funcaoDados = new FuncaoDados();
        Funcionalidade funcionalidade = new Funcionalidade();
        Modulo modulo = new Modulo();
        funcaoDados.setId((long) getCellValueAsNumber(row, 0));
        modulo.setNome(getCellValueAsString(row, 3)); // Coluna D
        funcionalidade.setModulo(modulo);
        funcionalidade.setNome(getCellValueAsString(row, 4)); // Coluna E
        funcaoDados.setFuncionalidade(funcionalidade);
        funcaoDados.setName(getCellValueAsString(row, 5)); // Coluna F
        
        // Alterado: Persistir/Vincular Funcionalidade e Módulo
        setarFuncionalidadeFuncao(funcaoDados, analise);
        
        setarTipoFuncaoDados(row, funcaoDados);
        
        // Alterado: Usar helper para ler Complexidade e PF (Colunas N e Q)
        setarFuncaoComplexidade(funcaoDados, row);
        
        funcaoDados.setSustantation(getCellValueAsString(row, 17)); // Coluna R
        funcaoDados.setOrdem(ordem.getAndIncrement());
        return funcaoDados;
    }

    // Alterado: Usar helper para ler tipo
    private void setarTipoFuncaoDados(XSSFRow row, FuncaoDados funcaoDados) {
        String tipo = getCellValueAsString(row, 6); // Coluna G
        switch (tipo) {
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
    public void setarInmExcelUpload(XSSFWorkbook excelFile, Set<FuncaoTransacao> funcaoTransacaos, Set<FuncaoDados> funcaoDados, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        XSSFSheet excelSheetINM = excelFile.getSheet(SHEET_INM);
        for (int i = 10; i < 382; i++) {
            XSSFRow row = excelSheetINM.getRow(i);
            if (row != null && getCellValueAsNumber(row, 0) > 0) {
                String tipo = getCellValueAsString(row, 6); // Coluna G
                if (tipoFuncaoDados().contains(tipo)) {
                    funcaoTransacaos.add(setarInm(row, analise, ordem));
                } else if (tipoFuncaoTransacao().contains(tipo)) {
                    funcaoDados.add(setarFuncaoDadosInm(row, analise, ordem));
                }
            }
        }
    }

    private FuncaoTransacao setarInm(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
        String tipo = getCellValueAsString(row, 6); // Coluna G
        if (TipoFuncaoTransacao.INM.toString().equals(tipo)) {
            setarModuloFuncionalidadeInm(funcaoTransacao, row);
            
            // Alterado: Persistir/Vincular Funcionalidade e Módulo
            setarFuncionalidadeFuncao(funcaoTransacao, analise);
            
            if (METODO_CE.equals(tipo)) {
                funcaoTransacao.setTipo(TipoFuncaoTransacao.INM);
            }
            setarDerAlrFuncaoTransacao(funcaoTransacao, row);
            funcaoTransacao.setSustantation(getCellValueAsString(row, 17)); // Coluna R
            
            String status = getCellValueAsString(row, 18); // Coluna S
            if (statusFuncao().contains(status)) {
                switch (status) {
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
            funcaoTransacao.setOrdem(ordem.getAndIncrement());
        }
        return funcaoTransacao;
    }

    private FuncaoDados setarFuncaoDadosInm(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        FuncaoDados funcaoDados = new FuncaoDados();
        String tipo = getCellValueAsString(row, 6); // Coluna G
        if (TipoFuncaoTransacao.INM.toString().equals(tipo)) {
            setarModuloFuncionalidadeInm(funcaoDados, row);
            
            // Alterado: Persistir/Vincular Funcionalidade e Módulo
            setarFuncionalidadeFuncao(funcaoDados, analise);
            
            if (tipo.equals(METODO_CE)) {
                funcaoDados.setTipo(TipoFuncaoDados.INM);
            }
            setarDerRlrFuncaoDados(funcaoDados, row);
            funcaoDados.setOrdem(ordem.getAndIncrement());
        }
        return funcaoDados;
    }

    private void setarModuloFuncionalidadeInm(FuncaoAnalise funcao, XSSFRow row) {
        Modulo modulo = new Modulo();
        FatorAjuste fatorAjuste = new FatorAjuste();
        Funcionalidade funcionalidade = new Funcionalidade();
        funcao.setId((long) getCellValueAsNumber(row, 0));
        modulo.setNome(getCellValueAsString(row, 3)); // Coluna D
        funcionalidade.setModulo(modulo);
        funcionalidade.setNome(getCellValueAsString(row, 4)); // Coluna E
        funcao.setFuncionalidade(funcionalidade);
        funcao.setName(getCellValueAsString(row, 5)); // Coluna F
        fatorAjuste.setNome(getCellValueAsString(row, 1)); // Coluna B
        funcao.setFatorAjuste(fatorAjuste);
    }

    // Alterado: Usar helpers para ler células (trata fórmulas e textos)
    private void setarFuncaoComplexidade(FuncaoAnalise funcao, XSSFRow row) {
        String complexidade = getCellValueAsString(row, 13); // Coluna N (Complexidade)
        
        log.info("=== LENDO COMPLEXIDADE: Linha={}, Valor bruto='{}'", row.getRowNum(), complexidade);
        
        if (complexidade != null) {
            complexidade = complexidade.trim();
            
            log.info("=== COMPLEXIDADE APÓS TRIM: '{}'", complexidade);
            
            // Alterado: Converter texto da planilha para enum
            // Simples -> BAIXA, Médio -> MEDIA, Complexo -> ALTA
            if ("Simples".equalsIgnoreCase(complexidade)) {
                funcao.setComplexidade(Complexidade.BAIXA);
                log.info("=== COMPLEXIDADE DEFINIDA: BAIXA");
            } else if ("Médio".equalsIgnoreCase(complexidade)) {
                funcao.setComplexidade(Complexidade.MEDIA);
                log.info("=== COMPLEXIDADE DEFINIDA: MEDIA");
            } else if ("Complexo".equalsIgnoreCase(complexidade)) {
                funcao.setComplexidade(Complexidade.ALTA);
                log.info("=== COMPLEXIDADE DEFINIDA: ALTA");
            } else {
                // Se não for nenhum dos valores esperados, deixa null
                log.warn("=== COMPLEXIDADE NÃO RECONHECIDA: '{}' (length={}). Valores esperados: Simples, Médio, Complexo", 
                    complexidade, complexidade.length());
            }
        } else {
            log.warn("=== COMPLEXIDADE é NULL para linha {}", row.getRowNum());
        }
        
        // Alterado: Usar helper para ler PF (pode ser fórmula)
        double pf = getCellValueAsNumber(row, 16); // Coluna Q (PF)
        funcao.setPf(BigDecimal.valueOf(pf));
        funcao.setGrossPF(BigDecimal.valueOf(pf));
    }


    public void salvarAnalise(Analise analise) {
        analiseFacade.salvarAnalise(analise);
    }

    public Compartilhada salvarCompartilhada(CompartilhadaDTO compartilhadaDTO) {
        return converterCompartilhadaParaEntidade(compartilhadaDTO);
    }

    // Alterado: Métodos auxiliares para clonar objetos Der/Rlr/Alr e evitar compartilhamento
    private Set<Der> clonarDers(Set<Der> originais) {
        Set<Der> clones = new HashSet<>();
        if (originais != null) {
            originais.forEach(derOriginal -> {
                Der derClone = new Der();
                derClone.setNome(derOriginal.getNome());
                derClone.setValor(derOriginal.getValor());
                clones.add(derClone);
            });
        }
        return clones;
    }

    private Set<Rlr> clonarRlrs(Set<Rlr> originais) {
        Set<Rlr> clones = new HashSet<>();
        if (originais != null) {
            originais.forEach(rlrOriginal -> {
                Rlr rlrClone = new Rlr();
                rlrClone.setNome(rlrOriginal.getNome());
                rlrClone.setValor(rlrOriginal.getValor());
                clones.add(rlrClone);
            });
        }
        return clones;
    }

    private Set<Alr> clonarAlrs(Set<Alr> originais) {
        Set<Alr> clones = new HashSet<>();
        if (originais != null) {
            originais.forEach(alrOriginal -> {
                Alr alrClone = new Alr();
                alrClone.setNome(alrOriginal.getNome());
                alrClone.setValor(alrOriginal.getValor());
                clones.add(alrClone);
            });
        }
        return clones;
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
