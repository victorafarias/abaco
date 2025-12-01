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
import java.util.concurrent.atomic.AtomicLong;
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

    public Set<FuncaoTransacao> bindCloneFuncaoTransacao(Analise analise, Analise analiseClone) {
        Set<FuncaoTransacao> funcaoTransacoes = new HashSet<>();
        analise.getFuncaoTransacao().forEach(ft -> {
            FuncaoTransacao funcaoTransacao = bindFuncaoTransacao(analiseClone, ft);
            funcaoTransacoes.add(funcaoTransacao);
        });
        return funcaoTransacoes;
    }

    public Set<FuncaoTransacao> bindDivergenceFuncaoTransacao(Analise analise, Analise analiseClone) {
        Set<FuncaoTransacao> funcaoTransacoes = new LinkedHashSet<>();
        analise.getFuncaoTransacao().forEach(ft -> {
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

        // ATUALIZADO: Adicionado RoundingMode.HALF_UP
        BigDecimal sumFase = new BigDecimal(BigInteger.ZERO).setScale(DECIMAL_PLACE, RoundingMode.HALF_UP);

        if (analise.getEsforcoFases() != null && (!analise.getEsforcoFases().isEmpty())) {
            for (EsforcoFase esforcoFase : analise.getEsforcoFases()) {
                // ATUALIZADO: Adicionado RoundingMode.HALF_UP
                sumFase = sumFase.add(esforcoFase.getEsforco().setScale(DECIMAL_PLACE, RoundingMode.HALF_UP));
            }
        }

        // ATUALIZADO: Adicionado RoundingMode.HALF_UP no divide() e no setScale()
        sumFase = sumFase.divide(percent, DECIMAL_PLACE, RoundingMode.HALF_UP);

        // Alterado: Adicionar null check para evitar NullPointerException quando a view não retorna dados
        if (vwAnaliseSomaPf != null) {
            // ATUALIZADO: Adicionado RoundingMode.HALF_UP
            analise.setPfTotal(vwAnaliseSomaPf.getPfGross().setScale(DECIMAL_PLACE, RoundingMode.HALF_UP));

            // JÁ CORRETO: Já tem RoundingMode.HALF_DOWN
            analise.setAdjustPFTotal(vwAnaliseSomaPf.getPfTotal().multiply(sumFase).setScale(DECIMAL_PLACE, RoundingMode.HALF_DOWN));

            // ATUALIZADO: Adicionado RoundingMode.HALF_UP
            analise.setPfTotalValor(vwAnaliseSomaPf.getPfGross().setScale(DECIMAL_PLACE, RoundingMode.HALF_UP).doubleValue());

            // JÁ CORRETO: Já tem RoundingMode.HALF_DOWN
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
            analiseClone.setFuncaoTransacao(bindCloneFuncaoTransacao(analise, analiseClone));
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
        analise.getFuncaoTransacao().forEach(funcao -> funcao.setEquipe(analise.getEquipeResponsavel()));
        analiseClone.setFuncaoDados(bindDivergenceFuncaoDados(analise, analiseClone));
        analiseClone.setFuncaoTransacao(bindDivergenceFuncaoTransacao(analise, analiseClone));
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
        Set<FuncaoTransacao> lstfuncaotransacao = new LinkedHashSet<>();
        Set<FuncaoDados> lstOrganizadaFuncaoDados = new LinkedHashSet<>();
        Set<FuncaoTransacao> lstOrganizadaFuncaoTransacao = new LinkedHashSet<>();

        analisePrincipal.getFuncaoDados().forEach(funcao -> funcao.setEquipe(analisePrincipal.getEquipeResponsavel()));
        analisePrincipal.getFuncaoTransacao().forEach(funcao -> funcao.setEquipe(analisePrincipal.getEquipeResponsavel()));
        analiseSecundaria.getFuncaoDados().forEach(funcao -> funcao.setEquipe(analiseSecundaria.getEquipeResponsavel()));
        analiseSecundaria.getFuncaoTransacao().forEach(funcao -> funcao.setEquipe(analiseSecundaria.getEquipeResponsavel()));

        lstFuncaoDados.addAll(analisePrincipal.getFuncaoDados());
        lstFuncaoDados.addAll(analiseSecundaria.getFuncaoDados());
        lstfuncaotransacao.addAll(analisePrincipal.getFuncaoTransacao());
        lstfuncaotransacao.addAll(analiseSecundaria.getFuncaoTransacao());

        carregarFuncoes(lstFuncaoDados, lstfuncaotransacao, lstOrganizadaFuncaoDados, lstOrganizadaFuncaoTransacao);
        analisePrincipal.setFuncaoDados(lstOrganizadaFuncaoDados);
        analisePrincipal.setFuncaoTransacao(lstOrganizadaFuncaoTransacao);
        analiseDivergenciaPrincipal.setFuncaoDados(bindDivergenceFuncaoDados(analisePrincipal, analiseDivergenciaPrincipal));
        analiseDivergenciaPrincipal.setFuncaoTransacao(bindDivergenceFuncaoTransacao(analisePrincipal, analiseDivergenciaPrincipal));
    }

    private void carregarFuncoes
        (Set<FuncaoDados> lstFuncaoDados, Set<FuncaoTransacao> lstfuncaotransacao, Set<FuncaoDados> lstOrganizadaFuncaoDados, Set<FuncaoTransacao> lstOrganizadaFuncaoTransacao) {
        carregarFuncoesDados(lstFuncaoDados, lstOrganizadaFuncaoDados);
        carregarFuncoesTransacao(lstfuncaotransacao, lstOrganizadaFuncaoTransacao);
    }

    private void carregarFuncoesTransacao
        (Set<FuncaoTransacao> lstfuncaotransacao, Set<FuncaoTransacao> lstOrganizadaFuncaoTransacao) {
        int ordem = 1;
        for (FuncaoTransacao funcao : lstfuncaotransacao) {
            if (funcao.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO)) {
                funcao.setOrdem((long) ordem++);
                lstOrganizadaFuncaoTransacao.add(funcao);
                for (FuncaoTransacao funcaoSecundaria : lstfuncaotransacao) {
                    if (!funcaoSecundaria.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO) && isFuncaoEquiparada(funcao, funcaoSecundaria)) {
                        funcaoSecundaria.setOrdem((long) ordem++);
                        lstOrganizadaFuncaoTransacao.add(funcaoSecundaria);
                    }
                }
            }
        }

        for (FuncaoTransacao funcao : lstfuncaotransacao) {
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
    
    // Alterado: Método que retorna DTO com módulos serializados
    public br.com.basis.abaco.service.dto.upload.AnaliseUploadDTO uploadExcelComDTO(MultipartFile file) throws IOException {
        Analise analise = uploadExcel(file);
        return converterParaUploadDTO(analise);
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
        analiseNova.setFuncaoTransacao(analise.getFuncaoTransacao());
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
    public void salvarFuncoesExcel(Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaotransacao, Analise analise, Map<String, Long> mapaFatorAjuste) {
        java.util.concurrent.atomic.AtomicLong ordemDados = new java.util.concurrent.atomic.AtomicLong(1L);
        java.util.concurrent.atomic.AtomicLong ordemTransacao = new java.util.concurrent.atomic.AtomicLong(1L);
        
        // Alterado: Validação de integridade - verificar se há funções INM com ordem null
        long ftInmComOrdemNull = funcaotransacao.stream()
            .filter(ft -> ft.getTipo() == TipoFuncaoTransacao.INM && ft.getOrdem() == null)
            .count();
        
        if (ftInmComOrdemNull > 0) {
            log.warn("ATENÇÃO: {} funções INM com ordem NULL detectadas antes da persistência!", ftInmComOrdemNull);
        }
        
        salvarFuncaoDadosExcel(funcaoDados, analise, mapaFatorAjuste, ordemDados);
        salvarFuncaoTransacaoExcel(funcaotransacao, analise, mapaFatorAjuste, ordemTransacao);
        
        // Alterado: Flush para garantir que os dados das funções sejam persistidos no banco
        // antes de consultar a view vw_analise_soma_pf em atualizarPF
        entityManager.flush();
        
        // Alterado: Log de resumo das ordens após persistência
        log.info("Persistência concluída - Ordens atribuídas:");
        log.info("  - Funções de Dados: 1 até {}", ordemDados.get() - 1);
        log.info("  - Funções de Transação (todas): 1 até {}", ordemTransacao.get() - 1);
        
        atualizarPF(analise);
        salvarAnalise(analise);
    }

    @Transactional
    public Analise importarAnaliseExcel(AnaliseEditDTO analiseDTO) {
        User usuario = analiseFacade.obterUsuarioPorLogin();
        Analise analiseOrigem = converterEditDtoParaEntidade(analiseDTO);
        
        // Alterado: VALIDAR fatores de ajuste ANTES de criar nova instância e salvar
        // Validar sobre as funções da analiseOrigem (que contêm dados)
        log.info("===== VALIDAÇÃO DE FATORES DE AJUSTE =====");
        log.info("Mapa de Fator Ajuste: {}", analiseDTO.getMapaFatorAjuste());
        log.info("Manual: {}", analiseOrigem.getManual() != null ? analiseOrigem.getManual().getNome() : "null");
        log.info("Total funções dados: {}", analiseOrigem.getFuncaoDados() != null ? analiseOrigem.getFuncaoDados().size() : 0);
        log.info("Total funções transação: {}", analiseOrigem.getFuncaoTransacao() != null ? analiseOrigem.getFuncaoTransacao().size() : 0);
        
        // Alterado: Log detalhado dos fatores de ajuste nas funções
        if (analiseOrigem.getFuncaoDados() != null) {
            analiseOrigem.getFuncaoDados().forEach(fd -> {
                if (fd.getFatorAjuste() != null) {
                    log.info("FD '{}' tem FA: {}", fd.getName(), fd.getFatorAjuste().getNome());
                }
            });
        }
        if (analiseOrigem.getFuncaoTransacao() != null) {
        analiseOrigem.getFuncaoTransacao().forEach(ft -> {
            log.info("FT '{}' - Tipo: {} - Quantidade: {} - Ordem: {}", ft.getName(), ft.getTipo(), ft.getQuantidade(), ft.getOrdem());
            if (ft.getFatorAjuste() != null) {
                log.info("FT '{}' tem FA: {}", ft.getName(), ft.getFatorAjuste().getNome());
            }
        });
        }
        
        validarFatoresAjuste(
            analiseOrigem.getManual(),
            analiseOrigem.getFuncaoDados(),
            analiseOrigem.getFuncaoTransacao(),
            analiseDTO.getMapaFatorAjuste()
        );
        
        // Limpar a sessão para desanexar todas as entidades carregadas pelo converterEditDtoParaEntidade
        // Isso evita que objetos Der/Rlr/Alr gerenciados causem EntityExistsException quando salvamos novos objetos com mesmos IDs (se houver)
        entityManager.clear();
        
        // Recarregar usuário pois foi desanexado
        usuario = analiseFacade.obterUsuarioPorLogin();
        
        // Se chegou aqui, validação passou - prosseguir com salvamento
        Analise analise = new Analise();
        org.springframework.beans.BeanUtils.copyProperties(analiseOrigem, analise, "id", "funcaoDados", "funcaotransacao", "users");

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
        Set<FuncaoTransacao> funcaotransacao = analiseOrigem.getFuncaoTransacao();

        analise.setFuncaoTransacao(new HashSet<>());
        analise.setFuncaoDados(new HashSet<>());
        analise.setDataCriacaoOrdemServico(Timestamp.from(Instant.now()));

        // Alterado: Salvar APENAS no banco durante a transação (não no ElasticSearch)
        analiseFacade.salvarAnaliseApenasDB(analise);
        
        // Salvar funções (também apenas no banco)
        salvarFuncoesExcel(funcaoDados, funcaotransacao, analise, analiseDTO.getMapaFatorAjuste());
        
        // Alterado: Salvar no ElasticSearch APENAS após tudo ser persistido com sucesso
        // Se houver qualquer erro acima, a transação faz rollback e o ES não é tocado
        analiseFacade.salvarAnaliseApenasES(analise);
        
        return analise;
    }

    // Alterado: Receber coleções de funções como parâmetros em vez de usar analise.getFuncaoDados()
    private void validarFatoresAjuste(Manual manual, Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaotransacao, Map<String, Long> mapaFatorAjuste) {
        if (manual == null) {
            return;
        }
        
        // Alterado: Logs detalhados para debug
        log.info("===== DEBUG VALIDAÇÃO DE FATORES DE AJUSTE =====");
        log.info("Manual: {}", manual != null ? manual.getNome() : "null");
        
        Set<String> fatoresNaoEncontrados = new HashSet<>();
        Set<String> nomesFatoresManual = manual.getFatoresAjuste().stream()
                .map(FatorAjuste::getNome)
                .collect(Collectors.toSet());
        
        // Alterado: Log dos fatores do manual
        log.info("Fatores disponíveis no manual (total: {}): {}", nomesFatoresManual.size(), nomesFatoresManual);
        log.info("Mapa de de-para fornecido: {}", mapaFatorAjuste);
        
        // Alterado: Coletar TODOS os fatores únicos das funções
        Set<String> todosFatoresDaPlanilha = new HashSet<>();
        if (funcaoDados != null) {
            funcaoDados.forEach(fd -> {
                if (fd.getFatorAjuste() != null && fd.getFatorAjuste().getNome() != null) {
                    todosFatoresDaPlanilha.add(fd.getFatorAjuste().getNome());
                }
            });
        }
        if (funcaotransacao != null) {
            funcaotransacao.forEach(ft -> {
                if (ft.getFatorAjuste() != null && ft.getFatorAjuste().getNome() != null) {
                    todosFatoresDaPlanilha.add(ft.getFatorAjuste().getNome());
                }
            });
        }
        
        log.info("Fatores ÚNICOS encontrados na planilha (total: {}): {}", todosFatoresDaPlanilha.size(), todosFatoresDaPlanilha);

        // Alterado: Validar sobre as coleções recebidas como parâmetro
        if (funcaoDados != null) {
            funcaoDados.forEach(fd -> verificarFatorAjuste(fd, nomesFatoresManual, mapaFatorAjuste, fatoresNaoEncontrados));
        }
        if (funcaotransacao != null) {
            funcaotransacao.forEach(ft -> verificarFatorAjuste(ft, nomesFatoresManual, mapaFatorAjuste, fatoresNaoEncontrados));
        }
        
        // Alterado: Log dos fatores NÃO encontrados
        log.info("Fatores NÃO ENCONTRADOS (serão exibidos no de-para) (total: {}): {}", fatoresNaoEncontrados.size(), fatoresNaoEncontrados);
        log.info("==============================================");

        if (!fatoresNaoEncontrados.isEmpty()) {
            throw new FatorAjusteException(new ArrayList<>(fatoresNaoEncontrados));
        }
    }

    private void verificarFatorAjuste(FuncaoAnalise funcao, Set<String> nomesFatoresManual, Map<String, Long> mapaFatorAjuste, Set<String> fatoresNaoEncontrados) {
        if (funcao.getFatorAjuste() != null) {
            String nomeFator = funcao.getFatorAjuste().getNome();
            
            // Alterado: Verificação com trim e validação de null/vazio
            if (nomeFator == null || nomeFator.trim().isEmpty()) {
                log.warn("Função '{}' tem FatorAjuste com nome null ou vazio. Ignorando.", funcao.getName());
                return;
            }
            
            // Alterado: Usar trim para comparação
            String nomeFatorTrimmed = nomeFator.trim();
            boolean existeNoManual = nomesFatoresManual.contains(nomeFatorTrimmed);
            boolean existeNoMapa = mapaFatorAjuste != null && mapaFatorAjuste.containsKey(nomeFatorTrimmed);
            
            // Alterado: Log detalhado apenas para fatores não encontrados
            if (!existeNoManual && !existeNoMapa) {
                log.debug("Fator '{}' (length={}) NÃO encontrado - Manual: {} | Mapa: {}", 
                    nomeFatorTrimmed, nomeFatorTrimmed.length(), existeNoManual, existeNoMapa);
                fatoresNaoEncontrados.add(nomeFatorTrimmed);
            }
        }
    }

    // Alterado: Recebe contador de ordem para funções de transação
    private void salvarFuncaoTransacaoExcel(Set<FuncaoTransacao> funcaotransacao,
                                        Analise analise,
                                        Map<String, Long> mapaFatorAjuste,
                                        AtomicLong ordemTransacao) {
        
        log.info("=== SALVANDO {} FUNÇÕES DE TRANSAÇÃO ===", funcaotransacao.size());
        
        funcaotransacao.stream()
            .sorted(Comparator.comparing(FuncaoTransacao::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
            .forEach(funcaoTransacao -> {

                // Alterado: Log específico para funções INM
                if (funcaoTransacao.getTipo() == TipoFuncaoTransacao.INM) {
                    log.info("Persistindo FT INM '{}' - Ordem: {} - Quantidade ANTES cópia: {}", 
                        funcaoTransacao.getName(), 
                        funcaoTransacao.getOrdem(),
                        funcaoTransacao.getQuantidade());
                    
                    // Alterado: Validação crítica - ordem de INM não pode ser null
                    if (funcaoTransacao.getOrdem() == null) {
                        throw new RuntimeException(
                            String.format("ERRO CRÍTICO: Função INM '%s' com ordem NULL! " +
                                "Isso indica falha no processamento da extração.", 
                                funcaoTransacao.getName())
                        );
                    }
                } else {
                    log.debug("Processando FT normal '{}' - Ordem: {} - Quantidade: {}", 
                        funcaoTransacao.getName(), 
                        funcaoTransacao.getOrdem(),
                        funcaoTransacao.getQuantidade());
                }
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
                novaFuncao.setOrdem(funcaoTransacao.getOrdem());
                novaFuncao.setQuantidade(funcaoTransacao.getQuantidade());
                log.info("FT '{}' - Quantidade APÓS cópia: {}", funcaoTransacao.getName(), novaFuncao.getQuantidade());
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
                
                // Excluído: Linha que sobrescrevia a ordem já copiada da extração (linha 1103)
                // A ordem já foi corretamente definida durante a extração (uploadExcel)
                // e copiada na linha 1103. Não deve ser sobrescrita aqui.
                
                verificarFuncoes(novaFuncao, analise, mapaFatorAjuste);
                
                // Alterado: Cálculo do GrossPF baseado no Fator de Ajuste (APÓS verificarFuncoes)
                // Para funções INM, grosspf sempre é igual a pf (multiplicação por 1) - não recalcular
                if (novaFuncao.getTipo() != TipoFuncaoTransacao.INM) {
                    if (novaFuncao.getFatorAjuste() != null && novaFuncao.getFatorAjuste().getFator() != null) {
                        BigDecimal fator = novaFuncao.getFatorAjuste().getFator();
                        // Fator vem como 100, 50, etc.
                        // Se fator != 100 e != 0
                        if (fator.compareTo(new BigDecimal("100")) != 0 && fator.compareTo(BigDecimal.ZERO) != 0) {
                            // Fator Decimal = Fator / 100
                            BigDecimal fatorDecimal = fator.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                            // GrossPF = PF / FatorDecimal
                            BigDecimal grossPFCalculado = novaFuncao.getPf().divide(fatorDecimal, 4, RoundingMode.HALF_UP);
                            novaFuncao.setGrossPF(grossPFCalculado);
                            log.info("FT '{}' - Recalculado GrossPF: PF={} / (Fator={}/100) -> GrossPF={}", 
                                novaFuncao.getName(), novaFuncao.getPf(), fator, grossPFCalculado);
                        }
                    }
                } else {
                    log.info("FT INM '{}' - GrossPF mantido igual a PF: {}", novaFuncao.getName(), novaFuncao.getPf());
                }
                
                // Alterado: Criar novos Ders/Alrs ANTES de salvar a função
                Set<Der> novosDers = new HashSet<>();
                if (funcaoTransacao.getDers() != null) {
                    log.info("FT '{}' - Copiando {} DERs", funcaoTransacao.getName(), funcaoTransacao.getDers().size());
                    funcaoTransacao.getDers().forEach(derOriginal -> {
                        Der novoDer = new Der();
                        novoDer.setNome(derOriginal.getNome());
                        novoDer.setValor(derOriginal.getValor());
                        novoDer.setFuncaoTransacao(novaFuncao);
                        novosDers.add(novoDer);
                    });
                } else {
                    log.warn("FT '{}' - Lista de DERs é NULL", funcaoTransacao.getName());
                }
                novaFuncao.setDers(novosDers);
                
                Set<Alr> novosAlrs = new HashSet<>();
                if (funcaoTransacao.getAlrs() != null) {
                    log.info("FT '{}' - Copiando {} ALRs", funcaoTransacao.getName(), funcaoTransacao.getAlrs().size());
                    funcaoTransacao.getAlrs().forEach(alrOriginal -> {
                        Alr novoAlr = new Alr();
                        novoAlr.setNome(alrOriginal.getNome());
                        novoAlr.setValor(alrOriginal.getValor());
                        novoAlr.setFuncaoTransacao(novaFuncao);
                        novosAlrs.add(novoAlr);
                    });
                } else {
                    log.warn("FT '{}' - Lista de ALRs é NULL", funcaoTransacao.getName());
                }
                novaFuncao.setAlrs(novosAlrs);

                setarFuncionalidadeFuncao(novaFuncao, analise);
                analiseFacade.salvarFuncaoTransacao(novaFuncao);

                log.debug("FT '{}' salva com ordem: {}", 
                     novaFuncao.getName(), 
                     novaFuncao.getOrdem());
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
                
                // Alterado: Cálculo do GrossPF baseado no Fator de Ajuste (APÓS verificarFuncoes)
                if (novaFuncao.getFatorAjuste() != null && novaFuncao.getFatorAjuste().getFator() != null) {
                    BigDecimal fator = novaFuncao.getFatorAjuste().getFator();
                    // Fator vem como 100, 50, etc.
                    // Se fator != 100 e != 0
                    if (fator.compareTo(new BigDecimal("100")) != 0 && fator.compareTo(BigDecimal.ZERO) != 0) {
                        // Fator Decimal = Fator / 100
                        BigDecimal fatorDecimal = fator.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                        // GrossPF = PF / FatorDecimal
                        BigDecimal grossPFCalculado = novaFuncao.getPf().divide(fatorDecimal, 4, RoundingMode.HALF_UP);
                        novaFuncao.setGrossPF(grossPFCalculado);
                        log.info("FD '{}' - Recalculado GrossPF: PF={} / (Fator={}/100) -> GrossPF={}", 
                            novaFuncao.getName(), novaFuncao.getPf(), fator, grossPFCalculado);
                    }
                }
                
                // Alterado: Criar novos Ders/Rlrs ANTES de salvar a função
                Set<Der> novosDers = new HashSet<>();
                if (funcaoDado.getDers() != null) {
                    log.info("FD '{}' - Copiando {} DERs", funcaoDado.getName(), funcaoDado.getDers().size());
                    funcaoDado.getDers().forEach(derOriginal -> {
                        Der novoDer = new Der();
                        novoDer.setNome(derOriginal.getNome());
                        novoDer.setValor(derOriginal.getValor());
                        novoDer.setFuncaoDados(novaFuncao);
                        novosDers.add(novoDer);
                    });
                } else {
                    log.warn("FD '{}' - Lista de DERs é NULL", funcaoDado.getName());
                }
                novaFuncao.setDers(novosDers);
                
                Set<Rlr> novosRlrs = new HashSet<>();
                if (funcaoDado.getRlrs() != null) {
                    log.info("FD '{}' - Copiando {} RLRs", funcaoDado.getName(), funcaoDado.getRlrs().size());
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
    
    // Alterado: Se o módulo vier null (caso da importação com @JsonBackReference),
    // buscar a funcionalidade pelo nome nos módulos do sistema
    if (nomeModulo == null && nomeFuncionalidade != null) {
        log.info("Módulo null - buscando funcionalidade existente pelo nome: '{}'", nomeFuncionalidade);
        
        // Buscar pelos módulos do sistema
        if (analise.getSistema().getModulos() != null) {
            for (Modulo mod : analise.getSistema().getModulos()) {
                Optional<List<Funcionalidade>> funcionalidades = funcionalidadeRepository
                    .findAllByNomeIgnoreCaseAndModuloId(nomeFuncionalidade, mod.getId());
                
                if (funcionalidades.isPresent() && !funcionalidades.get().isEmpty()) {
                    Funcionalidade funcEncontrada = funcionalidades.get().get(0);
                    log.info("Funcionalidade encontrada: ID={}, Nome='{}', Módulo='{}'", 
                        funcEncontrada.getId(), funcEncontrada.getNome(), mod.getNome());
                    
                    funcao.setFuncionalidade(funcEncontrada);
                    return;
                }
            }
            log.warn("Funcionalidade '{}' não encontrada em nenhum módulo do sistema.", nomeFuncionalidade);
        } else {
            log.warn("Sistema não possui módulos carregados ou lista vazia.");
        }
        
        // Alterado: Se ainda está null, significa que a funcionalidade não existe no banco
        // Nesse caso, buscamos o nome do módulo do próprio objeto transiente da funcionalidade
        if (nomeModulo == null && funcao.getFuncionalidade().getModulo() != null) {
            nomeModulo = funcao.getFuncionalidade().getModulo().getNome();
            log.info("Módulo obtido do objeto transiente: '{}'", nomeModulo);
        }
    }

    
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
            analise.setFuncaoTransacao(analiseFacade.obterFuncaoTransacaoPorAnaliseId(idAnalise));
        }
        analise.setFuncaoDados(analiseFacade.obterFuncaoDadosPorAnaliseIdStatusFuncao(idAnalise));
        analise.setFuncaoTransacao(analiseFacade.obterFuncaoTransacaoPorAnaliseIdStatusFuncao(idAnalise));
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
        // Alterado: Log de início do processamento
        log.info("===== INÍCIO UPLOAD EXCEL =====");
        log.info("Arquivo recebido: {}", file.getOriginalFilename());
        
        Path tempDir = Files.createTempDirectory("");
        File tempFile = tempDir.resolve(file.getOriginalFilename()).toFile();
        file.transferTo(tempFile);

        try(
            FileInputStream inputStream = new FileInputStream(tempFile);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream)
        ) {

            Analise analise = new Analise();
            Set<FuncaoDados> funcaoDados = new HashSet<>();
            Set<FuncaoTransacao> funcaotransacao = new HashSet<>();
            
            // Alterado: Contadores separados para ordem das funções de dados e transação
            // A numeração de função de dados é independente da numeração de função de transação
            java.util.concurrent.atomic.AtomicLong ordemDados = new java.util.concurrent.atomic.AtomicLong(1L);
            java.util.concurrent.atomic.AtomicLong ordemTransacao = new java.util.concurrent.atomic.AtomicLong(1L);

            setarResumoExcelUpload(workbook, analise);
            
            // Alterado: Log dos dados gerais extraídos
            log.info("Dados Gerais Extraídos:");
            log.info("  - Identificador: {}", analise.getIdentificadorAnalise());
            log.info("  - Número OS: {}", analise.getNumeroOs());
            log.info("  - Método Contagem: {}", analise.getMetodoContagem());
            log.info("  - Tipo Análise: {}", analise.getTipoAnalise());
            
            // ATUALIZADO: Processa funções NORMAIS primeiro
            // ATUALIZADO: Chamadas corrigidas com argumentos corretos
            if (analise.getMetodoContagem().equals(MetodoContagem.INDICATIVA)) {
                // ✅ CORRETO: Indicativa processa apenas funções de dados (4 argumentos)
                setarIndicativaExcelUpload(workbook, funcaoDados, analise, ordemDados);

            } else if (analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)) {
                // ✅ CORRETO: Detalhada processa dados e transações (6 argumentos)
                setarExcelDetalhadaUpload(workbook, funcaoDados, funcaotransacao, analise, ordemDados, ordemTransacao);

            } else if (analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)) {
                // ✅ CORRETO: Estimada processa dados e transações (6 argumentos)
                setarExcelEstimadaUpload(workbook, funcaoDados, funcaotransacao, analise, ordemDados, ordemTransacao);
            }

            log.info("Funções normais processadas - FD: {}, FT: {}", funcaoDados.size(), funcaotransacao.size());
            log.info("Última ordem FT: {}", ordemTransacao.get() - 1);

            // ATUALIZADO: Processa funções INM DEPOIS (se não for Indicativa)
            if (!analise.getMetodoContagem().equals(MetodoContagem.INDICATIVA)) {
                log.info("=== INICIANDO PROCESSAMENTO INM ===");
                
                // Alterado: Criar contador separado para INM que começa do último valor de FT + 1
                java.util.concurrent.atomic.AtomicLong ordemInm = new java.util.concurrent.atomic.AtomicLong(ordemTransacao.get());
                log.info("Contador INM criado iniciando em: {}", ordemInm.get());

                setarInmExcelUpload(workbook, funcaotransacao, funcaoDados, analise, ordemInm);

                log.info("INM processado. Total FT (com INM): {}", funcaotransacao.size());
                log.info("Última ordem após INM: {}", ordemInm.get() - 1);
            }

            // Atribui funções à análise
            analise.setFuncaoDados(funcaoDados);
            analise.setFuncaoTransacao(funcaotransacao);
            
            // Alterado: Log resumo das funções extraídas
            log.info("Funções Extraídas:");
            log.info("  - Total Funções de Dados: {}", funcaoDados.size());
            log.info("  - Total Funções de Transação: {}", funcaotransacao.size());
            
            // Alterado: Log detalhes das primeiras 3 funções de dados (para debug)
            if (!funcaoDados.isEmpty()) {
                log.info("Primeiras Funções de Dados:");
                funcaoDados.stream().limit(3).forEach(fd -> {
                    String nomeModulo = fd.getFuncionalidade() != null && fd.getFuncionalidade().getModulo() != null 
                        ? fd.getFuncionalidade().getModulo().getNome() : "null";
                    String nomeFuncionalidade = fd.getFuncionalidade() != null 
                        ? fd.getFuncionalidade().getNome() : "null";
                    log.info("    FD: {} | Módulo: {} | Funcionalidade: {} | Tipo: {} | Ordem: {}", 
                        fd.getName(), nomeModulo, nomeFuncionalidade, fd.getTipo(), fd.getOrdem());
                });
            }
            
            // Alterado: Log detalhes das primeiras 3 funções de transação (para debug)
            if (!funcaotransacao.isEmpty()) {
                log.info("Primeiras Funções de Transação:");
                funcaotransacao.stream()
                    .limit(3)
                    .forEach(ft -> {
                        String nomeModulo = ft.getFuncionalidade() != null && ft.getFuncionalidade().getModulo() != null 
                            ? ft.getFuncionalidade().getModulo().getNome() : "NULL";
                        String nomeFuncionalidade = ft.getFuncionalidade() != null 
                            ? ft.getFuncionalidade().getNome() : "NULL";
                        log.info("    FT: {} | Módulo: {} | Funcionalidade: {} | Tipo: {} | Ordem: {} | Quantidade: {}",
                            ft.getName(), nomeModulo, nomeFuncionalidade, ft.getTipo(), ft.getOrdem(), ft.getQuantidade());
                    });
            }
            
            log.info("===== FIM UPLOAD EXCEL =====");
            return analise;

        }
    }

    // Planilha Detalhada
    // Alterado: Recebe contadores separados para funções de dados e transação
    public void setarExcelDetalhadaUpload(XSSFWorkbook excelFile, Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaotransacao, Analise analise, java.util.concurrent.atomic.AtomicLong ordemDados, java.util.concurrent.atomic.AtomicLong ordemTransacao) {
        XSSFSheet excelSheet = excelFile.getSheet(DETALHADA);
        // Alterado: Iteração corrigida - linha 10 (índice 9) até linha 1323 (índice 1322)
        for (int i = 9; i < 1323; i++) {
            XSSFRow row = excelSheet.getRow(i);
            if (row != null && getCellValueAsNumber(row, 0) > 0) {
                // Alterado: Usar helper para ler tipo (Coluna G = índice 6)
                String tipo = getCellValueAsString(row, 6);
                if (tipoFuncaoDados().contains(tipo)) {
                    funcaoDados.add(setarFuncaoDadosDetalhada(row, analise, ordemDados));
                } else if (tipoFuncaoTransacao().contains(tipo)) {
                    funcaotransacao.add(setarFuncaoTrasacaoDetalhada(row, analise, ordemTransacao));
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
            log.info("Row {} - Raw DERs: '{}'", row.getRowNum() + 1, dersValue);
            // Split by comma, semicolon, or newline, ignoring surrounding whitespace
            String[] values = dersValue.split("[,;\\n]+");
            Arrays.stream(values).forEach(value -> {
                if (value != null && !value.trim().isEmpty()) {
                    Der der = new Der();
                    der.setNome(value.trim());
                    ders.add(der);
                }
            });
            log.info("Row {} - Parsed {} DERs", row.getRowNum() + 1, ders.size());
        } else {
            log.info("Row {} - Raw DERs is empty or null", row.getRowNum() + 1);
        }

        // Coluna K (índice 10) - RLR/ALR Descrição
        String alrsValue = getCellValueAsString(row, 10);
        if (alrsValue != null && !alrsValue.trim().isEmpty()) {
            log.info("Row {} - Raw ALRs: '{}'", row.getRowNum() + 1, alrsValue);
            String[] values = alrsValue.split("[,;\\n]+");
            Arrays.stream(values).forEach(value -> {
                if (value != null && !value.trim().isEmpty()) {
                    Alr alr = new Alr();
                    alr.setNome(value.trim());
                    alrs.add(alr);
                }
            });
            log.info("Row {} - Parsed {} ALRs", row.getRowNum() + 1, alrs.size());
        } else {
            log.info("Row {} - Raw ALRs is empty or null", row.getRowNum() + 1);
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

    // Alterado: Método setarModuloFuncionalidade removido daqui - agora está implementado com validações mais abaixo (linha ~2199)

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
        
        // Alterado: Coluna J (índice 9) - Quantidade
        double quantidade = getCellValueAsNumber(row, 9);
        funcaoTransacao.setQuantidade((int) quantidade);
        
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
    public void setarExcelEstimadaUpload(XSSFWorkbook excelFile, Set<FuncaoDados> funcaoDados, Set<FuncaoTransacao> funcaotransacao, Analise analise, java.util.concurrent.atomic.AtomicLong ordemDados, java.util.concurrent.atomic.AtomicLong ordemTransacao) {
        XSSFSheet excelSheetEstimada = excelFile.getSheet(ESTIMATIVA);
        for (int i = 10; i < 1081; i++) {
            XSSFRow row = excelSheetEstimada.getRow(i);
            if (row != null && getCellValueAsNumber(row, 0) > 0) {
                // Alterado: Coluna H (índice 7) - Tipo da função (antes coluna G)
                String tipo = getCellValueAsString(row, 7);
                if (tipoFuncaoDados().contains(tipo)) {
                    funcaoDados.add(setarFuncaoDadosEstimada(row, analise, ordemDados));
                } else if (tipoFuncaoTransacao().contains(tipo)) {
                    funcaotransacao.add(setarFuncaoTransacaoEstimada(row, analise, ordemTransacao));
                }
            }
        }
    }

    // Alterado: Novo mapeamento de colunas para análise Estimada
    private void setarModuloFuncionalidadeEstimada(FuncaoAnalise funcao, XSSFRow row) {
        Funcionalidade funcionalidade = new Funcionalidade();
        Modulo modulo = new Modulo();
        FatorAjuste fatorAjuste = new FatorAjuste();
        
        funcao.setId((long) getCellValueAsNumber(row, 0));
        
        // Alterado: Coluna E (índice 4) - Módulo
        modulo.setNome(getCellValueAsString(row, 4));
        funcionalidade.setModulo(modulo);
        
        // Alterado: Coluna F (índice 5) - Funcionalidade
        funcionalidade.setNome(getCellValueAsString(row, 5));
        funcao.setFuncionalidade(funcionalidade);
        
        // Alterado: Coluna G (índice 6) - Nome da função
        funcao.setName(getCellValueAsString(row, 6));
        
        // Alterado: Fator de Ajuste com trim() e validação (consistência com Detalhada)
        // Alterado: Ler também o valor da coluna C (índice 2) e concatenar ao nome
        String nomeFatorAjuste = getCellValueAsString(row, 1); // Coluna B
        String valorFatorAjuste = getCellValueAsString(row, 2); // Coluna C
        
        if (nomeFatorAjuste != null && !nomeFatorAjuste.trim().isEmpty()) {
            String nomeCompletoFA = nomeFatorAjuste.trim();
            if (valorFatorAjuste != null && !valorFatorAjuste.trim().isEmpty()) {
                nomeCompletoFA = nomeCompletoFA + " - " + valorFatorAjuste.trim();
            }
            fatorAjuste.setNome(nomeCompletoFA);
            funcao.setFatorAjuste(fatorAjuste);
        } else {
            log.warn("Função '{}' não tem Fator de Ajuste na planilha (Coluna B vazia)", funcao.getName());
        }
    }

    // Alterado: Novo mapeamento de colunas e complexidade fixa para análise Estimada
    private FuncaoDados setarFuncaoDadosEstimada(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        FuncaoDados funcaoDados = new FuncaoDados();
        setarModuloFuncionalidadeEstimada(funcaoDados, row);
        
        // Alterado: Persistir/Vincular Funcionalidade e Módulo
        setarFuncionalidadeFuncao(funcaoDados, analise);
        
        // Alterado: Coluna H (índice 7) - Tipo da função (antes coluna G)
        String tipo = getCellValueAsString(row, 7);
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
        
        // Alterado: Complexidade FIXA = BAIXA para funções de dados (análise Estimada)
        funcaoDados.setComplexidade(Complexidade.BAIXA);
        
        // Alterado: Coluna I (índice 8) - PF (mantém posição)
        funcaoDados.setPf(BigDecimal.valueOf(getCellValueAsNumber(row, 8)));
        funcaoDados.setGrossPF(BigDecimal.valueOf(getCellValueAsNumber(row, 8)));
        
        // Alterado: Coluna J (índice 9) - Sustentação (antes coluna R)
        funcaoDados.setSustantation(getCellValueAsString(row, 9));
        
        funcaoDados.setOrdem(ordem.getAndIncrement());
        return funcaoDados;
    }

    // Alterado: Novo mapeamento de colunas e complexidade fixa para análise Estimada
    private FuncaoTransacao setarFuncaoTransacaoEstimada(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordem) {
        FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
        setarModuloFuncionalidadeEstimada(funcaoTransacao, row);
        
        // Alterado: Persistir/Vincular Funcionalidade e Módulo
        setarFuncionalidadeFuncao(funcaoTransacao, analise);
        
        // Alterado: Coluna H (índice 7) - Tipo da função (antes coluna G)
        setarTipoFuncaoTransacao(funcaoTransacao, getCellValueAsString(row, 7));
        
        // Alterado: Complexidade FIXA = MEDIA para funções de transação (análise Estimada)
        funcaoTransacao.setComplexidade(Complexidade.MEDIA);
        
        // Alterado: Coluna I (índice 8) - PF (mantém posição)
        funcaoTransacao.setPf(BigDecimal.valueOf(getCellValueAsNumber(row, 8)));
        funcaoTransacao.setGrossPF(BigDecimal.valueOf(getCellValueAsNumber(row, 8)));
        
        // Alterado: Coluna J (índice 9) - Sustentação (antes coluna R)
        funcaoTransacao.setSustantation(getCellValueAsString(row, 9));
        
        // Alterado: Coluna J (índice 9) - Quantidade
        double quantidade = getCellValueAsNumber(row, 9);
        funcaoTransacao.setQuantidade((int) quantidade);
        
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
    
    // Alterado: Método para converter Analise para AnaliseUploadDTO incluindo módulos
    private br.com.basis.abaco.service.dto.upload.AnaliseUploadDTO converterParaUploadDTO(Analise analise) {
        br.com.basis.abaco.service.dto.upload.AnaliseUploadDTO dto = new br.com.basis.abaco.service.dto.upload.AnaliseUploadDTO();
        
        dto.setIdentificadorAnalise(analise.getIdentificadorAnalise());
        dto.setNumeroOs(analise.getNumeroOs());
        dto.setMetodoContagem(analise.getMetodoContagem() != null ? analise.getMetodoContagem().toString() : null);
        dto.setEscopo(analise.getEscopo());
        dto.setPropositoContagem(analise.getPropositoContagem());
        dto.setBloqueiaAnalise(analise.isBloqueiaAnalise());
        dto.setEnviarBaseline(analise.isEnviarBaseline());
        
        // Converter funções de dados
        if (analise.getFuncaoDados() != null) {
            analise.getFuncaoDados().forEach(fd -> {
                br.com.basis.abaco.service.dto.upload.FuncaoUploadDTO funcaoDTO = new br.com.basis.abaco.service.dto.upload.FuncaoUploadDTO();
                funcaoDTO.setId(fd.getId());
                funcaoDTO.setName(fd.getName());
                funcaoDTO.setTipo(fd.getTipo() != null ? fd.getTipo().toString() : null);
                funcaoDTO.setComplexidade(fd.getComplexidade());
                funcaoDTO.setPf(fd.getPf());
                funcaoDTO.setGrossPF(fd.getGrossPF());
                funcaoDTO.setSustantation(fd.getSustantation());
                funcaoDTO.setStatusFuncao(fd.getStatusFuncao());
                funcaoDTO.setImpacto(fd.getImpacto());
                funcaoDTO.setOrdem(fd.getOrdem());
                
                // Converter funcionalidade e módulo
                if (fd.getFuncionalidade() != null) {
                    br.com.basis.abaco.service.dto.upload.FuncionalidadeUploadDTO funcionalidadeDTO = 
                        new br.com.basis.abaco.service.dto.upload.FuncionalidadeUploadDTO();
                    funcionalidadeDTO.setId(fd.getFuncionalidade().getId());
                    funcionalidadeDTO.setNome(fd.getFuncionalidade().getNome());
                    
                    if (fd.getFuncionalidade().getModulo() != null) {
                        br.com.basis.abaco.service.dto.upload.ModuloUploadDTO moduloDTO = 
                            new br.com.basis.abaco.service.dto.upload.ModuloUploadDTO();
                        moduloDTO.setId(fd.getFuncionalidade().getModulo().getId());
                        moduloDTO.setNome(fd.getFuncionalidade().getModulo().getNome());
                        funcionalidadeDTO.setModulo(moduloDTO);
                    }
                    
                    funcaoDTO.setFuncionalidade(funcionalidadeDTO);
                }

                // Converter Fator de Ajuste
                if (fd.getFatorAjuste() != null) {
                    br.com.basis.abaco.service.dto.upload.FatorAjusteUploadDTO fatorDTO = 
                        new br.com.basis.abaco.service.dto.upload.FatorAjusteUploadDTO();
                    fatorDTO.setId(fd.getFatorAjuste().getId());
                    fatorDTO.setNome(fd.getFatorAjuste().getNome());
                    fatorDTO.setFator(fd.getFatorAjuste().getFator());
                    fatorDTO.setAtivo(fd.getFatorAjuste().getAtivo());
                    fatorDTO.setTipoAjuste(fd.getFatorAjuste().getTipoAjuste());
                    fatorDTO.setImpacto(fd.getFatorAjuste().getImpacto());
                    fatorDTO.setDescricao(fd.getFatorAjuste().getDescricao());
                    fatorDTO.setCodigo(fd.getFatorAjuste().getCodigo());
                    fatorDTO.setOrigem(fd.getFatorAjuste().getOrigem());
                    funcaoDTO.setFatorAjuste(fatorDTO);
                }

                // Converter DERs
                if (fd.getDers() != null) {
                    fd.getDers().forEach(der -> {
                        br.com.basis.abaco.service.dto.DerDTO derDTO = new br.com.basis.abaco.service.dto.DerDTO();
                        derDTO.setId(der.getId());
                        derDTO.setNome(der.getNome());
                        derDTO.setValor(der.getValor());
                        funcaoDTO.getDers().add(derDTO);
                    });
                }

                // Converter RLRs
                if (fd.getRlrs() != null) {
                    fd.getRlrs().forEach(rlr -> {
                        br.com.basis.abaco.service.dto.RlrDTO rlrDTO = new br.com.basis.abaco.service.dto.RlrDTO();
                        rlrDTO.setId(rlr.getId());
                        rlrDTO.setNome(rlr.getNome());
                        rlrDTO.setValor(rlr.getValor());
                        funcaoDTO.getRlrs().add(rlrDTO);
                    });
                }
                
                dto.getFuncaoDados().add(funcaoDTO);
            });
        }
        
        // Converter funções de transação
        if (analise.getFuncaoTransacao() != null) {
            analise.getFuncaoTransacao().forEach(ft -> {
                br.com.basis.abaco.service.dto.upload.FuncaoUploadDTO funcaoDTO = new br.com.basis.abaco.service.dto.upload.FuncaoUploadDTO();
                funcaoDTO.setId(ft.getId());
                funcaoDTO.setName(ft.getName());
                funcaoDTO.setTipo(ft.getTipo() != null ? ft.getTipo().toString() : null);
                funcaoDTO.setComplexidade(ft.getComplexidade());
                funcaoDTO.setPf(ft.getPf());
                funcaoDTO.setGrossPF(ft.getGrossPF());
                funcaoDTO.setSustantation(ft.getSustantation());
                funcaoDTO.setStatusFuncao(ft.getStatusFuncao());
                funcaoDTO.setImpacto(ft.getImpacto());
                funcaoDTO.setOrdem(ft.getOrdem());
                funcaoDTO.setQuantidade(ft.getQuantidade()); // Alterado: Adicionado para preservar quantidade no DTO de upload
                
                // Converter funcionalidade e módulo
                if (ft.getFuncionalidade() != null) {
                    br.com.basis.abaco.service.dto.upload.FuncionalidadeUploadDTO funcionalidadeDTO = 
                        new br.com.basis.abaco.service.dto.upload.FuncionalidadeUploadDTO();
                    funcionalidadeDTO.setId(ft.getFuncionalidade().getId());
                    funcionalidadeDTO.setNome(ft.getFuncionalidade().getNome());
                    
                    if (ft.getFuncionalidade().getModulo() != null) {
                        br.com.basis.abaco.service.dto.upload.ModuloUploadDTO moduloDTO = 
                            new br.com.basis.abaco.service.dto.upload.ModuloUploadDTO();
                        moduloDTO.setId(ft.getFuncionalidade().getModulo().getId());
                        moduloDTO.setNome(ft.getFuncionalidade().getModulo().getNome());
                        funcionalidadeDTO.setModulo(moduloDTO);
                    }
                    
                    funcaoDTO.setFuncionalidade(funcionalidadeDTO);
                }

                // Converter Fator de Ajuste
                if (ft.getFatorAjuste() != null) {
                    br.com.basis.abaco.service.dto.upload.FatorAjusteUploadDTO fatorDTO = 
                        new br.com.basis.abaco.service.dto.upload.FatorAjusteUploadDTO();
                    fatorDTO.setId(ft.getFatorAjuste().getId());
                    fatorDTO.setNome(ft.getFatorAjuste().getNome());
                    fatorDTO.setFator(ft.getFatorAjuste().getFator());
                    fatorDTO.setAtivo(ft.getFatorAjuste().getAtivo());
                    fatorDTO.setTipoAjuste(ft.getFatorAjuste().getTipoAjuste());
                    fatorDTO.setImpacto(ft.getFatorAjuste().getImpacto());
                    fatorDTO.setDescricao(ft.getFatorAjuste().getDescricao());
                    fatorDTO.setCodigo(ft.getFatorAjuste().getCodigo());
                    fatorDTO.setOrigem(ft.getFatorAjuste().getOrigem());
                    funcaoDTO.setFatorAjuste(fatorDTO);
                }

                // Converter DERs
                if (ft.getDers() != null) {
                    ft.getDers().forEach(der -> {
                        br.com.basis.abaco.service.dto.DerDTO derDTO = new br.com.basis.abaco.service.dto.DerDTO();
                        derDTO.setId(der.getId());
                        derDTO.setNome(der.getNome());
                        derDTO.setValor(der.getValor());
                        funcaoDTO.getDers().add(derDTO);
                    });
                }

                // Converter ALRs
                if (ft.getAlrs() != null) {
                    ft.getAlrs().forEach(alr -> {
                        br.com.basis.abaco.service.dto.AlrDTO alrDTO = new br.com.basis.abaco.service.dto.AlrDTO();
                        alrDTO.setId(alr.getId());
                        alrDTO.setNome(alr.getNome());
                        alrDTO.setValor(alr.getValor());
                        funcaoDTO.getAlrs().add(alrDTO);
                    });
                }
                
                dto.getFuncaoTransacao().add(funcaoDTO);
            });
        }
        
        return dto;
    }

    // Planilha Resumo
    public void setarResumoExcelUpload(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet sheet = excelFile.getSheet(RESUMO);
        analise.setNumeroOs(sheet.getRow(3).getCell(1).getStringCellValue());
        
        // Alterado: Usar helper para tratar células que podem conter fórmulas
        // Linha 10 (índice 9), Coluna A (índice 0)
        analise.setPropositoContagem(getCellValueAsString(sheet.getRow(9), 0));
        
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
    // Alterado: Refatorado completamente - Aba AFP-INM para análises Estimada ou Detalhada
    // Todas as funções INM são do tipo FuncaoTransacao com tipo 'INM'
    public void setarInmExcelUpload(
        XSSFWorkbook excelFile,
        Set<FuncaoTransacao> funcaotransacao,
        Set<FuncaoDados> funcaoDados,
        Analise analise,
        AtomicLong ordemInm // Alterado: Contador SEPARADO exclusivo para funções INM
    ) {
        
        log.info("=== PROCESSANDO ABA AFP - INM ===");
        log.info("Ordem inicial para INM (contador separado): {}", ordemInm.get());

        XSSFSheet excelSheetINM = excelFile.getSheet("AFP - INM");
        
        if (excelSheetINM == null) {
            log.warn("Aba 'AFP - INM' não encontrada no arquivo Excel");
            return;
        }
        
        // Alterado: Verifica se aba INM tem conteúdo (linha 11, coluna B)
        XSSFRow primeiraLinhaINM = excelSheetINM.getRow(10); // Linha 11 (índice 10)
        if (primeiraLinhaINM == null || getCellValueAsString(primeiraLinhaINM, 1).trim().isEmpty()) {
            log.info("Aba AFP-INM está vazia (sem dados na linha 11, coluna B). Pulando processamento INM.");
            return;
        }
        
        log.info("Processando aba AFP-INM - Funções de Transação tipo INM");
        
        int countINM = 0;
        // Alterado: Iteração correta - linha 11 (índice 10) até linha 382 (índice 381)
        for (int i = 10; i < 382; i++) {
            XSSFRow row = excelSheetINM.getRow(i);
            
            // Alterado: Valida se linha existe e tem conteúdo na coluna B (Fator de Ajuste)
            if (row != null && !getCellValueAsString(row, 1).trim().isEmpty()) {
                FuncaoTransacao funcaoINM = setarFuncaoTransacaoInm(row, analise, ordemInm);
                if (funcaoINM != null) {
                    funcaotransacao.add(funcaoINM);
                    countINM++;
                    log.debug("INM '{}' - Ordem: {}", funcaoINM.getName(), funcaoINM.getOrdem());
                }
            }
        }
        
        log.info("Total de funções INM processadas: {}", countINM);
        log.info("Ordem final após INM: {}", ordemInm.get() - 1);
        log.info("=== FIM PROCESSAMENTO INM ===");
    }

    // Alterado: Novo método para criar Função de Transação INM com mapeamento correto
    private FuncaoTransacao setarFuncaoTransacaoInm(XSSFRow row, Analise analise, java.util.concurrent.atomic.AtomicLong ordemInm) {
        FuncaoTransacao funcaoTransacao = new FuncaoTransacao();
        
        try {
            // Alterado: Tipo fixo para todas as funções INM
            funcaoTransacao.setTipo(TipoFuncaoTransacao.INM);
            
            // Alterado: Atribuir ordem PRIMEIRO, antes de qualquer validação que possa causar return
            // Isso garante que a função sempre terá ordem, mesmo se houver erro posterior
            funcaoTransacao.setOrdem(ordemInm.getAndIncrement());
            
            // Alterado: Mapeamento correto das colunas
            // Coluna B (1): Fator de Ajuste
            String nomeFatorAjuste = getCellValueAsString(row, 1);
            if (nomeFatorAjuste != null && !nomeFatorAjuste.trim().isEmpty()) {
                FatorAjuste fatorAjuste = new FatorAjuste();
                fatorAjuste.setNome(nomeFatorAjuste.trim());
                funcaoTransacao.setFatorAjuste(fatorAjuste);
            }
            
            // Alterado: Extrair Módulo e Funcionalidade
            // Coluna F (5): Módulo
            // Coluna G (6): Funcionalidade
            String nomeModulo = getCellValueAsString(row, 5);
            String nomeFuncionalidade = getCellValueAsString(row, 6);
            
            if (nomeModulo == null || nomeModulo.trim().isEmpty() || 
                nomeFuncionalidade == null || nomeFuncionalidade.trim().isEmpty()) {
                log.warn("Linha {} da aba INM: Módulo ou Funcionalidade vazios. Pulando função.", row.getRowNum() + 1);
                return null;
            }
            
            // Alterado: Criar estrutura Módulo -> Funcionalidade
            Modulo modulo = new Modulo();
            modulo.setNome(nomeModulo.trim());
            
            Funcionalidade funcionalidade = new Funcionalidade();
            funcionalidade.setModulo(modulo);
            funcionalidade.setNome(nomeFuncionalidade.trim());
            
            funcaoTransacao.setFuncionalidade(funcionalidade);
            
            // Alterado: Coluna H (7): Nome da função
            String nomeFuncao = getCellValueAsString(row, 7);
            funcaoTransacao.setName(nomeFuncao != null ? nomeFuncao.trim() : "");
            
            // Alterado: Coluna J (9): Quantidade
            double quantidade = getCellValueAsNumber(row, 9);
            if (quantidade < 0) {
                log.warn("INM linha {}: Quantidade negativa ({}) ajustada para 1", row.getRowNum() + 1, quantidade);
                quantidade = 1;
            }
            funcaoTransacao.setQuantidade((int) quantidade);
            log.debug("INM '{}' - Quantidade: {}", funcaoTransacao.getName(), (int)quantidade);
            
            // Alterado: Coluna M (12): Tipo do cálculo (Unidade ou Percentual)
            // String tipoCalculo = getCellValueAsString(row, 12);
            // TODO: Verificar se existe campo específico para armazenar tipo de cálculo
            // Por enquanto, pode ser armazenado em observações ou campo customizado
            
            // Alterado: Coluna P (15): Complexidade
            String complexidadeStr = getCellValueAsString(row, 15);
            if (complexidadeStr != null && !complexidadeStr.trim().isEmpty()) {
                complexidadeStr = complexidadeStr.trim();
                if ("Simples".equalsIgnoreCase(complexidadeStr)) {
                    funcaoTransacao.setComplexidade(Complexidade.BAIXA);
                } else if ("Média".equalsIgnoreCase(complexidadeStr) || "Medio".equalsIgnoreCase(complexidadeStr)) {
                    funcaoTransacao.setComplexidade(Complexidade.MEDIA);
                } else if ("Complexa".equalsIgnoreCase(complexidadeStr) || "Complexo".equalsIgnoreCase(complexidadeStr)) {
                    funcaoTransacao.setComplexidade(Complexidade.ALTA);
                } else {
                    log.warn("Complexidade desconhecida na linha {}: '{}'", row.getRowNum() + 1, complexidadeStr);
                }
            }
            
            // Alterado: Coluna S (18): PF (Pontos de Função)
            double pf = getCellValueAsNumber(row, 18);
            funcaoTransacao.setPf(new BigDecimal(pf));
            funcaoTransacao.setGrossPF(new BigDecimal(pf)); // PF bruto = PF
            
            // Alterado: Coluna T (19): Observação/Sustentação
            String observacao = getCellValueAsString(row, 19);
            funcaoTransacao.setSustantation(observacao != null ? observacao.trim() : "");
            
            // Alterado: Status e Impacto ficam como null (não definidos para funções INM)
            funcaoTransacao.setStatusFuncao(null);
            funcaoTransacao.setImpacto(null);
            
            log.debug("INM '{}' - Ordem atribuída: {}", funcaoTransacao.getName(), funcaoTransacao.getOrdem());
            log.debug("Função INM criada: {} - Módulo: {} - Funcionalidade: {} - Ordem: {}", 
                funcaoTransacao.getName(), nomeModulo, nomeFuncionalidade, funcaoTransacao.getOrdem());
            
            return funcaoTransacao;
            
        } catch (Exception e) {
            log.error("Erro ao processar função INM na linha {}: {}", row.getRowNum() + 1, e.getMessage(), e);
            return null;
        }
    }

    // Alterado: Método antigo removido - não é mais necessário
    // private FuncaoTransacao setarInm() - REMOVIDO
    
    // Alterado: Método antigo removido - funções INM não são do tipo FuncaoDados
    // private FuncaoDados setarFuncaoDadosInm() - REMOVIDO

    private void setarModuloFuncionalidade(FuncaoAnalise funcao, XSSFRow row) {
        Modulo modulo = new Modulo();
        Funcionalidade funcionalidade = new Funcionalidade();
        FatorAjuste fatorAjuste = new FatorAjuste();
        
        // Alterado: Extração dos valores de Módulo e Funcionalidade
        String nomeModulo = getCellValueAsString(row, 3); // Coluna D
        String nomeFuncionalidade = getCellValueAsString(row, 4); // Coluna E
        String nomeFuncao = getCellValueAsString(row, 5); // Coluna F
        
        // Alterado: Validação rigorosa com mensagem amigável ao usuário
        if (nomeModulo == null || nomeModulo.trim().isEmpty()) {
            String mensagemErro = String.format(
                "Erro na importação: Módulo não encontrado na linha %d da planilha. " +
                "A função '%s' está sem o nome do módulo (coluna D). " +
                "Por favor, verifique a planilha e preencha o módulo para todas as funções.",
                row.getRowNum() + 1,
                nomeFuncao != null && !nomeFuncao.trim().isEmpty() ? nomeFuncao : "sem nome"
            );
            log.error(mensagemErro);
            throw new RuntimeException(mensagemErro);
        }
        
        if (nomeFuncionalidade == null || nomeFuncionalidade.trim().isEmpty()) {
            String mensagemErro = String.format(
                "Erro na importação: Funcionalidade não encontrada na linha %d da planilha. " +
                "A função '%s' do módulo '%s' está sem o nome da funcionalidade (coluna E). " +
                "Por favor, verifique a planilha e preencha a funcionalidade para todas as funções.",
                row.getRowNum() + 1,
                nomeFuncao != null && !nomeFuncao.trim().isEmpty() ? nomeFuncao : "sem nome",
                nomeModulo.trim()
            );
            log.error(mensagemErro);
            throw new RuntimeException(mensagemErro);
        }
        
        // Alterado: Se passou nas validações, atribuir os valores
        modulo.setNome(nomeModulo.trim());
        funcionalidade.setModulo(modulo);
        funcionalidade.setNome(nomeFuncionalidade.trim());
        funcao.setFuncionalidade(funcionalidade);
        funcao.setName(nomeFuncao != null ? nomeFuncao.trim() : "");
        
        // Alterado: Fator de Ajuste (Coluna B)
        // Alterado: Ler também o valor da coluna C (índice 2) e concatenar ao nome
        String nomeFatorAjuste = getCellValueAsString(row, 1);
        String valorFatorAjuste = getCellValueAsString(row, 2); // Coluna C
        
        log.info("=== LENDO FA: Linha={}, Coluna B='{}', Coluna C='{}', Nome Função='{}'", row.getRowNum() + 1, nomeFatorAjuste, valorFatorAjuste, nomeFuncao);
        
        if (nomeFatorAjuste != null && !nomeFatorAjuste.trim().isEmpty()) {
            String nomeCompletoFA = nomeFatorAjuste.trim();
            if (valorFatorAjuste != null && !valorFatorAjuste.trim().isEmpty()) {
                nomeCompletoFA = nomeCompletoFA + " - " + valorFatorAjuste.trim();
            }
            
            fatorAjuste.setNome(nomeCompletoFA);
            funcao.setFatorAjuste(fatorAjuste);
            log.info("=== FA SETADO: {} para função '{}'", fatorAjuste.getNome(), funcao.getName());
        } else {
            log.warn("=== FA VAZIO: Linha {} não tem Fator de Ajuste na coluna B", row.getRowNum() + 1);
        }
    }

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
