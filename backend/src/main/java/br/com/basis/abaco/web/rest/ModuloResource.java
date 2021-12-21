package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.domain.Funcionalidade;
import br.com.basis.abaco.domain.Modulo;
import br.com.basis.abaco.repository.ModuloRepository;
import br.com.basis.abaco.repository.search.ModuloSearchRepository;
import br.com.basis.abaco.service.dto.filter.SearchFilterDTO;
import br.com.basis.abaco.service.exception.RelatorioException;
import br.com.basis.abaco.service.relatorio.RelatorioModuloColunas;
import br.com.basis.abaco.service.relatorio.RelatorioStatusColunas;
import br.com.basis.abaco.utils.AbacoUtil;
import br.com.basis.abaco.web.rest.util.HeaderUtil;
import br.com.basis.dynamicexports.service.DynamicExportsService;
import br.com.basis.dynamicexports.util.DynamicExporter;
import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * REST controller for managing Modulo.
 */
@RestController
@RequestMapping("/api")
public class ModuloResource {

    private final Logger log = LoggerFactory.getLogger(ModuloResource.class);

    private static final String ENTITY_NAME = "modulo";

    private final ModuloRepository moduloRepository;

    private final ModuloSearchRepository moduloSearchRepository;

    private final DynamicExportsService dynamicExportsService;

    public ModuloResource(ModuloRepository moduloRepository, ModuloSearchRepository moduloSearchRepository, DynamicExportsService dynamicExportsService) {
        this.moduloRepository = moduloRepository;
        this.moduloSearchRepository = moduloSearchRepository;
        this.dynamicExportsService = dynamicExportsService;
    }

    /**
     * POST  /modulos : Create a new modulo.
     *
     * @param modulo the modulo to create
     * @return the ResponseEntity with status 201 (Created) and with body the new modulo, or with status 400 (Bad Request) if the modulo has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/modulos")
    @Timed
    public ResponseEntity<Modulo> createModulo(@Valid @RequestBody Modulo modulo) throws URISyntaxException {
        log.debug("REST request to save Modulo : {}", modulo);
        if (modulo.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new modulo cannot already have an ID")).body(null);
        }

        Optional<List<Modulo>> findModulo = moduloRepository.findAllByNomeAndSistemaId(modulo.getNome().toLowerCase(), modulo.getSistema().getId());
        if(findModulo.isPresent() && !findModulo.get().isEmpty()){
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "moduloexists", "Modulo name and system already in use")).body(null);
        }
        Modulo result = moduloRepository.save(modulo);
        moduloSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/modulos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }


    /**
     * PUT  /modulos : Updates an existing modulo.
     *
     * @param modulo the modulo to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated modulo,
     * or with status 400 (Bad Request) if the modulo is not valid,
     * or with status 500 (Internal Server Error) if the modulo couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/modulos")
    @Timed
    public ResponseEntity<Modulo> updateModulo(@Valid @RequestBody Modulo modulo) throws URISyntaxException {
        log.debug("REST request to update Modulo : {}", modulo);
        if (modulo.getId() == null) {
            return createModulo(modulo);
        }
        Modulo result = moduloRepository.save(modulo);
        moduloSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, modulo.getId().toString()))
            .body(result);
    }

    /**
     * GET  /modulos : get all the modulos.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of modulos in body
     */
    @GetMapping("/modulos")
    @Timed
    public List<Modulo> getAllModulos() {
        log.debug("REST request to get all Modulos");
        return moduloRepository.findAll();
    }

    /**
     * GET  /modulos/:id : get the "id" modulo.
     *
     * @param id the id of the modulo to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the modulo, or with status 404 (Not Found)
     */
    @GetMapping("/modulos/{id}")
    @Timed
    public ResponseEntity<Modulo> getModulo(@PathVariable Long id) {
        log.debug("REST request to get Modulo : {}", id);
        Modulo modulo = moduloRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(modulo));
    }



    @GetMapping("/modulos/funcionalidade/{id}")
    @Timed
    public ResponseEntity<Modulo>getModuloByFuncionalidade(@PathVariable Long id) {
        log.debug("REST request to get Modulo by Funcionalidade : {}", id);
        Modulo modulo = moduloRepository.findByFuncionalidade(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(modulo));
    }

    /**
     * DELETE  /modulos/:id : delete the "id" modulo.
     *
     * @param id the id of the modulo to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/modulos/{id}")
    @Timed
    public ResponseEntity<Void> deleteModulo(@PathVariable Long id) {
        log.debug("REST request to delete Modulo : {}", id);
        moduloRepository.delete(id);
        moduloSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/modulos?query=:query : search for the modulo corresponding
     * to the query.
     *
     * @param query the query of the modulo search
     * @return the result of the search
     */
    @GetMapping("/_search/modulos")
    @Timed
    public List<Modulo> searchModulos(@RequestParam(defaultValue = "*") String query) {
        log.debug("REST request to search Modulos for query {}", query);
        return StreamSupport
            .stream(moduloSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

    @PostMapping(value = "/modulos/exportacao/{tipoRelatorio}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Timed
    public ResponseEntity<InputStreamResource> gerarRelatorioExportacao(@PathVariable String tipoRelatorio, @RequestBody SearchFilterDTO filtro) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream = this.gerarRelatorio(filtro, tipoRelatorio);
        return DynamicExporter.output(byteArrayOutputStream, "relatorio." + tipoRelatorio);
    }

    @PostMapping(value = "/modulos/exportacao-arquivo", produces = MediaType.APPLICATION_PDF_VALUE)
    @Timed
    public ResponseEntity<byte[]> gerarRelatorioImprimir(@RequestBody SearchFilterDTO filtro)
        throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream = this.gerarRelatorio(filtro, "pdf");
        return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), HttpStatus.OK);
    }

    public ByteArrayOutputStream gerarRelatorio(SearchFilterDTO filtro, String tipoRelatorio) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            Page<Modulo> page = new PageImpl<>(filtro.getModulos(), dynamicExportsService.obterPageableMaximoExportacao(), filtro.getModulos().size());
            byteArrayOutputStream = dynamicExportsService.export(new RelatorioModuloColunas(), page, tipoRelatorio,
                Optional.empty(), Optional.ofNullable(AbacoUtil.REPORT_LOGO_PATH),
                Optional.ofNullable(AbacoUtil.getReportFooter()));
        } catch (DRException | ClassNotFoundException | JRException | NoClassDefFoundError e) {
            throw new RelatorioException(e);
        }

        return byteArrayOutputStream;
    }
}
