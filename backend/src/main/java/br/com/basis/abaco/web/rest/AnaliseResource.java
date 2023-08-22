package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Compartilhada;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoDeDataAnalise;
import br.com.basis.abaco.domain.enumeration.TipoRelatorio;
import br.com.basis.abaco.reports.rest.RelatorioAnaliseRest;
import br.com.basis.abaco.reports.util.RelatorioUtil;
import br.com.basis.abaco.repository.AnaliseRepository;
import br.com.basis.abaco.repository.CompartilhadaRepository;
import br.com.basis.abaco.service.AnaliseService;
import br.com.basis.abaco.service.dto.AnaliseDTO;
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
import br.com.basis.abaco.web.rest.util.HeaderUtil;
import br.com.basis.abaco.web.rest.util.PaginationUtil;
import br.com.basis.dynamicexports.util.DynamicExporter;
import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import net.sf.jasperreports.engine.JRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AnaliseResource {

    private final Logger log = LoggerFactory.getLogger(AnaliseResource.class);
    public static final String API_ANALISES = "/api/analises/";
    public static final String NOME_RELATORIO = "relatorio.";
    public static final String ENTITY_NAME = "analise";
    public static final String PAGE = "page";
    private final AnaliseRepository analiseRepository;
    private final CompartilhadaRepository compartilhadaRepository;
    private final AnaliseService analiseService;
    private RelatorioAnaliseRest relatorioAnaliseRest;
    private HttpServletResponse response;
    private HttpServletRequest request;

    @PostConstruct
    private void init() {
        relatorioAnaliseRest = new RelatorioAnaliseRest(response, request);
    }

    public AnaliseResource(AnaliseRepository analiseRepository,
                           CompartilhadaRepository compartilhadaRepository,
                           AnaliseService analiseService) {
        this.analiseRepository = analiseRepository;
        this.compartilhadaRepository = compartilhadaRepository;
        this.analiseService = analiseService;
    }

    @PostMapping("/analises")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_CADASTRAR")
    public ResponseEntity<AnaliseDTO> criarAnalise(@Valid @RequestBody AnaliseEditDTO analise) throws URISyntaxException {
        if (analise.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new analise cannot already have an ID")).body(null);
        } else {
            AnaliseDTO analiseDTO = analiseService.criarAnalise(analise);
            return ResponseEntity.created(new URI(API_ANALISES + null)).headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, analiseDTO.getId().toString())).body(analiseDTO);
        }
    }

    @PutMapping("/analises")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EDITAR")
    public ResponseEntity<AnaliseDTO> atualizarAnalise(@Valid @RequestBody AnaliseEditDTO analiseDTO) throws URISyntaxException {
        if (analiseDTO.getId() == null) {
            return criarAnalise(analiseDTO);
        } else {
            AnaliseDTO analise = analiseService.atualizarAnalise(analiseDTO);
            if (analise.isBloqueiaAnalise()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "analiseblocked", "You cannot edit an blocked analise")).body(null);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, analise.getId().toString())).body(analise);
        }
    }

    @PutMapping("/analises/{id}/block")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_BLOQUEAR_DESBLOQUEAR")
    public ResponseEntity<AnaliseEditDTO> bloquearDesbloquearAnalise(@PathVariable Long id) {
        log.debug("REST request to block Analise : {}", id);
        AnaliseEditDTO analiseEditDTO = analiseService.bloquearDesbloquearAnalise(id);
        if (analiseEditDTO != null) {
            return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, analiseEditDTO.getId().toString())).body(analiseEditDTO);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AnaliseEditDTO());
        }
    }

    @GetMapping("/analises/clonar/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_CLONAR")
    public ResponseEntity<AnaliseDTO> cloneAnalise(@PathVariable Long id) {
        AnaliseDTO analiseDto = analiseService.clonarAnalise(id, null);
        if (analiseDto.getId() != null) {
            return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, analiseDto.getId().toString())).body(analiseDto);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AnaliseDTO());
        }
    }

    @GetMapping("/analises/clonar/{id}/{idEquipe}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_CLONAR_EQUIPE")
    public ResponseEntity<AnaliseDTO> cloneAnaliseToEquipe(@PathVariable Long id, @PathVariable Long idEquipe) {
        AnaliseDTO analiseEditDTO = analiseService.clonarAnalise(id, idEquipe);
        if (analiseEditDTO.getId() != null) {
            return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, analiseEditDTO.getId().toString())).body(analiseEditDTO);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AnaliseDTO());
        }
    }

    @GetMapping("/analises/{id}")
    @Timed
    @Secured({"ROLE_ABACO_ANALISE_CONSULTAR", "ROLE_ABACO_ANALISE_EDITAR"})
    public ResponseEntity<AnaliseEditDTO> obterAnalise(@PathVariable Long id) {
        Analise analise = analiseService.recuperarAnalise(id);
        if (analise != null) {
            return ResponseUtil.wrapOrNotFound(Optional.ofNullable(analiseService.obterAnalise(id)));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    @GetMapping("/analises/view/{id}")
    @Timed
    public ResponseEntity<AnaliseEditDTO> visualizarAnalise(@PathVariable Long id) {
        if (analiseService.recuperarAnalise(id) != null) {
            return ResponseUtil.wrapOrNotFound(Optional.ofNullable(analiseService.converterParaAnaliseEditDTO(analiseService.recuperarAnalise(id))));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    @GetMapping("/analises/baseline")
    @Timed
    public List<Analise> obterTodasBaselines(@RequestParam(value = "sistema", required = true) String sistema) {
        return analiseRepository.findAllByBaseline();
    }

    @DeleteMapping("/analises/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EXCLUIR")
    public ResponseEntity<Void> deletarAnalise(@PathVariable Long id) {
        if (analiseService.recuperarAnalise(id) != null) {
            analiseService.deletarAnalise(id);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/compartilhada/{idAnalise}")
    @Timed
    public List<Compartilhada> obterTodasAnalisesCompartilhadas(@PathVariable Long idAnalise) {
        return compartilhadaRepository.findAllByAnaliseId(idAnalise);
    }

    @PostMapping("/analises/compartilhar")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_COMPARTILHAR")
    public ResponseEntity<AbacoMensagens> preencherCompartilhar(@Valid @RequestBody Set<CompartilhadaDTO> compartilhadaList, @RequestParam(value = "ehMultiplo", required = false) Boolean ehMultiplo) {
        return new ResponseEntity<>(analiseService.preencherCompartilhar(compartilhadaList, ehMultiplo), HttpStatus.OK);
    }


    @DeleteMapping("/analises/compartilhar/delete/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_COMPARTILHAR")
    public ResponseEntity<Void> deletarAnaliseCompartilhada(@PathVariable Long id) {
        analiseService.deletarAnaliseCompartilhada(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @PutMapping("/analises/compartilhar/viewonly/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_COMPARTILHAR")
    public ResponseEntity<Compartilhada> visualizarApenas(@Valid @RequestBody CompartilhadaDTO compartilhadaDTO) {
        Compartilhada result = analiseService.salvarCompartilhada(compartilhadaDTO);
        return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, compartilhadaDTO.getId().toString())).body(result);
    }


    @GetMapping("/relatorioPdfArquivo/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR")
    public ResponseEntity<byte[]> downloadPdfArquivo(@PathVariable Long id) throws IOException, JRException {
        return relatorioAnaliseRest.downloadPdfArquivo(analiseService.obterAnaliseSetarFuncoes(id), TipoRelatorio.ANALISE);
    }

    @GetMapping("/relatorioPdfBrowser/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR")
    public @ResponseBody
    ResponseEntity<byte[]> downloadPdfBrowser(@PathVariable Long id) throws IOException, JRException {
        return relatorioAnaliseRest.downloadPdfBrowser(analiseService.obterAnaliseSetarFuncoes(id), TipoRelatorio.ANALISE);
    }

    @GetMapping("/downloadPdfDetalhadoBrowser/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR_RELATORIO_DETALHADO")
    public @ResponseBody
    ResponseEntity<byte[]> downloadPdfDetalhadoBrowser(@PathVariable Long id) throws IOException, JRException {
        return relatorioAnaliseRest.downloadPdfBrowser(analiseService.obterAnaliseSetarFuncoes(id), TipoRelatorio.ANALISE);
    }

    @GetMapping("/downloadRelatorioExcel/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR_RELATORIO_EXCEL")
    public @ResponseBody
    ResponseEntity<byte[]> downloadRelatorioExcel(@PathVariable Long id) throws IOException, JRException {
        return relatorioAnaliseRest.downloadExcel(analiseService.recuperarAnalise(id), analiseService.arquivosCarregados(id));
    }

    @GetMapping("/relatorioContagemPdf/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR_RELATORIO_FUNDAMENTACAO")
    public @ResponseBody
    ResponseEntity<InputStreamResource> gerarRelatorioContagemPdf(@PathVariable Long id) throws IOException {
        return relatorioAnaliseRest.downloadReportContagem(analiseService.obterAnaliseSetarFuncoes(id));
    }

    @GetMapping(value = "/analise/exportaPdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR")
    public ResponseEntity<InputStreamResource> gerarRelatorioPdf(@RequestParam(defaultValue = "*") String query) throws RelatorioException {
        return DynamicExporter.output(analiseService.gerarRelatorioPdf(query), "relatorio" + "pdf");
    }

    @PostMapping(value = "/analise/exportacao/{tipoRelatorio}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR")
    public ResponseEntity<InputStreamResource> gerarRelatorioExportacao(@PathVariable String tipoRelatorio, @RequestBody AnaliseFilterDTO filter) throws RelatorioException {
        return DynamicExporter.output(analiseService.gerarRelatorioExportacao(tipoRelatorio, filter), NOME_RELATORIO + tipoRelatorio);
    }


    @PostMapping(value = "/analise/exportacao-arquivo", produces = MediaType.APPLICATION_PDF_VALUE)
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR")
    public ResponseEntity<byte[]> gerarRelatorioAnaliseImprimir(@RequestBody AnaliseFilterDTO filter) {
        return new ResponseEntity<>(analiseService.gerarRelatorioAnaliseImprimir(filter).toByteArray(), HttpStatus.OK);
    }

    @GetMapping("/analises")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_ACESSAR")
    public ResponseEntity<List<AnaliseDTO>> obterTodasAnalisesEquipes(@RequestParam(defaultValue = "ASC", required = false) String order,
                                                                      @RequestParam(defaultValue = "0", name = PAGE) int pageNumber,
                                                                      @RequestParam(defaultValue = "20") int size,
                                                                      @RequestParam(defaultValue = "id") String sort,
                                                                      @RequestParam(value = "identificadorAnalise", required = false) String identificador,
                                                                      @RequestParam(value = "sistema", required = false) Set<Long> sistema,
                                                                      @RequestParam(value = "metodoContagem", required = false) Set<MetodoContagem> metodo,
                                                                      @RequestParam(value = "organizacao", required = false) Set<Long> organizacao,
                                                                      @RequestParam(value = "equipe", required = false) Long equipe,
                                                                      @RequestParam(value = "status", required = false) Set<Long> status,
                                                                      @RequestParam(value = "usuario", required = false) Set<Long> usuario,
                                                                      @RequestParam(value = "data", required = false) TipoDeDataAnalise data,
                                                                      @RequestParam(value = "dataInicio", required = false) Date dataInicio,
                                                                      @RequestParam(value = "dataFim", required = false) Date dataFim) throws URISyntaxException {
        AnalisePesquisaDTO pesquisaDTO = new AnalisePesquisaDTO();
        preencherOrganizacaoEquipeUsuarioStatusFiltro(pesquisaDTO, organizacao, equipe, status, usuario);
        preencherIdentificadorSistemaMetodoFiltro(pesquisaDTO, identificador, sistema, metodo, null);
        preencherSortPageNumberFiltro(pesquisaDTO, order, pageNumber, size, sort);
        preencherDatasFiltro(pesquisaDTO, data, dataInicio, dataFim);
        Page<AnaliseDTO> dtoPage = analiseService.obterTodasAnalisesEquipes(pesquisaDTO);
        return new ResponseEntity<>(dtoPage.getContent().stream().filter(analise -> analise.getId() != null).collect(Collectors.toList()), PaginationUtil.generatePaginationHttpHeaders(dtoPage, API_ANALISES), HttpStatus.OK);
    }




    @GetMapping("/analises/update-pf/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EDITAR")
    public ResponseEntity<AnaliseEditDTO> atualizarSomaPF(@PathVariable Long id) {
        AnaliseEditDTO analiseEditDTO = analiseService.atualizarSomaPF(id);
        if (analiseService.recuperarAnalise(id) != null) {
            return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, analiseEditDTO.getId().toString())).body(analiseEditDTO);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AnaliseEditDTO());
    }

    @GetMapping("/analises/update-divergente-pf/{id}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EDITAR")
    public ResponseEntity<AnaliseEditDTO> atualizarSomaDivergentePF(@PathVariable Long id) {
        AnaliseEditDTO analiseEditDTO = analiseService.atualizarSomaDivergentePF(id);
        if (analiseEditDTO.getId() != null) {
            return ResponseEntity.ok().headers(HeaderUtil.blockEntityUpdateAlert(ENTITY_NAME, analiseEditDTO.getId().toString())).body(analiseEditDTO);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AnaliseEditDTO());
    }

    @GetMapping("/analises/change-status/{id}/{idStatus}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_ALTERAR_STATUS")
    public ResponseEntity<AnaliseFormulario> alterarStatusAnalise(@PathVariable Long id, @PathVariable Long idStatus) {
        AnaliseFormulario analiseFormulario = analiseService.alterarStatusAnalise(id, idStatus);
        return ResponseEntity.status(HttpStatus.OK).body(analiseFormulario);
    }

    @GetMapping("/analises/gerar-divergencia/{idAnalisePadrao}/{idAnaliseComparada}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_GERAR_VALIDACAO")
    public ResponseEntity<AnaliseEditDTO> gerarDivergencia2(@PathVariable Long idAnalisePadrao, @PathVariable Long idAnaliseComparada, @RequestParam(value = "isUnion", defaultValue = "false") boolean isUnionFunction) {
        return ResponseEntity.ok(analiseService.gerarDivergencia2(idAnalisePadrao, idAnaliseComparada, isUnionFunction));
    }

    @GetMapping("/analises/divergente/update/{id}")
    @Timed
    @Secured("ROLE_ABACO_VALIDACAO_EDITAR")
    public ResponseEntity<AnaliseEditDTO> atualizarAnaliseDivergente(@PathVariable Long id) {
        return ResponseEntity.ok(analiseService.atualizarAnaliseDivergente(id));
    }

    @GetMapping(value = "/analises/importar-excel/{id}/{modelo}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR_RELATORIO_EXCEL")
    public ResponseEntity<byte[]> exportarExcel(@PathVariable Long id, @PathVariable Long modelo) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        String extensaoModelo = modelo == 6 ? "xls" : "xlsx";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s.%s", RelatorioUtil.pegarNomeRelatorio(analiseService.recuperarAnalise(id)), extensaoModelo));
        return new ResponseEntity<>(analiseService.exportarExcel(id, modelo), headers, HttpStatus.OK);
    }

    @GetMapping("/analises/analise-json/{id}")
    public AnaliseJsonDTO analiseJson(@PathVariable Long id) {
        return analiseService.converterParaAnaliseJsonDTO(analiseRepository.findById(id));
    }

    @PostMapping("/analises/importar-excel/Xlsx")
    public ResponseEntity<Analise> carregarArquivoExcel(@RequestParam("file") MultipartFile file) throws IOException {
        Analise analise = analiseService.uploadExcel(file);
        return ResponseEntity.status(HttpStatus.OK).body(analise);
    }

    @PostMapping("/analises/importar-Excel")
    public ResponseEntity<Analise> importarAnaliseExcel(@Valid @RequestBody AnaliseEditDTO analise) {
        if (analiseService.verificaModulos(analise)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok().body(analiseService.importarAnaliseExcel(analise));
    }

    @PostMapping("/analises/carregarAnalise")
    public ResponseEntity<Analise> carregarAnaliseExcel(@Valid @RequestBody AnaliseDTO analiseDTO) {
        AnaliseDTO novaAnalise = analiseService.carregarAnaliseExcel(analiseDTO);
        analiseService.carregarDadosExcel(analiseService.converterParaEntidade(novaAnalise), analiseService.converterParaEntidade(analiseDTO));
        return ResponseEntity.status(HttpStatus.OK).body(analiseService.converterParaEntidade(novaAnalise));
    }

    @GetMapping("/analises/FD")
    public ResponseEntity<List<AnaliseDTO>> carregarAnalisesFD(@RequestParam(name = "nomeFuncao") String nomeFuncao,
                                                               @RequestParam(name = "nomeModulo") String nomeModulo,
                                                               @RequestParam(name = "nomeFuncionalidade") String nomeFuncionalidade,
                                                               @RequestParam(name = "nomeSistema") String nomeSistema,
                                                               @RequestParam(name = "nomeEquipe") String nomeEquipe) {
        List<AnaliseDTO> analises = analiseService.carregarAnalisesFromFuncaoFD(nomeFuncao, nomeModulo, nomeFuncionalidade, nomeSistema, nomeEquipe);
        return new ResponseEntity<>(analises, HttpStatus.OK);
    }

    @GetMapping("/analises/FT")
    public ResponseEntity<List<AnaliseDTO>> carregarAnalisesFT(@RequestParam(name = "nomeFuncao") String nomeFuncao,
                                                               @RequestParam(name = "nomeModulo") String nomeModulo,
                                                               @RequestParam(name = "nomeFuncionalidade") String nomeFuncionalidade,
                                                               @RequestParam(name = "nomeSistema") String nomeSistema,
                                                               @RequestParam(name = "nomeEquipe") String nomeEquipe) {
        List<AnaliseDTO> analises = analiseService.carregarAnalisesFromFuncaoFT(nomeFuncao, nomeModulo, nomeFuncionalidade, nomeSistema, nomeEquipe);
        return new ResponseEntity<>(analises, HttpStatus.OK);
    }

    @PatchMapping("/analises/atualizar-encerramento")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_EDITAR")
    public ResponseEntity<Void> atualizarEncerramentoAnalise(@RequestBody AnaliseEncerramentoDTO analiseDTO) {
        analiseService.atualizarEncerramentoAnalise(analiseDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/divergencia/relatorioPdfArquivo/{id}")
    @Timed
    @Secured("ROLE_ABACO_VALIDACAO_EXPORTAR")
    public ResponseEntity<byte[]> downloadDivergenciaPdfArquivo(@PathVariable Long id) throws IOException, JRException {
        return relatorioAnaliseRest.downloadPdfArquivo(analiseService.obterAnaliseSetarFuncoes(id), TipoRelatorio.ANALISE);
    }

    @GetMapping("/divergencia/downloadRelatorioExcel/{id}")
    @Timed
    @Secured("ROLE_ABACO_VALIDACAO_EXPORTAR")
    public @ResponseBody
    ResponseEntity<byte[]> downloadDivergenciaRelatorioExcel(@PathVariable Long id) throws IOException, JRException {
        return relatorioAnaliseRest.downloadExcel(analiseService.obterAnaliseSetarFuncoes(id), analiseService.arquivosCarregados(id));
    }

    @PostMapping(value = "/divergencia/exportacao-arquivo", produces = MediaType.APPLICATION_PDF_VALUE)
    @Timed
    @Secured("ROLE_ABACO_VALIDACAO_EXPORTAR")
    public ResponseEntity<byte[]> gerarRelatorioDivergenciaImprimir(@RequestBody AnaliseFilterDTO filter, @ApiParam Pageable pageable) throws RelatorioException {
        return new ResponseEntity<>(analiseService.gerarRelatorioDivergenciaImprimir(filter), HttpStatus.OK);
    }

    @PostMapping(value = "/divergencia/exportacao/{tipoRelatorio}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Timed
    @Secured("ROLE_ABACO_VALIDACAO_EXPORTAR")
    public ResponseEntity<InputStreamResource> gerarRelatorioDivergenciaExportacao(@PathVariable String tipoRelatorio, @RequestBody AnaliseFilterDTO filter, @ApiParam Pageable pageable) throws RelatorioException {
        return DynamicExporter.output(analiseService.gerarRelatorioDivergenciaExportacao(tipoRelatorio, filter), NOME_RELATORIO + tipoRelatorio);
    }

    @GetMapping("/analises/divergencia/{idAnaliseComparada}")
    @Timed
    @Secured("ROLE_ABACO_ANALISE_GERAR_VALIDACAO")
    public ResponseEntity<AnaliseEditDTO> gerarDivergencia1(@PathVariable Long idAnaliseComparada) {
        return ResponseEntity.ok(analiseService.gerarDivergencia1(idAnaliseComparada));
    }

    @GetMapping("/divergencia")
    @Timed
    @Secured({"ROLE_ABACO_VALIDACAO_ACESSAR", "ROLE_ABACO_VALIDACAO_PESQUISAR"})
    public ResponseEntity<List<AnaliseDTO>> obterDivergencias(@RequestParam(defaultValue = "ASC", required = false) String order,
                                                              @RequestParam(defaultValue = "0", name = PAGE) int pageNumber,
                                                              @RequestParam(defaultValue = "20") int size,
                                                              @RequestParam(defaultValue = "dataCriacaoOrdemServico") String sort,
                                                              @RequestParam(value = "identificador", required = false) String identificador,
                                                              @RequestParam(value = "sistema", required = false) Set<Long> sistema,
                                                              @RequestParam(value = "organizacao", required = false) Set<Long> organizacao,
                                                              @RequestParam(value = "status", required = false) Set<Long> status,
                                                              @RequestParam(value = "bloqueiaAnalise", required = false) Boolean bloqueado) throws URISyntaxException {
        AnalisePesquisaDTO pesquisaDTO = new AnalisePesquisaDTO();
        preencherOrganizacaoEquipeUsuarioStatusFiltro(pesquisaDTO, organizacao, null, status, null);
        preencherIdentificadorSistemaMetodoFiltro(pesquisaDTO, identificador, sistema, null, bloqueado);
        preencherSortPageNumberFiltro(pesquisaDTO, order, pageNumber, size, sort);
        Page<AnaliseDTO> dtoPage = analiseService.obterDivergencias(pesquisaDTO);
        return new ResponseEntity<>(dtoPage.getContent(), PaginationUtil.generatePaginationHttpHeaders(dtoPage, API_ANALISES), HttpStatus.OK);
    }

    @GetMapping("/divergencia/{id}")
    @Timed
    @Secured({"ROLE_ABACO_VALIDACAO_CONSULTAR", "ROLE_ABACO_VALIDACAO_EDITAR"})
    public ResponseEntity<AnaliseDivergenceEditDTO> obterDivergencia(@PathVariable Long id) {
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(analiseService.obterDivergencia(id)));
    }

    @DeleteMapping("/divergencia/{id}")
    @Timed
    @Secured("ROLE_ABACO_VALIDACAO_EXCLUIR")
    public ResponseEntity<Void> deletarAnaliseDivergencia(@PathVariable Long id) {
        analiseService.deletarAnaliseDivergencia(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping(value = "/divergencia/importar-excel/{id}/{modelo}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Secured("ROLE_ABACO_ANALISE_EXPORTAR_RELATORIO_EXCEL")
    public ResponseEntity<byte[]> importarExcelDivergencia(@PathVariable Long id, @PathVariable Long modelo) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        String extensaoModelo = modelo == 6 ? "xls" : "xlsx";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s.%s", RelatorioUtil.pegarNomeRelatorio(analiseService.recuperarAnalise(id)), extensaoModelo));
        return new ResponseEntity<>(analiseService.importarExcelDivergencia(id, modelo), headers, HttpStatus.OK);
    }

    private void preencherOrganizacaoEquipeUsuarioStatusFiltro(AnalisePesquisaDTO pesquisaDTO,Set<Long> organizacao, Long equipe, Set<Long> status, Set<Long> usuario) {
        pesquisaDTO.setOrganizacao(organizacao);
        if (equipe != null) {
            pesquisaDTO.setEquipe(equipe);
        }
        if (usuario != null) {
            pesquisaDTO.setUsuario(usuario);
        }
        pesquisaDTO.setStatus(status);
    }

    private void preencherIdentificadorSistemaMetodoFiltro(AnalisePesquisaDTO pesquisaDTO, String identificador, Set<Long> sistema, Set<MetodoContagem> metodo, Boolean bloqueado) {
        pesquisaDTO.setIdentificador(identificador);
        pesquisaDTO.setSistema(sistema);
        if (metodo != null) {
            pesquisaDTO.setMetodo(metodo);
        }
        if (bloqueado != null) {
            pesquisaDTO.setBloqueado(bloqueado);
        }
    }

    private void preencherSortPageNumberFiltro(AnalisePesquisaDTO pesquisaDTO, String order, int pageNumber, int size, String sort) {
        pesquisaDTO.setOrder(order);
        pesquisaDTO.setPageNumber(pageNumber);
        pesquisaDTO.setSize(size);
        pesquisaDTO.setSort(sort);
    }

    private void preencherDatasFiltro(AnalisePesquisaDTO pesquisaDTO, TipoDeDataAnalise data, Date dataInicio, Date dataFim) {
        pesquisaDTO.setData(data);
        pesquisaDTO.setDataInicio(dataInicio);
        pesquisaDTO.setDataFim(dataFim);
    }

}


