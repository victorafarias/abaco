package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.service.HistoricoService;
import br.com.basis.abaco.service.dto.AnaliseDTO;
import br.com.basis.abaco.service.dto.HistoricoDTO;
import br.com.basis.abaco.service.exception.RelatorioException;
import br.com.basis.dynamicexports.util.DynamicExporter;
import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Historico.
 */
@RestController
@RequestMapping("/api/historico")
public class HistoricoResource {

    private final HistoricoService historicoService;

    public HistoricoResource(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    /**
     * GET  /historico/:id : get the "id" analise.
     *
     * @param idAnalise the id of the analise to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the lista Historico, or with status 404 (Not Found)
     */
    @GetMapping("/{idAnalise}")
    @Timed
    public ResponseEntity<List<HistoricoDTO>> getListHistorico(@PathVariable Long idAnalise) {
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(historicoService.findAllByAnalise(idAnalise)));
    }

    @PostMapping
    public ResponseEntity<Void> inserirHistoricoAnalise(@RequestBody HistoricoDTO historicoDTO){
        if(historicoDTO.getAcao() != null && historicoDTO.getAnalise() != null){
            Analise analise = new Analise();
            analise.setId(historicoDTO.getAnalise().getId());
            historicoService.inserirHistoricoAnalise(analise, null, historicoDTO.getAcao());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }



    @PostMapping(value = "/exportacao/{tipoRelatorio}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Timed
    public ResponseEntity<InputStreamResource> gerarRelatorio(@PathVariable String tipoRelatorio, @RequestBody AnaliseDTO analise, @RequestParam(defaultValue = "*") String query) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream = historicoService.gerarRelatorio(analise, tipoRelatorio);
        return DynamicExporter.output(byteArrayOutputStream, "relatorio");
    }

    @PostMapping(value = "/exportacao-arquivo", produces = MediaType.APPLICATION_PDF_VALUE)
    @Timed
    public ResponseEntity<byte[]> gerarRelatorioImprimir(@RequestBody AnaliseDTO analise, @RequestParam(defaultValue = "*") String query) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream = historicoService.gerarRelatorio(analise, "pdf");
        return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), HttpStatus.OK);
    }

}
