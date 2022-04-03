package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.service.HistoricoService;
import br.com.basis.abaco.service.dto.HistoricoDTO;
import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Historico.
 */
@RestController
@RequestMapping("/api")
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
    @GetMapping("/historico/{idAnalise}")
    @Timed
    public ResponseEntity<List<HistoricoDTO>> getListHistorico(@PathVariable Long idAnalise) {
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(historicoService.findAllByAnalise(idAnalise)));
    }

}
